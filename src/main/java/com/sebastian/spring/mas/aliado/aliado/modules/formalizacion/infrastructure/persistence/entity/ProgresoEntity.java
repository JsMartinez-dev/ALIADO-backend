package com.sebastian.spring.mas.aliado.aliado.modules.formalizacion.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "progreso_formalizacion")
public class ProgresoEntity {
    @Id
    private UUID id;
    @Column(name = "prestador_id", nullable = false)
    private UUID prestadorId;
    @Column(name = "ruta_id", nullable = false)
    private UUID rutaId;
    @Column(name = "valor_orientativo", nullable = false)
    @JdbcTypeCode(SqlTypes.SMALLINT)
    private int valorOrientativo;
    @Column(name = "fecha_actualizacion", nullable = false)
    private OffsetDateTime fechaActualizacion;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getPrestadorId() { return prestadorId; }
    public void setPrestadorId(UUID prestadorId) { this.prestadorId = prestadorId; }
    public UUID getRutaId() { return rutaId; }
    public void setRutaId(UUID rutaId) { this.rutaId = rutaId; }
    public int getValorOrientativo() { return valorOrientativo; }
    public void setValorOrientativo(int valorOrientativo) { this.valorOrientativo = valorOrientativo; }
    public OffsetDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(OffsetDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }
}
