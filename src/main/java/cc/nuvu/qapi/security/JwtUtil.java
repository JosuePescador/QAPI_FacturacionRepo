package cc.nuvu.qapi.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import cc.nuvu.qapi.service.SecretService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class JwtUtil {

    private final SecretService secretService;

    public JwtUtil(SecretService secretService) {
        this.secretService = secretService;
    }
    
public Claims parseToken(String token) throws JwtException {
    String secretJson = secretService.getSecret();

    String secretKey;
    try {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(secretJson);
        secretKey = jsonNode.get("secret").asText();
    } catch (Exception e) {
        throw new RuntimeException("Error al procesar el secreto JWT", e);
    }

    secretKey = secretKey.trim().replaceAll("[^\\x20-\\x7E]", "");

    if (secretKey.isEmpty()) {
        throw new IllegalArgumentException("El secreto JWT no es válido.");
    }

    System.out.println(secretKey);

    return Jwts.parserBuilder()
            .setSigningKey(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)))
            .build()
            .parseClaimsJws(token)
            .getBody();
}
    
    

    public static boolean isTokenValid(Claims claims) {
        Date now = new Date();
        Date exp = claims.getExpiration();
        Date nbf = claims.getNotBefore();

        return (exp != null && exp.after(now)) && (nbf == null || nbf.before(now));
    }
} 