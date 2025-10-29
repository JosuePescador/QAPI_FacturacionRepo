package cc.nuvu.qapi.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${spring.profiles.active:default}")
    private String profile;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        if (profile.equals("dev")) {

            http
                    .csrf(CsrfConfigurer::disable)
                    .cors(t -> {
                        t.configurationSource(request -> {
                            var cors = new org.springframework.web.cors.CorsConfiguration();
                            cors.setAllowedOriginPatterns(List.of("*"));
                            cors.setAllowedMethods(List.of("*"));
                            cors.setAllowedHeaders(List.of("*"));
                            return cors;
                        });
                    })
                    .authorizeHttpRequests(
                            expressionInterceptUrlRegistry -> expressionInterceptUrlRegistry
                                    .requestMatchers("/**").permitAll()
                                    .anyRequest()
                                    .authenticated())
                    .oauth2ResourceServer(
                            oauth -> {
                                oauth.jwt(jwt -> {});
                            })
                    .sessionManagement(session -> session
                            .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

            return http.build();
        }

        http
                .csrf(CsrfConfigurer::disable)
                .cors(t -> {
                    t.configurationSource(request -> {
                        var cors = new org.springframework.web.cors.CorsConfiguration();
                        cors.setAllowedOriginPatterns(List.of("*"));
                        cors.setAllowedMethods(List.of("*"));
                        cors.setAllowedHeaders(List.of("*"));
                        return cors;
                    });
                })
                .authorizeHttpRequests(
                        expressionInterceptUrlRegistry -> expressionInterceptUrlRegistry
                                .requestMatchers("/iam/**").permitAll()
                                .requestMatchers("/helpCheck/**").permitAll()
                                .requestMatchers("/version/**").permitAll()
                                .requestMatchers("/ui/**").permitAll()
                                .requestMatchers(
                                        "/swagger-ui/**",
                                        "/swagger-ui.html",
                                        "/v3/api-docs/**",
                                        "/api-docs/**",
                                        "/webjars/**")
                                .permitAll()
                                .anyRequest()
                                .authenticated())
                .oauth2ResourceServer(
                        oauth -> {
                            oauth.jwt(jwt -> {});
                        })
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }
}
