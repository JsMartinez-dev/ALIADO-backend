package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.cursor;

import com.sebastian.spring.mas.aliado.aliado.common.exception.BusinessException;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.EstadoLectura;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.TipoNotificacion;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.UUID;

/**
 * Cursor opaco de la bandeja (F09-BANDEJA-03).
 *
 * <p>Representa el último par {@code (fechaGeneracion, id)} devuelto y se codifica en
 * Base64URL para que el cliente no dependa de su estructura. La representación conserva la
 * precisión de la fecha mediante segundos y nanosegundos de la época, y el UUID completo.
 * El token incluye la versión del formato y los filtros activos de la consulta; un cursor
 * de otros filtros se rechaza con el mismo error controlado que un formato inválido.</p>
 *
 * <p>El destinatario no forma parte del cursor: la consulta lo toma siempre de la sesión,
 * de modo que manipular o reutilizar un cursor nunca permite leer avisos de otra cuenta.</p>
 */
public record CursorBandeja(OffsetDateTime fechaGeneracion, UUID id) {

    private static final String VERSION = "v1";
    private static final int LONGITUD_MAXIMA = 512;

    public static String codificar(OffsetDateTime fechaGeneracion, UUID id,
                                   EstadoLectura estadoLectura, TipoNotificacion tipo) {
        String plano = String.join("|",
                VERSION,
                Long.toString(fechaGeneracion.toEpochSecond()),
                Integer.toString(fechaGeneracion.getNano()),
                id.toString(),
                filtros(estadoLectura, tipo));
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(plano.getBytes(StandardCharsets.UTF_8));
    }

    public static CursorBandeja decodificar(String cursor, EstadoLectura estadoLectura,
                                            TipoNotificacion tipo) {
        if (cursor == null || cursor.isBlank() || cursor.length() > LONGITUD_MAXIMA) {
            throw cursorInvalido();
        }
        try {
            String plano = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
            String[] partes = plano.split("\\|", -1);
            if (partes.length != 5 || !VERSION.equals(partes[0])
                    || !filtros(estadoLectura, tipo).equals(partes[4])) {
                throw cursorInvalido();
            }
            long segundos = Long.parseLong(partes[1]);
            int nanos = Integer.parseInt(partes[2]);
            if (nanos < 0 || nanos > 999_999_999) {
                throw cursorInvalido();
            }
            UUID id = UUID.fromString(partes[3]);
            return new CursorBandeja(
                    OffsetDateTime.ofInstant(Instant.ofEpochSecond(segundos, nanos), ZoneOffset.UTC), id);
        } catch (IllegalArgumentException | java.time.DateTimeException ex) {
            // Base64, números, UUID o fecha fuera de rango comparten el mismo rechazo controlado.
            throw cursorInvalido();
        }
    }

    private static String filtros(EstadoLectura estadoLectura, TipoNotificacion tipo) {
        return estadoLectura.name() + ":" + (tipo == null ? "" : tipo.name());
    }

    private static BusinessException cursorInvalido() {
        return new BusinessException.BadRequest("CURSOR_INVALIDO", "Cursor inválido.");
    }
}
