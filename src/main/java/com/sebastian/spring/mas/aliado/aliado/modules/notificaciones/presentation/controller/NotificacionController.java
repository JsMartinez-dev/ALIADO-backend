package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.presentation.controller;

import com.sebastian.spring.mas.aliado.aliado.common.web.SecurityUtils;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.usecase.ConsultarBandejaUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.usecase.ConsultarNotificacionesOportunidadesUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.usecase.ConsultarNotificacionesUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.usecase.ConsultarResumenNoLeidasUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.usecase.MarcarAvisoLeidoUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.usecase.MarcarTodasLeidasUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.usecase.RegistrarDispositivoPushUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.usecase.ResolverOportunidadAvisoUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.usecase.RetirarDispositivoPushUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.usecase.RevocarVinculoPushDiferidoUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.presentation.mapper.NotificacionPresentationMapper;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.presentation.dto.NotificacionDtos;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Bandeja de notificaciones propias (SPEC-09) y dispositivos push Android (F09-PUSH-01). */
@RestController
@RequestMapping("/api/v1/notificaciones")
public class NotificacionController {
    private static final String PRUEBA_INSTALACION = "X-ALIADO-Installation-Proof";
    private static final String CREDENCIAL_REVOCACION = "X-ALIADO-Push-Revocation";

    private final ConsultarNotificacionesUseCase notificaciones;
    private final ConsultarNotificacionesOportunidadesUseCase oportunidadesUseCase;
    private final ConsultarBandejaUseCase consultarBandeja;
    private final ConsultarResumenNoLeidasUseCase consultarResumenNoLeidas;
    private final MarcarAvisoLeidoUseCase marcarAvisoLeido;
    private final MarcarTodasLeidasUseCase marcarTodasLeidas;
    private final RegistrarDispositivoPushUseCase registrarDispositivo;
    private final RetirarDispositivoPushUseCase retirarDispositivo;
    private final RevocarVinculoPushDiferidoUseCase revocarVinculoDiferido;
    private final ResolverOportunidadAvisoUseCase resolverOportunidadAviso;

    public NotificacionController(ConsultarNotificacionesUseCase notificaciones,
                                  ConsultarNotificacionesOportunidadesUseCase oportunidadesUseCase,
                                  ConsultarBandejaUseCase consultarBandeja,
                                  ConsultarResumenNoLeidasUseCase consultarResumenNoLeidas,
                                  MarcarAvisoLeidoUseCase marcarAvisoLeido,
                                  MarcarTodasLeidasUseCase marcarTodasLeidas,
                                  RegistrarDispositivoPushUseCase registrarDispositivo,
                                  RetirarDispositivoPushUseCase retirarDispositivo,
                                  RevocarVinculoPushDiferidoUseCase revocarVinculoDiferido,
                                  ResolverOportunidadAvisoUseCase resolverOportunidadAviso) {
        this.notificaciones = notificaciones;
        this.oportunidadesUseCase = oportunidadesUseCase;
        this.consultarBandeja = consultarBandeja;
        this.consultarResumenNoLeidas = consultarResumenNoLeidas;
        this.marcarAvisoLeido = marcarAvisoLeido;
        this.marcarTodasLeidas = marcarTodasLeidas;
        this.registrarDispositivo = registrarDispositivo;
        this.retirarDispositivo = retirarDispositivo;
        this.revocarVinculoDiferido = revocarVinculoDiferido;
        this.resolverOportunidadAviso = resolverOportunidadAviso;
    }

    @GetMapping
    public ResponseEntity<List<NotificacionDtos.NotificacionResponse>> listar(
            @RequestParam(required = false) String tipo) {
        var command = new com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto.ConsultarNotificacionesCommand(SecurityUtils.actual().id(), tipo);
        return ResponseEntity.ok(notificaciones.ejecutar(command).stream()
                .map(NotificacionPresentationMapper::aResponse).toList());
    }

