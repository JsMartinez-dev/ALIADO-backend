package com.sebastian.spring.mas.aliado.aliado.modules.perfiles.infrastructure.persistence.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "perfiles_profesionales")
public class PerfilProfesionalEntity {
    @Id
    private UUID id;
    @Column(name = "prestador_id", nullable = false)
    private UUID prestadorId;
    @Column(name = "datos_generales")
    @JdbcTypeCode(SqlTypes.JSON)
    private String datosGenerales;
    @Column(name = "estado_exposicion_publica", nullable = false)
    private String estadoExposicionPublica;
    @Column(name = "fecha_creacion", nullable = false)
    private OffsetDateTime fechaCreacion;
    @Column(name = "fecha_actualizacion", nullable = false)
    private OffsetDateTime fechaActualizacion;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "perfil_categoria", joinColumns = @JoinColumn(name = "perfil_id"))
    @Column(name = "categoria_id")
    private Set<UUID> categoriaIds = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "perfil_zona_atencion", joinColumns = @JoinColumn(name = "perfil_id"))
    @Column(name = "zona_id")
    private Set<UUID> zonaIds = new HashSet<>();

    @OneToMany(mappedBy = "perfil", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ExperienciaEntity> experiencias = new ArrayList<>();

    @OneToMany(mappedBy = "perfil", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ServicioPortafolioEntity> portafolio = new ArrayList<>();

    @OneToMany(mappedBy = "perfil", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<DisponibilidadEntity> disponibilidades = new ArrayList<>();

    @OneToMany(mappedBy = "perfil", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<CompromisoAgendaEntity> compromisos = new ArrayList<>();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getPrestadorId() { return prestadorId; }
    public void setPrestadorId(UUID prestadorId) { this.prestadorId = prestadorId; }
    public String getDatosGenerales() { return datosGenerales; }
    public void setDatosGenerales(String datosGenerales) { this.datosGenerales = datosGenerales; }
    public String getEstadoExposicionPublica() { return estadoExposicionPublica; }
    public void setEstadoExposicionPublica(String estadoExposicionPublica) { this.estadoExposicionPublica = estadoExposicionPublica; }
    public OffsetDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(OffsetDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public OffsetDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(OffsetDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }
    public Set<UUID> getCategoriaIds() { return categoriaIds; }
    public void setCategoriaIds(Set<UUID> categoriaIds) { this.categoriaIds = categoriaIds; }
    public Set<UUID> getZonaIds() { return zonaIds; }
    public void setZonaIds(Set<UUID> zonaIds) { this.zonaIds = zonaIds; }
    public List<ExperienciaEntity> getExperiencias() { return experiencias; }
    public void setExperiencias(List<ExperienciaEntity> experiencias) { this.experiencias = experiencias; }
    public List<ServicioPortafolioEntity> getPortafolio() { return portafolio; }
    public void setPortafolio(List<ServicioPortafolioEntity> portafolio) { this.portafolio = portafolio; }
    public List<DisponibilidadEntity> getDisponibilidades() { return disponibilidades; }
    public void setDisponibilidades(List<DisponibilidadEntity> disponibilidades) { this.disponibilidades = disponibilidades; }
    public List<CompromisoAgendaEntity> getCompromisos() { return compromisos; }
    public void setCompromisos(List<CompromisoAgendaEntity> compromisos) { this.compromisos = compromisos; }
}
