package cc.nuvu.qapi.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import cc.nuvu.qapi.service.SecretService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {

    private final SecretService secretService;

    public JwtUtil(SecretService secretService) {
        this.secretService = secretService;
    }
    
    public Claims parseToken(String token) throws JwtException {
        String secretKey = secretService.getSecret(); // 🔹 Obtiene la clave desde AWS
        System.out.println("Using Secret: " + secretKey);  // Verifica el secreto usado
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