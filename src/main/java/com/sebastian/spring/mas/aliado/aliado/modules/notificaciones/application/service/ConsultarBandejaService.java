package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.service;

import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.port.ReferenciaPropuestaAvisoPort;
import com.sebastian.spring.mas.aliado.aliado.common.exception.BusinessException;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.cursor.CursorBandeja;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto.AvisoBandejaResult;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto.BandejaAvisosResult;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto.ConsultarBandejaCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.usecase.ConsultarBandejaUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.EstadoLectura;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.Notificacion;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.TipoNotificacion;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.repository.NotificacionRepository;
import java.util.List;
import java.util.Objects;

/**
 * Consulta paginada de la bandeja propia (F09-BANDEJA-03).
 *
 * <p>Valida los filtros y el cursor, pide {@code limite + 1} filas al repositorio y usa la
 * fila adicional solo como indicador de otra página. El cursor siguiente se construye con
 * el par {@code (fechaGeneracion, id)} de la última fila devuelta, que es el mismo orden
 * descendente del repositorio. El destinatario procede siempre del comando de sesión; el
 * cursor nunca lo sustituye.</p>
 */
public class ConsultarBandejaService implements ConsultarBandejaUseCase {

    static final int LIMITE_PREDETERMINADO = 20;
    static final int LIMITE_MAXIMO = 100;

    private final NotificacionRepository notificaciones;

    private final ReferenciaPropuestaAvisoPort referencias;

    public ConsultarBandejaService(NotificacionRepository notificaciones, ReferenciaPropuestaAvisoPort referencias) {
        this.referencias = Objects.requireNonNull(referencias);
        this.notificaciones = Objects.requireNonNull(notificaciones);
    }

    @Override
    public BandejaAvisosResult ejecutar(ConsultarBandejaCommand command) {
        if (command == null || command.destinatarioId() == null) {
            throw datosInvalidos();
        }
        EstadoLectura estadoLectura = estadoLectura(command.estadoLectura());
        TipoNotificacion tipo = tipo(command.tipo());
        int limite = limite(command.limite());
        CursorBandeja cursor = command.cursor() == null || command.cursor().isBlank()
                ? null
                : CursorBandeja.decodificar(command.cursor(), estadoLectura, tipo);

        List<Notificacion> filas = notificaciones.buscarBandeja(command.destinatarioId(),
                estadoLectura, tipo,
                cursor == null ? null : cursor.fechaGeneracion(),
                cursor == null ? null : cursor.id(),
                limite + 1);

        boolean hayOtraPagina = filas.size() > limite;
        List<Notificacion> pagina = hayOtraPagina ? filas.subList(0, limite) : filas;
        String cursorSiguiente = hayOtraPagina && !pagina.isEmpty()
                ? CursorBandeja.codificar(pagina.get(pagina.size() - 1).fechaGeneracion(),
                        pagina.get(pagina.size() - 1).id(), estadoLectura, tipo)
                : null;

        return new BandejaAvisosResult(pagina.stream().map(this::aAviso).toList(),
                cursorSiguiente);
    }

    private AvisoBandejaResult aAviso(Notificacion aviso) {
        var propuestaId = aviso.propuestaId();
        var solicitudId = propuestaId == null ? aviso.solicitudId()
                : referencias.solicitudPropia(propuestaId, aviso.destinatarioId()).orElse(null);
        return AvisoBandejaResult.desde(aviso, solicitudId);
    }

    private static EstadoLectura estadoLectura(String valor) {
        if (valor == null || valor.isBlank()) {
            return EstadoLectura.NO_LEIDA;
        }
        try {
            return EstadoLectura.valueOf(valor.trim());
        } catch (IllegalArgumentException ex) {
            throw new BusinessException.BadRequest("ESTADO_LECTURA_INVALIDO",
                    "Estado de lectura no reconocido.");
        }
    }

    private static TipoNotificacion tipo(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        try {
            return TipoNotificacion.valueOf(valor.trim());
        } catch (IllegalArgumentException ex) {
            throw new BusinessException.BadRequest("TIPO_INVALIDO",
                    "Tipo de notificación no reconocido.");
        }
    }

    private static int limite(Integer valor) {
        if (valor == null) {
            return LIMITE_PREDETERMINADO;
        }
        if (valor < 1 || valor > LIMITE_MAXIMO) {
            throw new BusinessException.BadRequest("LIMITE_INVALIDO", "Límite fuera de rango.");
        }
        return valor;
    }

    private static BusinessException datosInvalidos() {
        return new BusinessException.BadRequest("DATOS_INVALIDOS", "Datos inválidos.");
    }
}
