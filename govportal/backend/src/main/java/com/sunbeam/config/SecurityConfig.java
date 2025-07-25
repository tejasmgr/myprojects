package com.sunbeam.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.sunbeam.model.CustomUserDetails;
import com.sunbeam.model.User;
import com.sunbeam.repository.UserRepository;
import com.sunbeam.security.JwtAuthFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
	private final JwtAuthFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;
    private final CustomAuthenticationEntryPoint customEntryPoint;
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    		return http.csrf(csrf -> csrf.disable())
    			.cors(cors -> cors.configurationSource(corsConfigurationSource()))
    			.exceptionHandling(ex -> ex.authenticationEntryPoint(customEntryPoint)) 
    			.authorizeHttpRequests(auth -> auth
    					.requestMatchers(
		                        "/api/auth/**",
		                        "/v3/api-docs/**",
		                        "/swagger-ui/**",
		                        "/swagger-ui.html")
    					.permitAll()
    					.requestMatchers(
    		                    "/v1/api/**",
    		                    "/v2/api-docs",
    		                    "/v3/api-docs",
    		                    "/v3/api-docs/**",
    		                    "/swagger-resources",
    		                    "/swagger-resources/**",
    		                    "/configuration/ui",
    		                    "/configuration/security",
    		                    "/swagger-ui/**",
    		                    "/webjars/**",
    		                    "/swagger-ui.html"
    		                ).permitAll()
    					
		                .requestMatchers("/api/admin/**").hasRole("ADMIN")
		                .requestMatchers("/api/verifier/**").hasAnyRole("VERIFIER", "ADMIN")
		                .requestMatchers("/api/user/**").authenticated()
		                .anyRequest().authenticated()
                
        ).sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
    	.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
    	.userDetailsService(userDetailsService).build();
    }
    
    @Bean
	public  CorsConfigurationSource corsConfigurationSource() {
    	CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:3000")); // React app origin
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
	}
    
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    

    
}
