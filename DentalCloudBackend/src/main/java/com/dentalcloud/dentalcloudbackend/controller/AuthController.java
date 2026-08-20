package com.dentalcloud.dentalcloudbackend.controller;

import com.dentalcloud.dentalcloudbackend.domain.dto.AccountActivationRequestDTO;
import com.dentalcloud.dentalcloudbackend.domain.dto.AuthResponseDTO;
import com.dentalcloud.dentalcloudbackend.domain.dto.AuthRequestDTO;
import com.dentalcloud.dentalcloudbackend.domain.dto.RegisterPatientRequestDTO;
import com.dentalcloud.dentalcloudbackend.domain.dto.UserResponseDTO;
import com.dentalcloud.dentalcloudbackend.services.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


/*
 * Controlador REST para la autenticación de usuarios.
 * Proporciona endpoints para el registro, inicio de sesión y validación de tokens.
 */
@Slf4j
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"})
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /*
     * Endpoint para el registro de nuevos usuarios.
     * Recibe un objeto RegisterRequest en el cuerpo de la solicitud.
     * Retorna un mensaje de éxito si el registro es exitoso.
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid RegisterPatientRequestDTO request) {
        log.info("Registro iniciado para email: {}", request.getEmail());
        String result = authService.register(request);
        return ResponseEntity.ok().body(Map.of("message", result));
    }
    
    /*
     * Endpoint para el inicio de sesión de usuarios.
     * Recibe un objeto AuthRequest en el cuerpo de la solicitud.
     * Retorna un objeto AuthResponse con el token JWT si el inicio de sesión es exitoso.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid AuthRequestDTO request) {
        log.info("Login iniciado para email: {}", request.getEmail());
        AuthResponseDTO response = authService.login(request);
        log.info("Login exitoso para usuario: {}", request.getEmail());
        return ResponseEntity.ok(response);
    }

    /*
     * Endpoint de "Primer acceso": activa la cuenta web de un paciente dado de alta
     * por secretaría (active=false), verificando DUI + email + fecha de nacimiento
     * y estableciendo su contraseña por primera vez.
     */
    @PostMapping("/activate")
    public ResponseEntity<?> activate(@RequestBody @Valid AccountActivationRequestDTO request) {
        log.info("Activación de cuenta iniciada para DUI: {}", request.getDui());
        AuthResponseDTO response = authService.activateAccount(request);
        log.info("Activación de cuenta exitosa");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String token) {
        try {
            // Remover "Bearer " del token
            String jwt = token.replace("Bearer ", "");
            boolean isValid = authService.validateToken(jwt);
            return ResponseEntity.ok().body(Map.of("valid", isValid));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("valid", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> currentUser(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(authService.currentUser(userDetails.getUsername()));
    }
}
