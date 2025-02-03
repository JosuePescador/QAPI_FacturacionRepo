package cc.nuvu.qapi.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

public class JwtUtil {

    private static final String SECRET_KEY = "clave-segura-de-al-menos-32-caracteres";

    public static Claims parseToken(String token) throws JwtException {
        return Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public static boolean isTokenValid(Claims claims) {
        Date now = new Date();
        Date exp = claims.getExpiration();
        Date nbf = claims.getNotBefore();

        if (exp != null && exp.before(now)) {
            return false; // Token expirado
        }
        if (nbf != null && nbf.after(now)) {
            return false; // Token aún no válido
        }
        return true;
    }
}
