package com.dentalcloud.dentalcloudbackend.config;

import com.dentalcloud.dentalcloudbackend.domain.dto.ApiErrorResponse;
import com.dentalcloud.dentalcloudbackend.security.JwtAuthenticationFilter;
import com.dentalcloud.dentalcloudbackend.security.TraceIdFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/*
    Configuración de seguridad para la aplicación Dental Cloud.
    Define las reglas de autorización y autenticación, incluyendo el manejo de CORS y la integración del filtro JWT.
 */
@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;
    private final TraceIdFilter traceIdFilter;
    private final ObjectMapper objectMapper;

    /*
        Configura la cadena de filtros de seguridad HTTP.
        Define las políticas de CORS, CSRF, gestión de sesiones y reglas de autorización para diferentes endpoints.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Endpoints públicos
                        .requestMatchers("/api/auth/login", "/api/auth/register").permitAll()
                        // Validación de token
                        .requestMatchers("/api/auth/validate").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/documentos/*/contenido").permitAll()
                        // Tratamientos: solo roles internos pueden crear
                        .requestMatchers(HttpMethod.POST, "/api/tratamientos").hasAnyRole("DOCTOR", "SECRETARIA", "ADMIN")
                        // Cualquier otra petición requiere autenticación
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(authenticationEntryPoint())
                        .accessDeniedHandler(accessDeniedHandler()))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(traceIdFilter, JwtAuthenticationFilter.class);

        return http.build();
    }

    private AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, exception) -> writeSecurityError(
                request, response, HttpStatus.UNAUTHORIZED,
                "AUTHENTICATION_REQUIRED", "Se requiere una sesión válida.");
    }

    private AccessDeniedHandler accessDeniedHandler() {
        return (request, response, exception) -> writeSecurityError(
                request, response, HttpStatus.FORBIDDEN,
                "ACCESS_DENIED", "No tienes permisos para realizar esta acción.");
    }

    private void writeSecurityError(jakarta.servlet.http.HttpServletRequest request,
                                    jakarta.servlet.http.HttpServletResponse response,
                                    HttpStatus status,
                                    String code,
                                    String message) throws java.io.IOException {
        String traceId = (String) request.getAttribute(TraceIdFilter.TRACE_ID_ATTRIBUTE);
        response.setStatus(status.value());
        response.setContentType("application/json");
        objectMapper.writeValue(response.getOutputStream(), ApiErrorResponse.builder()
                .status(status.value())
                .code(code)
                .message(message)
                .traceId(traceId)
                .timestamp(java.time.Instant.now())
                .build());
    }

    /*
        Configura la fuente de configuración CORS para permitir solicitudes desde el frontend.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:5173"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /*
        Define el codificador de contraseñas utilizando BCrypt.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /*
        Proporciona el gestor de autenticación basado en la configuración de autenticación existente.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }
}
