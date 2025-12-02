package store.chat_storage.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        // Name of the security scheme to reference it in SecurityRequirement
        final String securitySchemeName = "ApiKeyAuth";

        SecurityScheme apiKeyScheme = new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.HEADER)
                .name("X-API-KEY")
                .description("API Key needed to access the endpoints. Send as header X-API-KEY: <key>");

        SecurityRequirement securityRequirement = new SecurityRequirement().addList(securitySchemeName);

        return new OpenAPI()
                .components(new Components().addSecuritySchemes(securitySchemeName, apiKeyScheme))
                .addSecurityItem(securityRequirement)
                .info(new Info()
                        .title("RAG Chat Storage API")
                        .version("1.0")
                        .description("API for storing and managing RAG chat sessions"))
                .servers(List.of(new Server().url("http://localhost:" + serverPort)));
    }
}
