package com.example.servicioautenticacion.controller;

import com.example.servicioautenticacion.RegistroEmocional;
import com.example.servicioautenticacion.RegistroEmocionalRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/registros")
public class RegistroEmocionalController {

    private final RegistroEmocionalRepository registroRepository;

    public RegistroEmocionalController(
            RegistroEmocionalRepository registroRepository) {
        this.registroRepository = registroRepository;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> guardarRegistro(
            @RequestBody RegistroEmocional registro) {

        if (registro.getNombreUsuario() == null
                || registro.getNombreUsuario().isBlank()
                || registro.getEstadoAnimo() == null
                || registro.getEstadoAnimo().isBlank()
                || registro.getFecha() == null) {

            return ResponseEntity.badRequest().body(
                    Map.of("mensaje",
                            "El usuario, la fecha y el estado de ánimo son obligatorios."));
        }

        if (registro.getNombreUsuario().trim().length() > 100
                || registro.getEstadoAnimo().trim().length() > 50
                || (registro.getComentario() != null
                && registro.getComentario().length() > 250)) {

            return ResponseEntity.badRequest().body(
                    Map.of("mensaje",
                            "Revisa la longitud de los datos ingresados."));
        }

        String estado = registro.getEstadoAnimo().trim();

        if (!List.of("Feliz", "Tranquila", "Normal", "Triste")
                .contains(estado)) {
            return ResponseEntity.badRequest().body(
                    Map.of("mensaje", "El estado de ánimo no es válido."));
        }

        registro.setNombreUsuario(registro.getNombreUsuario().trim());
        registro.setEstadoAnimo(estado);

        if (registro.getComentario() != null) {
            registro.setComentario(registro.getComentario().trim());
        }

        registroRepository.save(registro);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                Map.of("mensaje", "Registro emocional guardado correctamente."));
    }

    @GetMapping("/{nombreUsuario}")
    public ResponseEntity<List<RegistroEmocional>> consultarRegistros(
            @PathVariable String nombreUsuario) {

        if (nombreUsuario == null || nombreUsuario.isBlank()
                || nombreUsuario.length() > 100) {
            return ResponseEntity.badRequest().build();
        }

        List<RegistroEmocional> registros =
                registroRepository.findByNombreUsuarioOrderByIdDesc(
                        nombreUsuario.trim());

        return ResponseEntity.ok(registros);
    }
}
