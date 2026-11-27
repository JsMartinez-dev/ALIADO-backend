package com.sebastian.spring.mas.aliado.aliado.modules.perfil.infrastructure.adapter;

import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.port.ArchivosPendientesEliminacionPort;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.port.EliminacionDefinitivaPort;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class EliminacionDefinitivaAdapter implements EliminacionDefinitivaPort {
    private final JdbcTemplate jdbc;
    private final ArchivosPendientesEliminacionPort pendientes;

    public EliminacionDefinitivaAdapter(JdbcTemplate jdbc, ArchivosPendientesEliminacionPort pendientes) {
        this.jdbc = jdbc;
        this.pendientes = pendientes;
    }

    @Override
    public boolean estaEliminada(UUID usuarioId) {
        Integer count = jdbc.queryForObject(
                "select count(*) from usuarios where id = ? and estado_cuenta = 'ELIMINADA'",
                Integer.class, usuarioId);
        return count != null && count > 0;
    }

    @Override
    public void anonimizarYEncolarArchivos(UUID usuarioId, UUID operacionId) {
        if (estaEliminada(usuarioId)) {
            return;
        }
        List<String> keys = new ArrayList<>();
        keys.addAll(keys("""
                select f.archivo_key from fotos_solicitud f
                join solicitudes s on s.id = f.solicitud_id where s.cliente_id = ?
                """, usuarioId));
        keys.addAll(keys("""
                select sp.evidencia_key from servicios_portafolio sp
                join perfiles_profesionales p on p.id = sp.perfil_id
                where p.prestador_id = ? and sp.evidencia_key is not null
                """, usuarioId));
        keys.addAll(keys("""
                select dv.archivo_key from documentos_verificacion dv
                join solicitudes_verificacion sv on sv.id = dv.solicitud_verificacion_id
                where sv.prestador_id = ?
                """, usuarioId));
        keys.addAll(keys("""
                select er.archivo_key from evidencias_reporte er
                join reportes r on r.id = er.reporte_id
                left join servicios_contratados sc on sc.id = r.servicio_id
                where r.reportante_id = ? or r.usuario_objetivo_id = ?
                   or sc.cliente_id = ? or sc.prestador_id = ?
                """, usuarioId, usuarioId, usuarioId, usuarioId));
        keys.addAll(keys("""
                select archivo_key from descargas_historial
                where usuario_id = ? and archivo_key is not null
                """, usuarioId));

        jdbc.update("delete from sesiones where usuario_id = ?", usuarioId);
        jdbc.update("delete from tokens_recuperacion where usuario_id = ?", usuarioId);
        jdbc.update("delete from preferencias_notificacion where usuario_id = ?", usuarioId);
        jdbc.update("delete from notificaciones where destinatario_id = ?", usuarioId);
        jdbc.update("delete from descargas_historial where usuario_id = ?", usuarioId);
        jdbc.update("delete from progreso_formalizacion where prestador_id = ?", usuarioId);
        jdbc.update("delete from documentos_verificacion dv using solicitudes_verificacion sv "
                + "where dv.solicitud_verificacion_id = sv.id and sv.prestador_id = ?", usuarioId);
        jdbc.update("delete from solicitudes_verificacion where prestador_id = ?", usuarioId);
        jdbc.update("delete from fotos_solicitud f using solicitudes s "
                + "where f.solicitud_id = s.id and s.cliente_id = ?", usuarioId);
        jdbc.update("delete from servicios_portafolio sp using perfiles_profesionales p "
                + "where sp.perfil_id = p.id and p.prestador_id = ?", usuarioId);
        jdbc.update("delete from experiencias_profesionales ep using perfiles_profesionales p "
                + "where ep.perfil_id = p.id and p.prestador_id = ?", usuarioId);
        jdbc.update("delete from perfil_categoria pc using perfiles_profesionales p "
                + "where pc.perfil_id = p.id and p.prestador_id = ?", usuarioId);
        jdbc.update("delete from perfil_zona_atencion pz using perfiles_profesionales p "
                + "where pz.perfil_id = p.id and p.prestador_id = ?", usuarioId);
        jdbc.update("delete from disponibilidades d using perfiles_profesionales p "
                + "where d.perfil_id = p.id and p.prestador_id = ?", usuarioId);
        jdbc.update("delete from compromisos_agenda c using perfiles_profesionales p "
                + "where c.perfil_id = p.id and p.prestador_id = ?", usuarioId);
        jdbc.update("delete from perfiles_profesionales where prestador_id = ?", usuarioId);
        jdbc.update("delete from perfiles where usuario_id = ?", usuarioId);
        jdbc.update("delete from ubicaciones where usuario_id = ?", usuarioId);
        jdbc.update("delete from idempotency_keys where usuario_id = ?", usuarioId);

        jdbc.update("update solicitudes set descripcion = null where cliente_id = ?", usuarioId);
        jdbc.update("update propuestas set disponibilidad = '[Contenido eliminado]', mensaje = null "
                + "where prestador_id = ?", usuarioId);
        jdbc.update("update mensajes_servicio set contenido = '[Mensaje eliminado]' where remitente_id = ?", usuarioId);
        jdbc.update("update resenas set texto = '[Reseña eliminada]' where calificacion_id in "
                + "(select id from calificaciones where cliente_id = ? or prestador_id = ?)", usuarioId, usuarioId);
        jdbc.update("update reportes set motivo = '[Contenido eliminado]', "
                + "justificacion_resolucion = case when justificacion_resolucion is null then null "
                + "else '[Contenido eliminado]' end "
                + "where reportante_id = ? or usuario_objetivo_id = ?", usuarioId, usuarioId);
        jdbc.update("update evidencias_reporte er set archivo_key = 'ELIMINADO', tipo_contenido = 'ELIMINADO' "
                + "from reportes r left join servicios_contratados sc on sc.id = r.servicio_id "
                + "where er.reporte_id = r.id and (r.reportante_id = ? or r.usuario_objetivo_id = ? "
                + "or sc.cliente_id = ? or sc.prestador_id = ?)", usuarioId, usuarioId, usuarioId, usuarioId);
        jdbc.update("update bloqueos_preventivos set motivo = '[Contenido eliminado]' where usuario_id = ?", usuarioId);
        jdbc.update("update acciones_moderacion set motivo = '[Contenido eliminado]' "
                + "where objetivo_tipo = 'USUARIO' and objetivo_id = ?", usuarioId);

        jdbc.update("update usuarios set email = null, password_hash = null, estado_cuenta = 'ELIMINADA' where id = ?",
                usuarioId);

        for (String key : keys) {
            pendientes.encolar(key, operacionId);
        }
    }

    private List<String> keys(String sql, Object... args) {
        return jdbc.queryForList(sql, String.class, args).stream()
                .filter(k -> k != null && !k.isBlank())
                .toList();
    }
}
