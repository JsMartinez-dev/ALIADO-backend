package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.service;

import com.sebastian.spring.mas.aliado.aliado.common.exception.BusinessException;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto.RegistrarDispositivoPushCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.usecase.RegistrarDispositivoPushUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.HuellaPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.InstalacionPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.VinculoPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.repository.DispositivoPushRepository;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Alta y sincronización de la instalación Android de la cuenta autenticada
 * (F09-PUSH-01/D-08).
 *
 * <p>La prueba de posesión ({@code X-ALIADO-Installation-Proof}) es el secreto de alta:
 * un UUID por sí solo nunca autoriza reasignar ni revocar. La reasignación por cambio de
 * cuenta, la rotación del token y la baja por permiso apagado se resuelven dentro de la
 * misma transacción y de forma idempotente.</p>
 */
public class RegistrarDispositivoPushService implements RegistrarDispositivoPushUseCase {
    private static final String PLATAFORMA_ANDROID = "ANDROID";
    private static final int LONGITUD_MAXIMA_TOKEN = 4096;

    private final DispositivoPushRepository repositorio;

    public RegistrarDispositivoPushService(DispositivoPushRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public void ejecutar(RegistrarDispositivoPushCommand command) {
        validar(command);
        HuellaPush prueba = HuellaPush.de(command.pruebaInstalacion());
        OffsetDateTime ahora = OffsetDateTime.now();

        repositorio.bloquearRegistro();
        repositorio.asegurarInstalacion(new InstalacionPush(command.instalacionId(),
                PLATAFORMA_ANDROID, prueba.valor(), ahora));
        InstalacionPush instalacion = repositorio.bloquearInstalacion(command.instalacionId())
                .orElseThrow(this::instalacionNoDisponible);
        if (!instalacion.coincidePrueba(prueba.valor())) {
            throw instalacionNoDisponible();
        }

        Optional<VinculoPush> activo = repositorio.vinculoActivo(command.instalacionId());
        if (!command.permisoConcedido()) {
            desactivar(command, activo, ahora);
            return;
        }
        registrar(command, activo, ahora);
    }

    private void registrar(RegistrarDispositivoPushCommand command, Optional<VinculoPush> activo,
                           OffsetDateTime ahora) {
        HuellaPush token = HuellaPush.de(command.tokenFcm());
        HuellaPush credencial = HuellaPush.de(command.credencialRevocacion());

        if (activo.isPresent()) {
            VinculoPush vigente = activo.get();
            if (vigente.esDe(command.usuarioId()) && vigente.usaToken(token.valor())) {
                // Repetir el mismo PUT conserva la generación; otra credencial no reemplaza el vínculo.
                if (vigente.coincideCredencial(credencial.valor())) {
                    return;
                }
                throw vinculoEnUso();
            }
        }

        // Cambio de cuenta o rotación: la credencial debe ser nueva y el token no puede
        // pertenecer a otra instalación activa.
        if (repositorio.existeCredencial(credencial.valor())) {
            throw vinculoEnUso();
        }
        Optional<UUID> duenioToken = repositorio.instalacionConTokenActivo(token.valor());
        if (duenioToken.isPresent() && !duenioToken.get().equals(command.instalacionId())) {
            throw tokenEnUso();
        }

        long generacion = repositorio.siguienteGeneracion(command.instalacionId());
        activo.ifPresent(vigente ->
                repositorio.revocarVinculo(command.instalacionId(), vigente.generacion(), ahora));
        repositorio.guardarVinculo(new VinculoPush(command.instalacionId(), generacion,
                command.usuarioId(), command.tokenFcm(), token.valor(), credencial.valor(), ahora, null));
    }

    /**
     * Permiso apagado: retira la asociación vigente de la propia cuenta. La credencial es
     * opcional; si se presenta y no corresponde a la generación activa, se conserva el
     * vínculo para no invalidar un enlace renovado desde una ejecución atrasada. Un vínculo
     * de otra cuenta no se altera: la baja ajena queda limitada a la prueba y a la sesión
     * de su titular.
     */
    private void desactivar(RegistrarDispositivoPushCommand command, Optional<VinculoPush> activo,
                            OffsetDateTime ahora) {
        if (activo.isEmpty()) {
            return;
        }
        VinculoPush vigente = activo.get();
        if (!vigente.esDe(command.usuarioId())) {
            return;
        }
        if (command.credencialRevocacion() != null) {
            HuellaPush credencial = HuellaPush.de(command.credencialRevocacion());
            if (!vigente.coincideCredencial(credencial.valor())) {
                throw vinculoEnUso();
            }
        }
        repositorio.revocarVinculo(command.instalacionId(), vigente.generacion(), ahora);
    }

    private void validar(RegistrarDispositivoPushCommand command) {
        if (command == null || command.instalacionId() == null || command.usuarioId() == null
                || command.pruebaInstalacion() == null || command.pruebaInstalacion().isBlank()) {
            throw datosInvalidos();
        }
        if (!PLATAFORMA_ANDROID.equals(command.plataforma())) {
            throw datosInvalidos();
        }
        if (command.permisoConcedido()) {
            String token = command.tokenFcm();
            if (token == null || token.isBlank() || token.length() > LONGITUD_MAXIMA_TOKEN) {
                throw datosInvalidos();
            }
            if (command.credencialRevocacion() == null || command.credencialRevocacion().isBlank()) {
                throw datosInvalidos();
            }
        }
    }

    private BusinessException instalacionNoDisponible() {
        return new BusinessException.NotFound("INSTALACION_NO_DISPONIBLE",
                "La instalación no está disponible con la prueba presentada.");
    }

    private BusinessException vinculoEnUso() {
        return new BusinessException.Conflict("VINCULO_PUSH_EN_USO",
                "El vínculo push no coincide con la credencial vigente.");
    }

    private BusinessException tokenEnUso() {
        return new BusinessException.Conflict("TOKEN_PUSH_EN_USO",
                "El token push ya está asociado a otra instalación.");
    }

    private BusinessException datosInvalidos() {
        return new BusinessException.BadRequest("DATOS_INVALIDOS", "Datos inválidos.");
    }
}
