package com.rojas.spring.appgestion.productos.config;

import com.rojas.spring.appgestion.productos.Service.impl.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final JwtFilter jwtFilter; // <--- Nuevo: Inyectamos el filtro

    public SecurityConfig(UserDetailsServiceImpl userDetailsService, PasswordEncoder passwordEncoder, JwtFilter jwtFilter) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public org.springframework.security.config.core.GrantedAuthorityDefaults grantedAuthorityDefaults() {
        return new org.springframework.security.config.core.GrantedAuthorityDefaults("");
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // 1. Acceso público: Auth, Swagger, Documentación e Imágenes
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/uploads/**").permitAll()
                        .requestMatchers("/api/v1/auth/**").permitAll()

                        // 2. PRODUCTOS: Hacemos que TODO lo que sea GET en /product/** sea público
                        // Importante: Revisa si tu controlador usa /product o /products
                        .requestMatchers(HttpMethod.GET, "/api/v1/product/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/productos/**").permitAll() // Por si acaso
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 3. PRODUCTOS OPERACIONES (ADMIN): Requiere rol ADMIN
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/product/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/product/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/product/**").hasAuthority("ADMIN")

                        // 4. REPORTE DE VENTAS: Solo ADMIN
                        .requestMatchers("/api/v1/order/reporte-ventas").hasAuthority("ADMIN")

                        // 5. ÓRDENES: Requiere estar logueado (cualquier rol)
                        .requestMatchers("/api/v1/order/**").authenticated()

                        // 6. Cualquier otra ruta requiere estar autenticado
                        .anyRequest().authenticated()
                )
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // --- FILTRO JWT ---
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        // 3. Permitimos todos los encabezados (necesario para el header 'Authorization' del JWT)
        configuration.setAllowedHeaders(List.of("*"));
        // 4. CAMBIO CLAVE: Cambiar a 'false' si usas "*" en los orígenes.
        // Como usas JWT en LocalStorage, no necesitas enviar Cookies automáticas,
        // por lo que 'false' es lo correcto y evitará errores de seguridad en el navegador.
        configuration.setAllowCredentials(false);
        // 5. Exponemos el header de Authorization por si el front necesita leerlo
        configuration.setExposedHeaders(List.of("Authorization"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean //todo el manager y administrador
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder
                .userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder);
        return authenticationManagerBuilder.build();
    }
}