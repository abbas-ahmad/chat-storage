package store.chat_storage.config;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@Profile("docker")
public class DockerSecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(DockerSecurityConfig.class);

    // Use same property names/fallbacks as SecurityConfig
    @Value("${spring.security.api-key:${security.api-key:${SECURITY_API_KEY:}}}")
    private String serverApiKey;

    @PostConstruct
    public void postConstruct() {
        if (serverApiKey == null || serverApiKey.isEmpty()) {
            logger.warn("[docker] No API key configured (spring.security.api-key, security.api-key or SECURITY_API_KEY empty). Requests will be unauthorized.");
        } else {
            logger.info("[docker] API key configured for authentication (masked): {}", mask(serverApiKey));
        }
    }

    private static String mask(String s) {
        if (s == null) return null;
        if (s.length() <= 8) return "****";
        return s.substring(0, 4) + "..." + s.substring(s.length() - 4);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        ApiKeyAuthFilter apiKeyFilter = new ApiKeyAuthFilter(serverApiKey);

        http
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/public/**").permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/swagger-ui/**").permitAll()
                        .requestMatchers("/v3/api-docs/**").permitAll()
                        .requestMatchers("/swagger-ui.html").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(apiKeyFilter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    static class ApiKeyAuthFilter extends OncePerRequestFilter {
        private final String expectedKey;
        private static final String HEADER_NAME = "X-API-KEY";

        ApiKeyAuthFilter(String expectedKey) {
            this.expectedKey = expectedKey;
        }

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                throws ServletException, IOException {

            String provided = request.getHeader(HEADER_NAME);

            if (provided == null || provided.isEmpty()) {
                filterChain.doFilter(request, response);
                return;
            }

            if (expectedKey == null || expectedKey.isEmpty()) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "API key not configured on server");
                return;
            }

            if (!expectedKey.equals(provided)) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid API key");
                return;
            }

            Authentication auth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                    "api-key",
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_API"))
            );
            SecurityContextHolder.getContext().setAuthentication(auth);
            filterChain.doFilter(request, response);
        }
    }
}
