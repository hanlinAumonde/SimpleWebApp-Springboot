package com.devStudy.chat.config;

import java.util.function.Supplier;

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
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.CsrfTokenRequestHandler;
import org.springframework.security.web.csrf.XorCsrfTokenRequestAttributeHandler;
import org.springframework.util.StringUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.devStudy.chat.security.AccountAuthenticationFailureHandler;
import com.devStudy.chat.security.AccountAuthenticationProvider;
import com.devStudy.chat.security.AccountAuthenticationSuccessHandler;
import com.devStudy.chat.security.AccountLogoutSuccessHandler;
import com.devStudy.chat.service.implementations.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    /**
     * C'est pour encoder le mot de passe
     * @return
     */
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * C'est pour instancier le AccountAuthenticationProvider avec les dépendances
     * @param passwordEncoder
     * @param userService
     * @return
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
     * @return
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
     * C'est le filtre de sécurité qui va être appliqué à toutes les requêtes
     * Il replace "configure(HttpSecurity http)" dans la classe "WebSecurityConfigurerAdapter"
     * qui a le meme fonctionnement
     * @param http
     * @return
     */
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http    
        		.cors(cors -> 
        			cors.configurationSource(corsConfigurationSource())
        		)
        		
                .csrf(csrf -> 
                	csrf
                		.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                		.csrfTokenRequestHandler(new SpaCsrfTokenRequestHandler())
                )
                
                .authorizeHttpRequests(auth -> 
                	auth
                		.requestMatchers("/api/users/**","/api/chatrooms/**").hasRole("USER")
                        .requestMatchers("/api/login/**").permitAll()
                        .requestMatchers("ws://localhost:8080/**").hasRole("USER")
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
    
    //Ref: https://docs.spring.io/spring-security/reference/servlet/exploits/csrf.html#csrf-integration-javascript
    final class SpaCsrfTokenRequestHandler implements CsrfTokenRequestHandler {
    	private final CsrfTokenRequestHandler plain = new CsrfTokenRequestAttributeHandler();
    	private final CsrfTokenRequestHandler xor = new XorCsrfTokenRequestAttributeHandler();

    	@Override
    	public void handle(HttpServletRequest request, HttpServletResponse response, Supplier<CsrfToken> csrfToken) {
    		/*
    		 * Always use XorCsrfTokenRequestAttributeHandler to provide BREACH protection of
    		 * the CsrfToken when it is rendered in the response body.
    		 */
    		this.xor.handle(request, response, csrfToken);
    		/*
    		 * Render the token value to a cookie by causing the deferred token to be loaded.
    		 */
    		csrfToken.get();
    	}

    	@Override
    	public String resolveCsrfTokenValue(HttpServletRequest request, CsrfToken csrfToken) {
    		String headerValue = request.getHeader(csrfToken.getHeaderName());
    		/*
    		 * If the request contains a request header, use CsrfTokenRequestAttributeHandler
    		 * to resolve the CsrfToken. This applies when a single-page application includes
    		 * the header value automatically, which was obtained via a cookie containing the
    		 * raw CsrfToken.
    		 *
    		 * In all other cases (e.g. if the request contains a request parameter), use
    		 * XorCsrfTokenRequestAttributeHandler to resolve the CsrfToken. This applies
    		 * when a server-side rendered form includes the _csrf request parameter as a
    		 * hidden input.
    		 */
    		return (StringUtils.hasText(headerValue) ? this.plain : this.xor).resolveCsrfTokenValue(request, csrfToken);
    	}
    }

    /**
     * C'est pour configurer le CORS
     * @return
     */
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
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
