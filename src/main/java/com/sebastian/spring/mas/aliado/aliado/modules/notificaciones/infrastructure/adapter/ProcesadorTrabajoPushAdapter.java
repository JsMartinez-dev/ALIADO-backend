package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.infrastructure.adapter;

import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto.EnvioPushPendiente;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto.IniciarEntregaPushCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto.PrepararEntregasPushCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto.RegistrarResultadoEntregaPushCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.port.EntregaPushPort;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.port.ProcesadorTrabajoPushPort;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.usecase.IniciarEntregaPushUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.usecase.PrepararEntregasPushUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.usecase.RegistrarResultadoEntregaPushUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.ReclamacionPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.ResultadoEnvioPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.TipoReclamacionPush;
import java.util.Objects;
import java.util.Optional;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Procesador de una reclamación del worker push (F09-PUSH-08).
 *
 * <p>Cada reclamación se procesa después del commit de {@code ReclamacionTrabajoPushAdapter}.
 * La preparación corre en su propia transacción. El envío se divide en dos transacciones
 * cortas: la primera revalida y devuelve el mensaje; la llamada al proveedor ocurre entre
 * ambas, fuera de cualquier transacción; la segunda guarda el resultado condicionado a la
 * reclamación vigente y cierra el padre si ya no quedan entregas pendientes.</p>
 *
 * <p>Si el proveedor lanza una excepción, la reclamación permanece persistida y otra
 * instancia (o la misma) la recupera al vencer su plazo. No se abre una transacción
 * alrededor de la llamada externa. Si la revalidación suprime la entrega o la reclamación
 * no deja tiempo para enviar y guardar el resultado (D-08), la fase de inicio devuelve
 * vacío, libera la reclamación y el procesador no invoca al proveedor.</p>
 */
public class ProcesadorTrabajoPushAdapter implements ProcesadorTrabajoPushPort {

    private final PrepararEntregasPushUseCase preparar;
    private final IniciarEntregaPushUseCase iniciar;
    private final RegistrarResultadoEntregaPushUseCase registrar;
    private final EntregaPushPort entrega;
    private final TransactionTemplate transaccion;

    public ProcesadorTrabajoPushAdapter(PrepararEntregasPushUseCase preparar,
                                        IniciarEntregaPushUseCase iniciar,
                                        RegistrarResultadoEntregaPushUseCase registrar,
                                        EntregaPushPort entrega,
                                        TransactionTemplate transaccion) {
        this.preparar = Objects.requireNonNull(preparar);
        this.iniciar = Objects.requireNonNull(iniciar);
        this.registrar = Objects.requireNonNull(registrar);
        this.entrega = Objects.requireNonNull(entrega);
        this.transaccion = Objects.requireNonNull(transaccion);
    }

    @Override
    public void procesar(ReclamacionPush reclamacion) {
        if (reclamacion.tipo() == TipoReclamacionPush.PREPARACION) {
            transaccion.executeWithoutResult(estado ->
                    preparar.ejecutar(new PrepararEntregasPushCommand(reclamacion)));
            return;
        }

        Optional<EnvioPushPendiente> preparado = transaccion.execute(estado ->
                iniciar.ejecutar(new IniciarEntregaPushCommand(reclamacion)));
        if (preparado == null || preparado.isEmpty()) {
            return;
        }
        EnvioPushPendiente envio = preparado.get();

        // La llamada externa ocurre fuera de la transacción de reclamación y de la de
        // resultado. Un fallo aquí deja la reclamación persistida para su recuperación.
        ResultadoEnvioPush resultado = entrega.enviar(envio.mensaje());

        transaccion.executeWithoutResult(estado -> registrar.ejecutar(
                new RegistrarResultadoEntregaPushCommand(envio, resultado)));
    }
}
