package bytecode.rag_chat_storage.config;

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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
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
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    // Try multiple property names/fallbacks so local runs and docker runs work consistently.
    // Order: spring.security.api-key -> security.api-key -> SECURITY_API_KEY env
    @Value("${spring.security.api-key:${security.api-key:${SECURITY_API_KEY:}}}")
    private String serverApiKey;

    @PostConstruct
    public void postConstruct() {
        if (serverApiKey == null || serverApiKey.isEmpty()) {
            logger.warn("No API key configured (spring.security.api-key, security.api-key or SECURITY_API_KEY empty). Requests will be unauthorized.");
        } else {
            logger.info("API key configured for authentication (masked): {}", mask(serverApiKey));
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
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/public/**").permitAll()
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/swagger-ui/**").permitAll()
                        .requestMatchers("/v3/api-docs/**").permitAll()
                        .requestMatchers("/swagger-ui.html").permitAll()
                        .anyRequest().authenticated()
                )
                // Add API key filter before the username/password auth filter
                .addFilterBefore(apiKeyFilter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Simple OncePerRequestFilter that validates an API key from header X-API-KEY.
     */
    static class ApiKeyAuthFilter extends OncePerRequestFilter {
        private final String expectedKey;
        private static final String HEADER_NAME = "X-API-KEY";
        private final Logger log = LoggerFactory.getLogger(ApiKeyAuthFilter.class);

        ApiKeyAuthFilter(String expectedKey) {
            this.expectedKey = expectedKey;
        }

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                throws ServletException, IOException {

            String provided = request.getHeader(HEADER_NAME);

            // No API key header provided: continue and let security handle it (could be a public endpoint)
            if (provided == null || provided.isEmpty()) {
                log.debug("No API key header provided in request to {}", request.getRequestURI());
                filterChain.doFilter(request, response);
                return;
            }

            // Server misconfiguration: expected key not set
            if (expectedKey == null || expectedKey.isEmpty()) {
                log.warn("API key header provided but server has no configured key (serverApiKey empty). Rejecting request to {}", request.getRequestURI());
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "API key not configured on server");
                return;
            }

            // Wrong key
            if (!expectedKey.equals(provided)) {
                log.warn("Invalid API key for request to {}: provided={}, expected=****", request.getRequestURI(), maskPartial(provided));
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid API key");
                return;
            }

            // Success: create an Authentication and continue
            Authentication auth = new UsernamePasswordAuthenticationToken(
                    "api-key", // principal
                    null, // credentials
                    List.of(new SimpleGrantedAuthority("ROLE_API"))
            );
            SecurityContextHolder.getContext().setAuthentication(auth);
            filterChain.doFilter(request, response);
        }

        private String maskPartial(String s) {
            if (s == null) return null;
            if (s.length() <= 8) return "****";
            return s.substring(0, 2) + "..." + s.substring(s.length() - 2);
        }
    }
}
