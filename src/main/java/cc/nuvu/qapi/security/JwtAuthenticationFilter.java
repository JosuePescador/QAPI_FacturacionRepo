// package cc.nuvu.qapi.security;

// import com.fasterxml.jackson.databind.JsonNode;
// import com.fasterxml.jackson.databind.ObjectMapper;
// import io.jsonwebtoken.Claims;
// import io.jsonwebtoken.JwtParser;
// import io.jsonwebtoken.Jwts;
// import jakarta.servlet.FilterChain;
// import jakarta.servlet.ServletException;
// import jakarta.servlet.http.HttpServletRequest;
// import jakarta.servlet.http.HttpServletResponse;
// import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
// import org.springframework.security.core.authority.SimpleGrantedAuthority;
// import org.springframework.security.core.context.SecurityContextHolder;
// import org.springframework.stereotype.Component;
// import org.springframework.web.filter.OncePerRequestFilter;

// import java.io.ByteArrayInputStream;
// import java.io.IOException;
// import java.io.InputStream;
// import java.net.HttpURLConnection;
// import java.net.URL;
// import java.security.PublicKey;
// import java.security.cert.CertificateFactory;
// import java.security.cert.X509Certificate;
// import java.util.Base64;
// import java.util.Collections;
// import java.util.Optional;
// import java.util.stream.StreamSupport;

// @Component
// public class JwtAuthenticationFilter extends OncePerRequestFilter {

//     public JwtAuthenticationFilter() {
//     }

//     @Override
//     protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
//             throws ServletException, IOException {

//         String path = request.getServletPath();
//         if (path.equals("/") || path.equals("/status")) {
//             chain.doFilter(request, response);
//             return;
//         }

//         String authorizationHeader = request.getHeader("Authorization");
//         if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
//             response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token missing or malformed");
//             return;
//         }
//         String token = authorizationHeader.substring(7);

//         try {
//             // 1. Extract the JWT header and decode it to get the 'kid'
//             String[] tokenParts = token.split("\\.");
//             if (tokenParts.length < 2) {
//                 response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token format");
//                 return;
//             }
//             String headerJson = new String(Base64.getUrlDecoder().decode(tokenParts[0]));
//             JsonNode headerNode = new ObjectMapper().readTree(headerJson);
//             String kid = headerNode.get("kid").asText();

//             // 2. Download the JWK set from the provided URL
//             URL jwkUrl = new URL("https://iam.ia.ucaldas.nuvu.cc/realms/factMasivaTest/protocol/openid-connect/certs");
//             HttpURLConnection connection = (HttpURLConnection) jwkUrl.openConnection();
//             connection.setRequestMethod("GET");
//             InputStream is = connection.getInputStream();
//             JsonNode jwks = new ObjectMapper().readTree(is);
//             is.close();

//             // 3. Find the matching key using the 'kid'
//             Optional<JsonNode> keyNodeOpt = StreamSupport.stream(jwks.get("keys").spliterator(), false)
//                     .filter(node -> node.get("kid").asText().equals(kid))
//                     .findFirst();

//             if (!keyNodeOpt.isPresent()) {
//                 response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token kid");
//                 return;
//             }
//             JsonNode keyNode = keyNodeOpt.get();

//             // 4. Extract the certificate from the 'x5c' field and convert it to a PublicKey
//             String certString = keyNode.get("x5c").get(0).asText();
//             CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
//             ByteArrayInputStream certStream = new ByteArrayInputStream(Base64.getDecoder().decode(certString));
//             X509Certificate certificate = (X509Certificate) certificateFactory.generateCertificate(certStream);
//             PublicKey publicKey = certificate.getPublicKey();

//             // 5. Build the JWT parser with the public key using the non-deprecated method
//             JwtParser jwtParser = Jwts.parser()
//                     .verifyWith(publicKey)
//                     .build();

//             // Validate the token signature and parse the claims
//             Claims claims = jwtParser.parseClaimsJws(token).getBody();

//             // Optionally check exp and nbf
//             long now = System.currentTimeMillis();
//             if (claims.getExpiration() != null && claims.getExpiration().getTime() < now) {
//                 response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token expired");
//                 return;
//             }
//             if (claims.getNotBefore() != null && claims.getNotBefore().getTime() > now) {
//                 response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token not valid yet");
//                 return;
//             }

//             // Create an Authentication object.
//             // You can extract roles from claims if available. Here, for simplicity, we assign a default role.
//             UsernamePasswordAuthenticationToken authentication =
//                     new UsernamePasswordAuthenticationToken(claims.getSubject(), null,
//                             Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));

//             // Set the authentication in the security context
//             SecurityContextHolder.getContext().setAuthentication(authentication);

//             // Continue with the filter chain
//             chain.doFilter(request, response);
//         } catch (Exception e) {
//             System.out.println("Request - " + request.getRemoteAddr().toString() + " rejected.");
//             response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
//         }
//     }
// }
