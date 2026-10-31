
package com.example.servicioautenticacion;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "registros_emocionales")
public class RegistroEmocional {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_usuario", nullable = false, length = 100)
    private String nombreUsuario;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(name = "estado_animo", nullable = false, length = 50)
    private String estadoAnimo;

    @Column(length = 250)
    private String comentario;

    public RegistroEmocional() {
    }

    public RegistroEmocional(
            String nombreUsuario,
            LocalDate fecha,
            String estadoAnimo,
            String comentario) {
        this.nombreUsuario = nombreUsuario;
        this.fecha = fecha;
        this.estadoAnimo = estadoAnimo;
        this.comentario = comentario;
    }

    public Long getId() {
        return id;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public String getEstadoAnimo() {
        return estadoAnimo;
    }

    public void setEstadoAnimo(String estadoAnimo) {
        this.estadoAnimo = estadoAnimo;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }
}

