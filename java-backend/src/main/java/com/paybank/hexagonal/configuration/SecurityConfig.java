package  com.paybank.hexagonal.configuration;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        /*
    	http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // Autorise explicitement l'accès aux fichiers statiques et au login
                .requestMatchers("/", "/index.html", "/login.html", "/api/auth/login").permitAll()
                // Toutes les autres requêtes API doivent être authentifiées
                .anyRequest().authenticated()
            );
        
        return http.build();
        */
    	/*
    	http
        .cors(cors -> cors.configurationSource(corsConfigurationSource())) // Activez le CORS ici
        .csrf(csrf -> csrf.disable()) // Désactivez le CSRF pour les APIs
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/auth/**").permitAll() // Autorisez l'accès au login
            .anyRequest().authenticated()
        );
    	return http.build();
    	*/
    	/*
    	http
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .csrf(csrf -> csrf.disable()) // Crucial pour les formulaires POST
        .authorizeHttpRequests(auth -> auth
            // Autoriser l'accès à la page d'accueil et au login
            .requestMatchers("/", "/index.html", "/api/auth/**").permitAll()
            // Autoriser les ressources statiques (CSS, JS)
            .requestMatchers("/api/admin/**").hasAuthority("ADMIN")
            .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
            // Tout le reste nécessite une authentification
            .anyRequest().authenticated()
        );
    	return http.build();
    	*/
    	http
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(auth -> auth
            // Autoriser TOUT pendant 5 minutes pour tester
            .requestMatchers("/**").permitAll() 
        );
        return http.build();
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://127.0.0.1:8080", "http://localhost:8080"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
    
}