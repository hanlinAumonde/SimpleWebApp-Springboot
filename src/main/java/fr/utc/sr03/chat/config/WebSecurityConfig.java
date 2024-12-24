package fr.utc.sr03.chat.config;

import fr.utc.sr03.chat.security.AccountAuthenticationFailureHandler;
import fr.utc.sr03.chat.security.AccountAuthenticationProvider;
import fr.utc.sr03.chat.security.AccountAuthenticationSuccessHandler;
import fr.utc.sr03.chat.security.AccountLogoutSuccessHandler;
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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;


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

        http    .cors()
                .and()
                .csrf()
                    .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .and()
                .authorizeRequests()
                    .antMatchers("/logout").permitAll()
                    .antMatchers("/reset-password/**").permitAll()
                    .antMatchers("/user/**").hasRole("USER")
                    .antMatchers("/users/logged").permitAll()
                    .antMatchers("ws://localhost:8080/**").hasRole("USER")
                    .anyRequest().authenticated()
                .and()
                .formLogin()
                    //.loginPage("/login")
                    .loginProcessingUrl("/login-check")
                    .successHandler(new AccountAuthenticationSuccessHandler())
                    .failureHandler(new AccountAuthenticationFailureHandler())
                    .permitAll()
                .and()
                .logout()
                    .logoutUrl("/logout")
                    .invalidateHttpSession(true)
                    .clearAuthentication(true)
                    .logoutSuccessHandler(new AccountLogoutSuccessHandler())
                    .permitAll();

        return http.build();
    }



    /**
     * C'est pour encoder le mot de passe
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * C'est pour ignorer les fichiers statiques
     */
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().antMatchers("/css/**", "/js/**");
    }


    /**
     * C'est pour activer le WebSocket
     */
    @Bean
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }


}
