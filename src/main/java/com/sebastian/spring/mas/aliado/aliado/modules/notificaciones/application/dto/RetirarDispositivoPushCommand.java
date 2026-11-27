package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto;

import java.util.UUID;

/** Baja en línea de la instalación de la cuenta autenticada (F09-PUSH-01/D-08). */
public record RetirarDispositivoPushCommand(UUID instalacionId, UUID usuarioId, String pruebaInstalacion,
                                            String credencialRevocacion) {}
