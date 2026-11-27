package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "notificaciones")
public class NotificacionEntity {
    @Id
    private UUID id;
    @Column(name = "destinatario_id", nullable = false)
    private UUID destinatarioId;
    @Column(nullable = false)
    private String tipo;
    @Column(name = "aggregate_id", nullable = false)
    private UUID aggregateId;
    @Column(name = "event_version", nullable = false)
    private int eventVersion;
    private String detalle;
    @Column(nullable = false)
    private String canal;
    @Column(name = "estado_entrega", nullable = false)
    private String estadoEntrega;
    @Column(nullable = false)
    private int intentos;
    @Column(name = "fecha_generacion", nullable = false)
    private OffsetDateTime fechaGeneracion;
    @Column(name = "fecha_entrega")
    private OffsetDateTime fechaEntrega;
    /** Primera lectura del aviso; {@code null} mientras esté pendiente. */
    @Column(name = "leida_en")
    private OffsetDateTime leidaEn;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getDestinatarioId() { return destinatarioId; }
    public void setDestinatarioId(UUID destinatarioId) { this.destinatarioId = destinatarioId; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public UUID getAggregateId() { return aggregateId; }
    public void setAggregateId(UUID aggregateId) { this.aggregateId = aggregateId; }
    public int getEventVersion() { return eventVersion; }
    public void setEventVersion(int eventVersion) { this.eventVersion = eventVersion; }
    public String getDetalle() { return detalle; }
    public void setDetalle(String detalle) { this.detalle = detalle; }
    public String getCanal() { return canal; }
    public void setCanal(String canal) { this.canal = canal; }
    public String getEstadoEntrega() { return estadoEntrega; }
    public void setEstadoEntrega(String estadoEntrega) { this.estadoEntrega = estadoEntrega; }
    public int getIntentos() { return intentos; }
    public void setIntentos(int intentos) { this.intentos = intentos; }
    public OffsetDateTime getFechaGeneracion() { return fechaGeneracion; }
    public void setFechaGeneracion(OffsetDateTime fechaGeneracion) { this.fechaGeneracion = fechaGeneracion; }
    public OffsetDateTime getFechaEntrega() { return fechaEntrega; }
    public void setFechaEntrega(OffsetDateTime fechaEntrega) { this.fechaEntrega = fechaEntrega; }
    public OffsetDateTime getLeidaEn() { return leidaEn; }
    public void setLeidaEn(OffsetDateTime leidaEn) { this.leidaEn = leidaEn; }
}
