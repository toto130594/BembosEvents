package com.bembosevents.config;

import com.bembosevents.security.JwtAuthenticationFilter;
import com.bembosevents.security.RateLimitFilter;
import com.bembosevents.security.RestAccessDeniedHandler;
import com.bembosevents.security.RestAuthenticationEntryPoint;
import com.bembosevents.security.UsuarioDetailsService;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final UsuarioDetailsService usuarioDetailsService;

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    private final RateLimitFilter rateLimitFilter;

    private final RestAuthenticationEntryPoint authenticationEntryPoint;

    private final RestAccessDeniedHandler accessDeniedHandler;

    public SecurityConfig(
            UsuarioDetailsService usuarioDetailsService,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            RateLimitFilter rateLimitFilter,
            RestAuthenticationEntryPoint authenticationEntryPoint,
            RestAccessDeniedHandler accessDeniedHandler) {

        this.usuarioDetailsService =
                usuarioDetailsService;

        this.jwtAuthenticationFilter =
                jwtAuthenticationFilter;

        this.rateLimitFilter =
                rateLimitFilter;

        this.authenticationEntryPoint =
                authenticationEntryPoint;

        this.accessDeniedHandler =
                accessDeniedHandler;
    }

    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter>
            jwtFilterRegistration(
                    JwtAuthenticationFilter filter) {

        FilterRegistrationBean<JwtAuthenticationFilter> registration =
                new FilterRegistrationBean<>(filter);

        registration.setEnabled(false);

        return registration;
    }

    @Bean
    public FilterRegistrationBean<RateLimitFilter>
            rateLimitFilterRegistration(
                    RateLimitFilter filter) {

        FilterRegistrationBean<RateLimitFilter> registration =
                new FilterRegistrationBean<>(filter);

        registration.setEnabled(false);

        return registration;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(
            PasswordEncoder passwordEncoder) {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider();

        provider.setUserDetailsService(
                usuarioDetailsService);

        provider.setPasswordEncoder(
                passwordEncoder);

        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration)
            throws Exception {

        return configuration
                .getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            AuthenticationProvider authenticationProvider)
            throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS))

                .authenticationProvider(
                        authenticationProvider)

                .exceptionHandling(exceptions ->
                        exceptions
                                .authenticationEntryPoint(
                                        authenticationEntryPoint)
                                .accessDeniedHandler(
                                        accessDeniedHandler))

                .authorizeHttpRequests(auth ->
                        auth
                                .requestMatchers(
                                        "/api/auth/**",
                                        "/error")
                                .permitAll()

                                .requestMatchers(
                                        HttpMethod.GET,
                                        "/api/sedes",
                                        "/api/sedes/**",
                                        "/api/salones",
                                        "/api/salones/**",
                                        "/api/paquetes",
                                        "/api/paquetes/**",
                                        "/api/tematicas",
                                        "/api/tematicas/**",
                                        "/api/servicios-adicionales",
                                        "/api/servicios-adicionales/**")
                                .permitAll()

                                .requestMatchers(
                                        "/api/backoffice/**")
                                .hasAnyRole(
                                        "ENCARGADO_LOCAL",
                                        "ADMINISTRADOR")

                                .requestMatchers(
                                        "/api/usuarios/**")
                                .hasRole("ADMINISTRADOR")

                                .requestMatchers(
                                        HttpMethod.GET,
                                        "/api/pagos/*")
                                .hasRole("CLIENTE")

                                .requestMatchers(
                                        "/api/reservas/**")
                                .hasAnyRole(
                                        "CLIENTE",
                                        "ENCARGADO_LOCAL",
                                        "ADMINISTRADOR")

                                .anyRequest()
                                .authenticated())

                .addFilterBefore(
                        rateLimitFilter,
                        UsernamePasswordAuthenticationFilter.class)

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
