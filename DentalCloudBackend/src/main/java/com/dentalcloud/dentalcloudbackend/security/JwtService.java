package com.dentalcloud.dentalcloudbackend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;

/*
    Servicio para la generación y validación de tokens JWT.
 */
@Service
@Slf4j
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration-ms:1296000000}")
    private long expirationMs;

    /*
        Genera un token JWT para el email proporcionado.
     */
    public String generateToken(String email) {
        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getSignInKey())
                .compact();
    }

    /*
        Extrae el nombre de usuario (email) del token JWT.
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /*
        Extrae la fecha de expiración del token JWT.
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /*
        Extrae cualquier reclamo del token JWT usando la función proporcionada.
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claimsResolver.apply(claims);
    }

    /*
        Verifica si el token JWT ha expirado.
     */
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /*
        Valida el token JWT verificando su firma y expiración.
     */
    public boolean isTokenValid(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSignInKey())
                    .build()
                    .parseSignedClaims(token);
            return !isTokenExpired(token);
        } catch (ExpiredJwtException e) {
            log.warn("Token expirado: {}", e.getMessage());
            return false;
        } catch (SignatureException e) {
            log.warn("Firma inválida: {}", e.getMessage());
            return false;
        } catch (MalformedJwtException e) {
            log.warn("Token malformado: {}", e.getMessage());
            return false;
        } catch (UnsupportedJwtException e) {
            log.warn("Token no soportado: {}", e.getMessage());
            return false;
        } catch (IllegalArgumentException e) {
            log.warn("Token vacío o nulo: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("Error validando token: {}", e.getMessage());
            return false;
        }
    }

    /*
        Valida el token JWT comparando el nombre de usuario y verificando su expiración.
     */
    public boolean isTokenValid(String token, String username) {
        final String tokenUsername = extractUsername(token);
        return (tokenUsername.equals(username)) && !isTokenExpired(token);
    }

    /*
        Obtiene la clave de firma a partir del secreto configurado.
     */
    private SecretKey getSignInKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}