package com.rojas.spring.appgestion.productos.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtService {

    private final Key secretKey;

    public JwtService(@Value("${jwt.secret}") String secret) {
        if (secret == null || secret.length() < 32) {
            throw new IllegalStateException(
                    "jwt.secret debe tener al menos 32 caracteres. Configúralo en application.properties o como variable de entorno JWT_SECRET.");
        }
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    // metodo para extraer el nombre dentro del token
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    // Método genérico para extraer cualquier información (Claim) específica del token
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    // Verifica si el token pertenece al usuario que intenta acceder y si aún es vigente
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }
    // Comprueba si la fecha de expiración del token es anterior a la fecha actual
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
    // Extrae la fecha de vencimiento configurada en el token
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
    //Abre el token usando la llave secreta para leer todo su contenido (Payload)
    // Si el token fue alterado, este método lanzará una excepción de seguridad
    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    private Key getSignInKey() {
        return secretKey;
    }
    // Creamos el token si el user inicio sesión sin fallas
    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24)) // 24 horas
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }
}