    @GetMapping("/oportunidades")
    public ResponseEntity<List<NotificacionDtos.NotificacionResponse>> notificacionesOportunidades() {
        SecurityUtils.exigirRol("PRESTADOR");
        return ResponseEntity.ok(oportunidadesUseCase.ejecutar(SecurityUtils.actual().id()).stream()
                .map(NotificacionPresentationMapper::aResponse).toList());
    }

    /**
     * Página de la bandeja propia (F09-BANDEJA-04). El destinatario procede siempre de la
     * sesión; los filtros, el límite y el cursor los valida el caso de uso y un valor
     * inválido responde 400.
     */
    @Operation(summary = "Consultar una página de la bandeja propia",
            description = "Devuelve `items` y `cursorSiguiente`, ordenados por fecha de generación y aviso "
                    + "de más reciente a más antiguo. Por defecto solo incluye los avisos no leídos, "
                    + "con 20 elementos por página. Un `cursorSiguiente` nulo indica que no quedan "
                    + "páginas. El destinatario se toma de la sesión autenticada.")
    @ApiResponse(responseCode = "200", description = "Página de avisos propios")
    @ApiResponse(responseCode = "400", description = "Filtro, límite o cursor inválido")
    @ApiResponse(responseCode = "401", description = "Sin sesión")
    @GetMapping("/bandeja")
    public ResponseEntity<NotificacionDtos.BandejaResponse> bandeja(
            @Parameter(description = "Filtro de lectura: NO_LEIDA (por defecto), LEIDA o TODAS.")
            @RequestParam(required = false) String estadoLectura,
            @Parameter(description = "Tipo de aviso; omitido no filtra por tipo.")
            @RequestParam(required = false) String tipo,
            @Parameter(description = "Elementos por página: 20 por defecto, de 1 a 100.")
            @RequestParam(required = false) Integer limite,
            @Parameter(description = "cursorSiguiente de la página anterior; omitido inicia la paginación.")
            @RequestParam(required = false) String cursor) {
        return ResponseEntity.ok(NotificacionPresentationMapper.aResponse(consultarBandeja.ejecutar(
                NotificacionPresentationMapper.aConsultarBandejaCommand(SecurityUtils.actual().id(),
                        estadoLectura, tipo, limite, cursor))));
    }

    /** Indicador de la campana sin cargar la bandeja (F09-BANDEJA-04). */
    @Operation(summary = "Consultar si quedan avisos sin leer",
            description = "Devuelve `hayNoLeidas` para la cuenta de la sesión sin cargar la bandeja.")
    @ApiResponse(responseCode = "200", description = "Indicador de la campana")
    @ApiResponse(responseCode = "401", description = "Sin sesión")
    @GetMapping("/resumen")
    public ResponseEntity<NotificacionDtos.ResumenNoLeidasResponse> resumen() {
        return ResponseEntity.ok(NotificacionPresentationMapper.aResponse(
                consultarResumenNoLeidas.ejecutar(SecurityUtils.actual().id())));
    }

    /**
     * Lectura individual de un aviso propio (F09-BANDEJA-04). Sin cuerpo; responde 204 aunque
     * ya estuviera leído y conserva la primera fecha. Un aviso ajeno o inexistente comparte el
     * mismo 404.
     */
    @Operation(summary = "Marcar un aviso propio como leído",
            description = "Sin cuerpo. Responde 204 aunque el aviso ya estuviera leído y conserva la "
                    + "primera fecha de lectura. Responde 404 si el aviso no existe o pertenece a otra cuenta.")
    @ApiResponse(responseCode = "204", description = "Aviso propio leído")
    @ApiResponse(responseCode = "400", description = "Identificador con formato inválido")
    @ApiResponse(responseCode = "401", description = "Sin sesión")
    @ApiResponse(responseCode = "404", description = "Aviso inexistente o de otra cuenta")
    @PutMapping("/{notificacionId}/lectura")
    public ResponseEntity<Void> marcarLeida(@PathVariable UUID notificacionId) {
        marcarAvisoLeido.ejecutar(NotificacionPresentationMapper.aMarcarAvisoLeidoCommand(
                notificacionId, SecurityUtils.actual().id()));
        return ResponseEntity.noContent().build();
    }

