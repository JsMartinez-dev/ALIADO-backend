package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones;

import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.port.NotificacionPort;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.Canal;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.EstadoEntrega;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.Notificacion;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.TipoNotificacion;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.repository.NotificacionRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * F09-PUSH-11 (regresión): la entrega del aviso registrado se dispara al confirmar la
 * transacción de negocio y su resultado queda persistido en la bandeja.
 *
 * <p>Recorre el camino real evento + transacción. El listener corre en {@code AFTER_COMMIT},
 * cuando los recursos de la transacción confirmada siguen enlazados al hilo: si el caso de
 * uso no abre una transacción propia ({@code REQUIRES_NEW}), la actualización del aviso
 * participa en la transacción ya confirmada y se descarta sin commit. Los dobles de
 * {@link EntregaAvisoBandejaTest} invocan el servicio sin transacciones y no detectan ese
 * fallo, por eso esta prueba usa el contexto Spring completo.</p>
 */
@Timeout(60)
@SpringBootTest
class EntregaAvisoPostCommitTest {

    @Autowired private NotificacionPort notificacionesPort;
    @Autowired private NotificacionRepository notificaciones;
    @Autowired private PlatformTransactionManager transactionManager;

    @Test
    void elAvisoEntregadoTrasElCommitQuedaRegistradoEnLaBandeja() {
        UUID destinatario = UUID.randomUUID();
        UUID aggregate = UUID.randomUUID();

        // Transacción de negocio real: el aviso se registra y el evento se publica dentro;
        // el listener de AFTER_COMMIT solo debe ejecutarse al confirmar.
        new TransactionTemplate(transactionManager).executeWithoutResult(status ->
                notificacionesPort.registrar(TipoNotificacion.PROPUESTA_RECIBIDA, aggregate,
                        destinatario, 1, "detalle", Canal.PUSH));

        List<Notificacion> bandeja = notificaciones.porDestinatario(destinatario);
        assertEquals(1, bandeja.size(), "el aviso debe conservarse en la bandeja");

        Notificacion aviso = bandeja.get(0);
        assertEquals(EstadoEntrega.ENTREGADA, aviso.estadoEntrega(),
                "la entrega posterior al commit debe quedar persistida, no descartarse");
        assertEquals(1, aviso.intentos());
        assertNotNull(aviso.fechaEntrega());
    }
}
