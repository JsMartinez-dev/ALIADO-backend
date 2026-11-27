package com.sebastian.spring.mas.aliado.aliado.modules.propuestas.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "propuestas")
public class PropuestaEntity {
    @Id
    private UUID id;
    @Column(name = "solicitud_id", nullable = false)
    private UUID solicitudId;
    @Column(name = "prestador_id", nullable = false)
    private UUID prestadorId;
    @Column(nullable = false)
    private String disponibilidad;
    @Column(length = 1000)
    private String mensaje;
    @Column(name = "tarifa_cop")
    private Integer tarifaCop;
    @Column(nullable = false)
    private String estado;
    @Column(name = "fecha_creacion", nullable = false)
    private OffsetDateTime fechaCreacion;
    @Column(name = "fecha_actualizacion", nullable = false)
    private OffsetDateTime fechaActualizacion;
    @Version
    private int version;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getSolicitudId() { return solicitudId; }
    public void setSolicitudId(UUID solicitudId) { this.solicitudId = solicitudId; }
    public UUID getPrestadorId() { return prestadorId; }
    public void setPrestadorId(UUID prestadorId) { this.prestadorId = prestadorId; }
    public String getDisponibilidad() { return disponibilidad; }
    public void setDisponibilidad(String disponibilidad) { this.disponibilidad = disponibilidad; }
    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
    public Integer getTarifaCop() { return tarifaCop; }
    public void setTarifaCop(Integer tarifaCop) { this.tarifaCop = tarifaCop; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public OffsetDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(OffsetDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public OffsetDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(OffsetDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }
    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = version; }
}
