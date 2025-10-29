package cc.nuvu.qapi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.Scopes;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class SwaggerConfig {

    @Value("${spring.profiles.active:default}")
    private String profile;

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String realmUrl;

    @Bean
    public OpenAPI customOpenAPI() {

        final String securitySchemeName = "keycloak-oauth2";
        return new OpenAPI()
                .components(
                        new Components()
                                .addSecuritySchemes(securitySchemeName,
                                        new SecurityScheme()
                                                .type(SecurityScheme.Type.OAUTH2)
                                                .flows(new OAuthFlows()
                                                        .clientCredentials(new OAuthFlow()
                                                        .tokenUrl(realmUrl + "/protocol/openid-connect/token")
                                                        .scopes(new Scopes().addString("openid", "acceso"))
                                                        ))))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName));
    }
}
