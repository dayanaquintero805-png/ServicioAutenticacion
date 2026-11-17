package com.example.servicioautenticacion;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface TokenRecuperacionRepository
        extends JpaRepository<TokenRecuperacion, Long> {

    Optional<TokenRecuperacion> findByToken(String token);

    List<TokenRecuperacion> findAllByUsuarioAndUtilizadoFalse(
            Usuario usuario
    );
}