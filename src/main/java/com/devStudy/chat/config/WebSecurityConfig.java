package com.devStudy.chat.config;

import java.util.Map;

import javax.sql.DataSource;

import com.devStudy.chat.security.*;
import com.devStudy.chat.service.implementations.JwtTokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

import com.devStudy.chat.service.implementations.UserService;

import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {
	
	@Value("${chatroomApp.rememberMe.key}")
	private String rememberMeKey;
	
	@Value("${chatroomApp.rememberMe.expirationTime}")
	private int rememberMeExpirationTime;

    /**
     * C'est pour encoder le mot de passe
     * @return PasswordEncoder
     */
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * C'est pour instancier le AccountAuthenticationProvider avec les dépendances
     * @param passwordEncoder
     * @param userService
     * @return AccountAuthenticationProvider
     */
    @Bean
    AccountAuthenticationProvider authProvider(
            PasswordEncoder passwordEncoder,
            UserService userService) {
		return new AccountAuthenticationProvider(passwordEncoder, userService);
	}

    /**
     * C'est pour inscrire le AuthenticationProvider dans le AuthenticationManager
     * dans l'environment global de l'application
     * @param http
     * @param authProvider
     * @return AuthenticationManager
     * @throws Exception
     */
    @Bean
    AuthenticationManager authManager(HttpSecurity http, AccountAuthenticationProvider authProvider) throws Exception{
        AuthenticationManagerBuilder authenticationManagerBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.authenticationProvider(authProvider);
        return authenticationManagerBuilder.build();
    }

    /**
     * C'est pour instancier le PersistentTokenRepository,
     * stocker les tokens liés aux remember-me dans la base de données
     * @param dataSource
     * @return PersistentTokenRepository
     */
    @Bean
    PersistentTokenRepository persistentTokenRepository(DataSource dataSource) {
        JdbcTokenRepositoryImpl tokenRepository = new JdbcTokenRepositoryImpl();
        tokenRepository.setDataSource(dataSource);
        return tokenRepository;
	}

    /**
     * C'est le filtre de sécurité qui va être appliqué à toutes les requêtes
     * Il replace "configure(HttpSecurity http)" dans la classe "WebSecurityConfigurerAdapter"
     * qui a le meme fonctionnement
     * @param http
     * @param persistentTokenRepository
     * @param userDetailService
     * @return SecurityFilterChain
     */
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http,
                                    PersistentTokenRepository persistentTokenRepository,
                                    UserService userDetailService,
                                    JwtAuthenticationFilter jwtAuthenticationFilter,
                                    JwtTokenService jwtTokenService) throws Exception {
        return http    
          
                .csrf(AbstractHttpConfigurer::disable)

                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                
                .rememberMe(rememberMeConfig -> 
                	rememberMeConfig
	                	.tokenRepository(persistentTokenRepository)
	                    .key(rememberMeKey) 
	                    .tokenValiditySeconds(rememberMeExpirationTime)
	                    .userDetailsService(userDetailService)  
	                    .authenticationSuccessHandler(new AccountAuthenticationSuccessHandler(jwtTokenService))
                )
                
                .authorizeHttpRequests(auth -> 
                	auth
                		.requestMatchers("/api/users/**","/api/chatrooms/**").hasRole("USER")
                        .requestMatchers("/api/login/**").permitAll()
                        //.requestMatchers("ws://localhost:8080/**").hasRole("USER")
                        .requestMatchers("ws://").hasRole("USER")
                        .anyRequest().authenticated()
                )

                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                .formLogin(formLogin -> 
                	formLogin
						.loginProcessingUrl("/api/login/login-process")
						.successHandler(new AccountAuthenticationSuccessHandler(jwtTokenService))
						.failureHandler(new AccountAuthenticationFailureHandler())
                )

                .exceptionHandling(exception -> 
                    exception.authenticationEntryPoint((request, response, authException) -> {
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.setContentType("application/json;charset=UTF-8");
                        response.getWriter().write(new ObjectMapper().writeValueAsString(
                                Map.ofEntries(
                                        Map.entry("status", "error"),
                                        Map.entry("message", "Unauthorized"),
                                        Map.entry("isAuthenticated", false)
                                )
                        ));
                    })
                )
                .build();
    }
}
