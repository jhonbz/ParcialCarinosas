package com.carinosas.api_gateway.config;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import static org.springframework.security.config.Customizer.withDefaults;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        // Le damos acceso VIP a tu servidor local de Python
        corsConfig.setAllowedOrigins(List.of("http://localhost:5500", "http://127.0.0.1:5500"));
        // Le decimos qué acciones están permitidas
        corsConfig.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        // Permitimos que pasen todas las cabeceras (incluyendo nuestro Authorization Bearer)
        corsConfig.setAllowedHeaders(List.of("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);
        return source;
    }
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
                .cors(withDefaults())
                .csrf(csrf -> csrf.disable())
                .authorizeExchange(exchanges -> exchanges
                        // <-- Deja pasar el saludo invisible (Preflight) del navegador sin pedir token
                        .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 🛑 REGLAS DE ORO (RBAC) 🛑
                        // 1. Solo un ADMIN puede borrar cualquier cosa en cualquier microservicio
                        .pathMatchers(HttpMethod.DELETE, "/**").hasRole("ADMIN")

                        // 2. Solo un ADMIN puede crear y asignar tareas en el workflow
                        .pathMatchers(HttpMethod.POST, "/tasks/**").hasRole("ADMIN")

                        // 3. Solo un ADMIN puede crear casos nuevos
                        .pathMatchers(HttpMethod.POST, "/cases/**").hasRole("ADMIN")

                        // 4. Para todo lo demás (ej. GET ver casos, agregar evidencias, etc.),
                        // basta con tener un token válido (estar autenticado)
                        .anyExchange().authenticated()
                )
                // Le decimos al Gateway que use nuestro traductor especial de Keycloak
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(grantedAuthoritiesExtractor()))
                );
        return http.build();
    }

    // --- EL TRADUCTOR DE KEYCLOAK ---
    // Spring Security necesita leer "ROLE_ADMIN", pero Keycloak solo envía "ADMIN".
    // Esta clase extrae la lista de roles de Keycloak y la adapta para Spring.
    private ReactiveJwtAuthenticationConverterAdapter grantedAuthoritiesExtractor() {
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(new KeycloakRoleConverter());
        return new ReactiveJwtAuthenticationConverterAdapter(jwtAuthenticationConverter);
    }

    static class KeycloakRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {
        @Override
        public Collection<GrantedAuthority> convert(Jwt jwt) {
            Map<String, Object> realmAccess = (Map<String, Object>) jwt.getClaims().get("realm_access");
            if (realmAccess == null || realmAccess.isEmpty()) {
                return List.of();
            }
            List<String> roles = (List<String>) realmAccess.get("roles");

            return roles.stream()
                    // El compilador ahora será feliz con este pequeño "cast" implícito
                    .map(roleName -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + roleName))
                    .collect(Collectors.toList());
        }
    }
}