package com.example.servicioautenticacion;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class ServicioContrasenas {

    private final BCryptPasswordEncoder codificador =
            new BCryptPasswordEncoder();

    public String codificar(String contrasena) {
        return codificador.encode(contrasena);
    }

    public boolean verificar(String contrasena, String hashGuardado) {
        return codificador.matches(contrasena, hashGuardado);
    }
}