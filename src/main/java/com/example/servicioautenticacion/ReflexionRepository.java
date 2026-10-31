package com.example.servicioautenticacion;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReflexionRepository extends JpaRepository<Reflexion, Long> {

    List<Reflexion> findByNombreUsuarioOrderByFechaDesc(String nombreUsuario);
}
