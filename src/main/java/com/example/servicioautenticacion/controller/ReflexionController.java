package com.example.servicioautenticacion;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reflexiones")
public class ReflexionController {

    private final ReflexionRepository reflexionRepository;

    public ReflexionController(ReflexionRepository reflexionRepository) {
        this.reflexionRepository = reflexionRepository;
    }

    @PostMapping
public ResponseEntity<?> guardarReflexion(
        @RequestBody Reflexion reflexion,
        jakarta.servlet.http.HttpSession sesion) {
    String usuarioAutenticado =
        (String) sesion.getAttribute("usuarioAutenticado");

if (usuarioAutenticado == null) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
            Map.of("mensaje", "Debes iniciar sesión para guardar una reflexión.")
    );
}

reflexion.setNombreUsuario(usuarioAutenticado);
        if (reflexion.getNombreUsuario() == null
                || reflexion.getNombreUsuario().isBlank()
                || reflexion.getContenido() == null
                || reflexion.getContenido().isBlank()) {
            return ResponseEntity.badRequest().body(
                    Map.of("mensaje", "Debes escribir una reflexión.")
            );
        }

        if (reflexion.getContenido().trim().length() > 250) {
            return ResponseEntity.badRequest().body(
                    Map.of("mensaje", "La reflexión no puede superar los 250 caracteres.")
            );
        }

        reflexion.setNombreUsuario(reflexion.getNombreUsuario().trim());
        reflexion.setContenido(reflexion.getContenido().trim());
        reflexion.setFecha(LocalDateTime.now());

        Reflexion guardada = reflexionRepository.save(reflexion);

        return ResponseEntity.status(HttpStatus.CREATED).body(guardada);
    }

    @GetMapping("/{nombreUsuario}")
public ResponseEntity<List<Reflexion>> listarReflexiones(
        @PathVariable String nombreUsuario,
        jakarta.servlet.http.HttpSession sesion) { 
    String usuarioAutenticado =
        (String) sesion.getAttribute("usuarioAutenticado");

if (usuarioAutenticado == null) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
}

if (!usuarioAutenticado.equals(nombreUsuario)) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    
    }

return ResponseEntity.ok(
        reflexionRepository
                .findByNombreUsuarioOrderByFechaDesc(usuarioAutenticado)
    );
       }
    }
    
        