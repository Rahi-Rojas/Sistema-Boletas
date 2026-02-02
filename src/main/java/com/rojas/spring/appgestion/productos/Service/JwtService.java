package com.rojas.spring.appgestion.productos.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtService {

    // Usa una clave secreta de al menos 32 caracteres
    private static final String SECRET_KEY = "tu_clave_secreta_super_segura_que_debe_ser_larga_y_fuerte";

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
    // Transforma la cadena de texto de la SECRET_KEY en una llave criptográfica segura
    private Key getSignInKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
    }
    // Creamos el token si el user inicio sesión sin fallas
    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24)) // 24 horas
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact(); //todo: aumentar el refreshToken cada 10 minutos
    }
}