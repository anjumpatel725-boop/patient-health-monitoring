
package com.healthcare.monitoring.config;

import com.healthcare.monitoring.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter
    ) throws Exception {

        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> {})
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable())
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint((request, response, authException) ->
                    response.sendError(
                        HttpServletResponse.SC_UNAUTHORIZED,
                        "Unauthorized"
                    )
                )
            )
            .authorizeHttpRequests(auth -> auth

                // Public access: only patient registration and login
                .requestMatchers(
                    HttpMethod.POST,
                    "/api/auth/register",
                    "/api/auth/login"
                ).permitAll()

                // Authenticated user profile
                .requestMatchers(
                    HttpMethod.GET,
                    "/api/auth/me"
                ).authenticated()

                // Admin-only user management
                .requestMatchers("/api/users/**")
                    .hasRole("ADMIN")

                // Admin-only access to all health records
                .requestMatchers(
                    HttpMethod.GET,
                    "/api/health/all"
                ).hasRole("ADMIN")

                // Admin-only health data creation
                .requestMatchers(
                    HttpMethod.POST,
                    "/api/health/**"
                ).hasRole("ADMIN")

                // Individual health records require authentication
                .requestMatchers("/api/health/**")
                    .authenticated()

                // Admin-only automatic patient assignment
                .requestMatchers(
                    HttpMethod.POST,
                    "/api/assignments/auto-assign"
                ).hasRole("ADMIN")

                // Doctor assignment data requires authentication
                .requestMatchers(
                    HttpMethod.GET,
                    "/api/assignments/doctor/**"
                ).authenticated()
                .requestMatchers(
    HttpMethod.GET,
    "/api/assignments/patient/me"
).authenticated()

                // Deny every other API endpoint by default
                .requestMatchers("/api/**").denyAll()

                // Non-API routes remain unaffected
                .anyRequest().permitAll()
            )
            .addFilterBefore(
                jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }
}
