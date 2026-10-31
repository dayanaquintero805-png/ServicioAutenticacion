package com.example.servicioautenticacion;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RegistroEmocionalRepository
        extends JpaRepository<RegistroEmocional, Long> {

    List<RegistroEmocional> findByNombreUsuarioOrderByIdDesc(
            String nombreUsuario);
}
