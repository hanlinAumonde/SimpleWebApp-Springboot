package com.devStudy.chat.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.config.http.CorsBeanDefinitionParser;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

import com.devStudy.chat.security.AccountAuthenticationFailureHandler;
import com.devStudy.chat.security.AccountAuthenticationProvider;
import com.devStudy.chat.security.AccountAuthenticationSuccessHandler;
import com.devStudy.chat.security.AccountLogoutSuccessHandler;


@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Autowired
    private AccountAuthenticationProvider authProvider;

    /**
     * C'est pour inscrire le AuthenticationProvider dans le AuthenticationManager
     * dans l'environment global de l'application
     */
    @Bean
    public AuthenticationManager authManager(HttpSecurity http) throws Exception{
        AuthenticationManagerBuilder authenticationManagerBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.authenticationProvider(authProvider);
        return authenticationManagerBuilder.build();
    }

    /**
     * C'est le filtre de sécurité qui va être appliqué à toutes les requêtes
     * Il replace "configure(HttpSecurity http)" dans la classe "WebSecurityConfigurerAdapter"
     * qui a le meme fonctionnement
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
                		.antMatchers("/user/**").hasRole("USER")
                        .antMatchers("/users/logged", "/reset-password/**").permitAll()
                        .antMatchers("ws://localhost:8080/**").hasRole("USER")
                        .anyRequest().authenticated()
                )

                .formLogin(formLogin -> 
                	formLogin
						.loginProcessingUrl("/login-check")
						.successHandler(new AccountAuthenticationSuccessHandler())
						.failureHandler(new AccountAuthenticationFailureHandler())
						.permitAll()
                )
                
                .logout(logout -> 
                    logout
	                    .logoutUrl("/logout")
	                    .invalidateHttpSession(true)
	                    .clearAuthentication(true)
	                    .logoutSuccessHandler(new AccountLogoutSuccessHandler())
	                    .permitAll()
                );
        return http.build();
    }

    /**
     * C'est pour encoder le mot de passe
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    
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
