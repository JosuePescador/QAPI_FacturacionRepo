package cc.nuvu.qapi.security;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.core.userdetails.User;
import java.util.Collections;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthorizationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

                

        String authHeader = request.getHeader("Authorization");
        System.out.println("Authorization Header: " + authHeader); // Verifica el encabezado


        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Falta token de autorización");
            return;
        }

        String token = authHeader.replace("Bearer ", "").trim();

        try {
            Claims claims = jwtUtil.parseToken(token);
            if (!JwtUtil.isTokenValid(claims)) {
                response.sendError(HttpStatus.UNAUTHORIZED.value(), "Token inválido o expirado");
                return;
            }

            // Autenticación en Spring Security
            String username = claims.getSubject(); // Extraer el usuario del token
            UserDetails userDetails = new User(username, "", Collections.emptyList());
            
            UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            SecurityContextHolder.getContext().setAuthentication(authToken);

            System.out.println("Usuario autenticado correctamente: " + username);

        } catch (JwtException e) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Token no válido: " + e.getMessage());
            return;
        } catch (Exception e) {
            response.sendError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error interno del servidor");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