    /**
     * Lectura masiva de los pendientes de la cuenta (F09-BANDEJA-04). Sin cuerpo y sin
     * distinguir filas: una cuenta sin pendientes también responde 204.
     */
    @Operation(summary = "Marcar todos los avisos pendientes como leídos",
            description = "Sin cuerpo. Actualiza de forma atómica solo los avisos pendientes de la cuenta "
                    + "de la sesión y responde 204, también cuando no había pendientes. Los avisos "
                    + "publicados después de la actualización conservan su estado.")
    @ApiResponse(responseCode = "204", description = "Avisos pendientes de la cuenta leídos")
    @ApiResponse(responseCode = "401", description = "Sin sesión")
    @PutMapping("/lectura")
    public ResponseEntity<Void> marcarTodasLeidas() {
        marcarTodasLeidas.ejecutar(NotificacionPresentationMapper.aMarcarTodasLeidasCommand(
                SecurityUtils.actual().id()));
        return ResponseEntity.noContent().build();
    }

    /** Estado permitido al destinatario del aviso; cualquier otro caso usa el error genérico. */
    @GetMapping("/{notificacionId}/oportunidad")
    public ResponseEntity<NotificacionDtos.OportunidadDesdeAvisoResponse> oportunidadDesdeAviso(
            @PathVariable UUID notificacionId) {
        SecurityUtils.exigirRol("PRESTADOR");
        return ResponseEntity.ok(NotificacionPresentationMapper.aResponse(
                resolverOportunidadAviso.ejecutar(NotificacionPresentationMapper.aResolverOportunidadCommand(
                        notificacionId, SecurityUtils.actual().id()))));
    }

    /** Alta o sincronización de la instalación Android de la cuenta autenticada. */
    @PutMapping("/dispositivos/{instalacionId}")
    public ResponseEntity<Void> registrarDispositivo(
            @PathVariable UUID instalacionId,
            @RequestHeader(value = PRUEBA_INSTALACION, required = false) String pruebaInstalacion,
            @Valid @RequestBody NotificacionDtos.DispositivoPushRequest request) {
        registrarDispositivo.ejecutar(NotificacionPresentationMapper.aCommand(instalacionId,
                SecurityUtils.actual().id(), pruebaInstalacion, request));
        return ResponseEntity.noContent().build();
    }

    /** Baja en línea al cerrar sesión; revoca solo la generación de la credencial presentada. */
    @DeleteMapping("/dispositivos/{instalacionId}")
    public ResponseEntity<Void> retirarDispositivo(
            @PathVariable UUID instalacionId,
            @RequestHeader(value = PRUEBA_INSTALACION, required = false) String pruebaInstalacion,
            @RequestHeader(value = CREDENCIAL_REVOCACION, required = false) String credencialRevocacion) {
        retirarDispositivo.ejecutar(NotificacionPresentationMapper.aCommand(instalacionId,
                SecurityUtils.actual().id(), pruebaInstalacion, credencialRevocacion));
        return ResponseEntity.noContent().build();
    }

    /** Baja diferida sin sesión, autorizada únicamente por la credencial del vínculo. */
    @PostMapping("/dispositivos/{instalacionId}/revocacion")
    public ResponseEntity<Void> revocarVinculoDiferido(
            @PathVariable UUID instalacionId,
            @RequestHeader(value = CREDENCIAL_REVOCACION, required = false) String credencialRevocacion) {
        revocarVinculoDiferido.ejecutar(NotificacionPresentationMapper.aCommand(instalacionId,
                credencialRevocacion));
        return ResponseEntity.noContent().build();
    }
}
