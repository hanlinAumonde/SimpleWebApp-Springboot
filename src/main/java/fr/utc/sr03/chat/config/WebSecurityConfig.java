package fr.utc.sr03.chat.config;

import fr.utc.sr03.chat.security.AccountAuthenticationFailureHandler;
import fr.utc.sr03.chat.security.AccountAuthenticationProvider;
import fr.utc.sr03.chat.security.AccountAuthenticationSuccessHandler;
import fr.utc.sr03.chat.security.AccountLogoutSuccessHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;


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
                //.csrf().disable()
                .csrf()
                    .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .and()
                .authorizeRequests()
                    .antMatchers("/login").permitAll()
                    .antMatchers("/logout").permitAll()
                    .antMatchers("/reset-password/**").permitAll()
                    .antMatchers("/admin/**").hasRole("ADMIN")
                    .antMatchers("/user/**").hasRole("USER")
                    .antMatchers("/logged_user").permitAll()
                    .anyRequest().authenticated()
                .and()
                .formLogin()
                    .loginPage("/login")
                    .loginProcessingUrl("/check")
                    .successHandler(new AccountAuthenticationSuccessHandler())
                    .failureHandler(new AccountAuthenticationFailureHandler())
                    .permitAll()
                .and()
                .logout()
                    .logoutUrl("/logout")
                    .invalidateHttpSession(true)
                    .clearAuthentication(true)
                    .logoutSuccessHandler(new AccountLogoutSuccessHandler())
                    .permitAll()
                .and()
                .sessionManagement()
                    .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                    .maximumSessions(1)
                    .expiredUrl("/login")
                    .maxSessionsPreventsLogin(true);

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
}
