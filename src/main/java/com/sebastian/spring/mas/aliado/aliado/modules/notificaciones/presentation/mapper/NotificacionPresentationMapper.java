package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.presentation.mapper;

import com.sebastian.spring.mas.aliado.aliado.common.exception.BusinessException;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto.NotificacionOportunidadResult;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto.OportunidadDesdeAvisoResult;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto.RegistrarDispositivoPushCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto.ResolverOportunidadAvisoCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto.RetirarDispositivoPushCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto.RevocarVinculoPushDiferidoCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.presentation.dto.NotificacionDtos;
import java.util.UUID;
import java.util.regex.Pattern;

/** Conversión entre contratos HTTP y objetos de aplicación de notificaciones y push. */
public final class NotificacionPresentationMapper {
    /** Secreto de instalación o credencial de revocación: 32 bytes en base64url sin padding. */
    private static final Pattern SECRETO_BASE64URL = Pattern.compile("^[A-Za-z0-9_-]{43}$");
    private static final int LONGITUD_MAXIMA_TOKEN = 4096;

    private NotificacionPresentationMapper() {}

    public static NotificacionDtos.NotificacionResponse aResponse(NotificacionOportunidadResult result) {
        return new NotificacionDtos.NotificacionResponse(result.id(), "NUEVA_OPORTUNIDAD", null,
                result.solicitudId(), result.detalle(), result.estadoEntrega(), result.fechaGeneracion());
    }

    public static NotificacionDtos.NotificacionResponse aResponse(
            com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto.NotificacionResult result) {
        return new NotificacionDtos.NotificacionResponse(result.id(), result.tipo(), result.servicioId(),
                result.solicitudId(), result.detalle(), result.estadoEntrega(), result.fechaGeneracion());
    }

    /** GET /api/v1/notificaciones/{notificacionId}/oportunidad: solo cuenta y aviso (F09-PUSH-10). */
    public static ResolverOportunidadAvisoCommand aResolverOportunidadCommand(UUID notificacionId,
                                                                              UUID prestadorId) {
        if (notificacionId == null || prestadorId == null) {
            throw datosInvalidos();
        }
        return new ResolverOportunidadAvisoCommand(notificacionId, prestadorId);
    }

    public static NotificacionDtos.OportunidadDesdeAvisoResponse aResponse(OportunidadDesdeAvisoResult result) {
        return new NotificacionDtos.OportunidadDesdeAvisoResponse(result.solicitudId(),
                result.estado().name(), result.puedeCotizar());
    }

    public static RegistrarDispositivoPushCommand aCommand(UUID instalacionId, UUID usuarioId,
                                                           String pruebaInstalacion,
                                                           NotificacionDtos.DispositivoPushRequest request) {
        exigirInstalacion(instalacionId);
        exigirSecreto(pruebaInstalacion);
        if (request == null || request.plataforma() == null || request.permisoConcedido() == null) {
            throw datosInvalidos();
        }
        if (!"ANDROID".equals(request.plataforma())) {
            throw datosInvalidos();
        }
        if (request.permisoConcedido()) {
            if (request.token() == null || request.token().isBlank()
                    || request.token().length() > LONGITUD_MAXIMA_TOKEN) {
                throw datosInvalidos();
            }
            exigirSecreto(request.credencialRevocacion());
        } else if (request.token() != null && request.token().length() > LONGITUD_MAXIMA_TOKEN) {
            throw datosInvalidos();
        } else if (request.credencialRevocacion() != null && !request.credencialRevocacion().isBlank()) {
            exigirSecreto(request.credencialRevocacion());
        }
        return new RegistrarDispositivoPushCommand(instalacionId, usuarioId, pruebaInstalacion,
                request.plataforma(), request.permisoConcedido(), request.token(),
                vacioComoNulo(request.credencialRevocacion()));
    }

    public static RetirarDispositivoPushCommand aCommand(UUID instalacionId, UUID usuarioId,
                                                         String pruebaInstalacion, String credencialRevocacion) {
        exigirInstalacion(instalacionId);
        exigirSecreto(pruebaInstalacion);
        exigirSecreto(credencialRevocacion);
        return new RetirarDispositivoPushCommand(instalacionId, usuarioId, pruebaInstalacion,
                credencialRevocacion);
    }

    public static RevocarVinculoPushDiferidoCommand aCommand(UUID instalacionId, String credencialRevocacion) {
        exigirInstalacion(instalacionId);
        exigirSecreto(credencialRevocacion);
        return new RevocarVinculoPushDiferidoCommand(instalacionId, credencialRevocacion);
    }

    private static void exigirInstalacion(UUID instalacionId) {
        if (instalacionId == null) {
            throw datosInvalidos();
        }
    }

    private static void exigirSecreto(String secreto) {
        if (secreto == null || !SECRETO_BASE64URL.matcher(secreto).matches()) {
            throw datosInvalidos();
        }
    }

    private static String vacioComoNulo(String valor) {
        return valor == null || valor.isBlank() ? null : valor;
    }

    private static BusinessException datosInvalidos() {
        return new BusinessException.BadRequest("DATOS_INVALIDOS", "Datos inválidos.");
    }
}
