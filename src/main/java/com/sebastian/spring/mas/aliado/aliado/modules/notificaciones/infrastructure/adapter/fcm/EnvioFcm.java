package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.infrastructure.adapter.fcm;

/**
 * Transporte hacia el proveedor push (F09-PUSH-09).
 *
 * <p>Devuelve el identificador que asigna FCM o lanza {@link ExcepcionEnvioFcm} con los
 * códigos del proveedor. La política de reintentos no vive aquí: el adaptador solo
 * clasifica el resultado y el caso de uso de la aplicación decide el siguiente plazo.</p>
 */
@FunctionalInterface
public interface EnvioFcm {

    String enviar(MensajeFcm mensaje) throws ExcepcionEnvioFcm;
}
