package com.tienda.gamer.security;

import com.tienda.gamer.model.User; // Importa tu modelo User
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    @Value("${application.security.jwt.expiration}")
    private long jwtExpirationMs;

    private SecretKey getSigningKey() {
        // Aseguramos que la clave Base64 se decodifique correctamente
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // ========= EXTRACCIÓN =========

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // MÉTODO CORREGIDO: Extraer el ID del usuario como Long
    public Long extractUserId(String token) {
        // El claim 'userId' se almacena como un Integer o Long, por lo que lo leemos como Object
        // y lo convertimos a Long para seguridad de tipos.
        Object userIdClaim = extractClaim(token, claims -> claims.get("userId"));
        if (userIdClaim instanceof Integer) {
            return ((Integer) userIdClaim).longValue();
        } else if (userIdClaim instanceof Long) {
            return (Long) userIdClaim;
        }
        return null;
    }

    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return resolver.apply(claims);
    }

    // ========= GENERACIÓN =========

    public String generateToken(UserDetails userDetails, String role) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", role);

        // AÑADIR EL ID DEL USUARIO A LOS CLAIMS (Aseguramos que es un Long si es posible)
        if (userDetails instanceof User user) {
            extraClaims.put("userId", user.getId());
        }

        // buildToken requiere String subject, que es el username
        return buildToken(extraClaims, userDetails.getUsername());
    }

    private String buildToken(Map<String, Object> extraClaims, String subject) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .claims(extraClaims)
                .subject(subject) // Este es el String username
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getSigningKey())
                .compact();
    }

    // ========= VALIDACIÓN =========

    public boolean isTokenValid(String token, UserDetails userDetails) {
        String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        Date exp = extractClaim(token, Claims::getExpiration);
        return exp.before(new Date());
    }
}