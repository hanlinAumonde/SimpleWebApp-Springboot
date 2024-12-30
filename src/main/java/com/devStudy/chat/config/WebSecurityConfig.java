package com.devStudy.chat.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.devStudy.chat.security.AccountAuthenticationFailureHandler;
import com.devStudy.chat.security.AccountAuthenticationProvider;
import com.devStudy.chat.security.AccountAuthenticationSuccessHandler;
import com.devStudy.chat.security.AccountLogoutSuccessHandler;
import com.devStudy.chat.service.implementations.UserService;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {
	
	/**
     * C'est pour encoder le mot de passe
     * @return
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
	
    /**
     * C'est pour instancier le AccountAuthenticationProvider avec les dépendances
     * @param passwordEncoder
     * @param userService
     * @return
     */
	@Bean
	public AccountAuthenticationProvider authProvider(
			PasswordEncoder passwordEncoder,
			UserService userService) {
		return new AccountAuthenticationProvider(passwordEncoder, userService);
	}

    /**
     * C'est pour inscrire le AuthenticationProvider dans le AuthenticationManager
     * dans l'environment global de l'application
     * @param http
     * @param authProvider
     * @return
     * @throws Exception
     */
    @Bean
    public AuthenticationManager authManager(HttpSecurity http, AccountAuthenticationProvider authProvider) throws Exception{
        AuthenticationManagerBuilder authenticationManagerBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.authenticationProvider(authProvider);
        return authenticationManagerBuilder.build();
    }

    /**
     * C'est le filtre de sécurité qui va être appliqué à toutes les requêtes
     * Il replace "configure(HttpSecurity http)" dans la classe "WebSecurityConfigurerAdapter"
     * qui a le meme fonctionnement
     * @param http
     * @return
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http    
        		.cors(cors -> 
        			cors.configurationSource(corsConfigurationSource())
        		)
        		
                .csrf(csrf -> 
                	csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                )
                
                .authorizeRequests(auth -> 
                	auth
                		.antMatchers("/api/users/**","/api/chatrooms/**").hasRole("USER")
                        .antMatchers("/api/login/**").permitAll()
                        .antMatchers("ws://localhost:8080/**").hasRole("USER")
                        .anyRequest().authenticated()
                )

                .formLogin(formLogin -> 
                	formLogin
						.loginProcessingUrl("/api/login/login-process")
						.successHandler(new AccountAuthenticationSuccessHandler())
						.failureHandler(new AccountAuthenticationFailureHandler())
                )
                
                .logout(logout -> 
                    logout
	                    .logoutUrl("/api/login/logout")
	                    .invalidateHttpSession(true)
	                    .clearAuthentication(true)
	                    .logoutSuccessHandler(new AccountLogoutSuccessHandler())
                );
        return http.build();
    }

    /**
     * C'est pour configurer le CORS
     * @return
     */
    @Bean
	public CorsConfigurationSource corsConfigurationSource() {
    	CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.addAllowedOriginPattern("*");
        corsConfiguration.addAllowedHeader("*");
        corsConfiguration.addAllowedMethod("*");
        corsConfiguration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);
        return source;
	}

}
