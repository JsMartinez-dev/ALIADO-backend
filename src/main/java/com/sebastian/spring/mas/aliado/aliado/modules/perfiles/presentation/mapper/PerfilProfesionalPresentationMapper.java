package com.sebastian.spring.mas.aliado.aliado.modules.perfiles.presentation.mapper;

import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.dto.VistaPreviaPerfilResult;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.dto.GuardarCompromisoAgendaCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.dto.EliminarServicioPortafolioCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.dto.DisponibilidadDatos;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.dto.ConfigurarDisponibilidadCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.dto.AgregarServicioPortafolioCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.dto.RegistrarExperienciaProfesionalCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.dto.EditarPerfilProfesionalCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.dto.ActualizarZonasAtencionCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.dto.ActualizarCategoriasProfesionalesCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.dto.CrearPerfilProfesionalCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.dto.ExperienciaDatos;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.dto.PerfilProfesionalResult;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.presentation.dto.DatosGeneralesDtos;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.presentation.dto.PerfilProfesionalDtos;
import java.util.List;
import java.util.UUID;

public final class PerfilProfesionalPresentationMapper {
    private PerfilProfesionalPresentationMapper() {}

    public static ActualizarCategoriasProfesionalesCommand aCategoriasCommand(
            UUID prestadorId, PerfilProfesionalDtos.IdsRequest request) {
        return new ActualizarCategoriasProfesionalesCommand(prestadorId, request.ids());
    }

    public static ActualizarZonasAtencionCommand aZonasCommand(
            UUID prestadorId, PerfilProfesionalDtos.IdsRequest request) {
        return new ActualizarZonasAtencionCommand(prestadorId, request.ids());
    }

    public static RegistrarExperienciaProfesionalCommand aExperienciaCommand(
            UUID prestadorId, PerfilProfesionalDtos.ExperienciaRequest request) {
        return new RegistrarExperienciaProfesionalCommand(
                prestadorId, request.descripcion(), request.fechaInicio(), request.fechaFin());
    }

    public static PerfilProfesionalDtos.ExperienciaResponse aExperienciaResponse(PerfilProfesionalResult.ExperienciaResult result) {
        return new PerfilProfesionalDtos.ExperienciaResponse(result.id(), result.descripcion(), result.fechaInicio(), result.fechaFin());
    }

    public static EditarPerfilProfesionalCommand aEditarCommand(
            UUID prestadorId, PerfilProfesionalDtos.ActualizarDatosRequest request) {
        var datos = request.datosGenerales();
        return new EditarPerfilProfesionalCommand(prestadorId, datos == null ? null : datos.descripcion());
    }

    public static AgregarServicioPortafolioCommand aPortafolioCommand(UUID prestadorId,
            String descripcion, String nombreArchivo, String tipoContenido, byte[] contenido) {
        return new AgregarServicioPortafolioCommand(
                prestadorId, descripcion, nombreArchivo, tipoContenido, contenido);
    }
    public static PerfilProfesionalDtos.PortafolioResponse aPortafolioResponse(PerfilProfesionalResult.PortafolioResult result) {
        return new PerfilProfesionalDtos.PortafolioResponse(result.id(), result.descripcion(), result.evidenciaKey(), result.fechaCreacion());
    }

    public static EliminarServicioPortafolioCommand aEliminarPortafolioCommand(UUID prestadorId, UUID servicioId) {
        return new EliminarServicioPortafolioCommand(prestadorId, servicioId);
    }

    public static ConfigurarDisponibilidadCommand aDisponibilidadCommand(UUID prestadorId,
            PerfilProfesionalDtos.DisponibilidadRequest request) {
        var bloques = request.bloques() == null ? null : request.bloques().stream().map(b -> b == null ? null : new
                DisponibilidadDatos(b.diaSemana(),b.horaInicio(),b.horaFin())).toList();
        return new ConfigurarDisponibilidadCommand(prestadorId,bloques);
    }
    public static GuardarCompromisoAgendaCommand aCompromisoCommand(UUID prestadorId,
            PerfilProfesionalDtos.CompromisoRequest request) {
        return new GuardarCompromisoAgendaCommand(prestadorId,request.id(),request.fechaHoraInicio(),request.fechaHoraFin());
    }
    public static PerfilProfesionalDtos.CompromisoResponse aCompromisoResponse(PerfilProfesionalResult.CompromisoResult result) {
        return new PerfilProfesionalDtos.CompromisoResponse(result.id(),result.fechaHoraInicio(),result.fechaHoraFin(),result.origen());
    }
    public static PerfilProfesionalDtos.VistaPreviaResponse aVistaPreviaResponse(VistaPreviaPerfilResult result) {
        return new PerfilProfesionalDtos.VistaPreviaResponse(result.prestadorId(),result.categorias(),result.zonas(),result.reputacion(),
                result.cantidadCalificaciones(),result.nivelVerificacion(),result.disponibilidad().stream().map(d -> new
                    PerfilProfesionalDtos.DisponibilidadResponse(d.id(),d.diaSemana(),d.horaInicio(),d.horaFin())).toList(),
                DatosGeneralesDtos.DatosGeneralesResponse.desde(result.datosGenerales()),
                result.resenas().stream().map(r -> new PerfilProfesionalDtos.ResenaPublicaResponse(r.id(),r.valor(),r.texto(),r.fechaCreacion())).toList());
    }

    public static CrearPerfilProfesionalCommand aCommand(UUID prestadorId, PerfilProfesionalDtos.CrearRequest request) {
        List<ExperienciaDatos> experiencias = request.experiencia() == null ? List.of()
                : request.experiencia().stream().map(e -> e == null ? new ExperienciaDatos(null, null, null) : new ExperienciaDatos(
                        e.descripcion(), e.fechaInicio(), e.fechaFin())).toList();
        var datos = request.datosGenerales();
        return new CrearPerfilProfesionalCommand(prestadorId, datos == null ? null : datos.descripcion(),
                request.categoriaIds(), request.zonaIds(), experiencias);
    }

    public static PerfilProfesionalDtos.PerfilResponse aResponse(PerfilProfesionalResult result) {
        return new PerfilProfesionalDtos.PerfilResponse(result.id(), result.prestadorId(),
                DatosGeneralesDtos.DatosGeneralesResponse.desde(result.datosGenerales()),
                result.estadoExposicion(), result.categoriaIds(), result.zonaIds(),
                result.experiencias().stream().map(e -> new PerfilProfesionalDtos.ExperienciaResponse(
                        e.id(), e.descripcion(), e.fechaInicio(), e.fechaFin())).toList(),
                result.portafolio().stream().map(p -> new PerfilProfesionalDtos.PortafolioResponse(
                        p.id(), p.descripcion(), p.evidenciaKey(), p.fechaCreacion())).toList(),
                result.disponibilidades().stream().map(d -> new PerfilProfesionalDtos.DisponibilidadResponse(
                        d.id(), d.diaSemana(), d.horaInicio(), d.horaFin())).toList(),
                result.compromisos().stream().map(c -> new PerfilProfesionalDtos.CompromisoResponse(
                        c.id(), c.fechaHoraInicio(), c.fechaHoraFin(), c.origen())).toList());
    }
}
