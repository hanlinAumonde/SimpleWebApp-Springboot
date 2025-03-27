package com.devStudy.chat.security;

import com.devStudy.chat.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.devStudy.chat.service.implementations.UserService;

import static com.devStudy.chat.service.utils.ConstantValues.MAX_FAILED_ATTEMPTS;

import java.util.Collection;

public class AccountAuthenticationProvider implements AuthenticationProvider {

    private final Logger logger = LoggerFactory.getLogger(AccountAuthenticationProvider.class);

    private final PasswordEncoder passwordEncoder;
    private final UserService userService;
    
	public AccountAuthenticationProvider(PasswordEncoder passwordEncoder, UserService userService) {
		this.passwordEncoder = passwordEncoder;
		this.userService = userService;
	}

    /**
     * Ce méthode est appelée par le filtre d'authentification pour authentifier l'utilisateur.
     * L'utilisateur est identifié comme admin/user
     * Si un utilisateur a essayé de se connecter plus de 5 fois sans succès, son compte est bloqué
     */
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        logger.info("Commence l'authentification");
        String userEmail = authentication.getName();
        String password = authentication.getCredentials().toString();
        logger.info("userEmail : {}", userEmail);
        logger.info("password : {}", password);

        UserDetails account = userService.loadUserByUsername(userEmail);

        Collection<? extends GrantedAuthority> AUTHORITIES = account.getAuthorities();
        
        if (passwordEncoder.matches(password, account.getPassword())) {
            userService.resetFailedAttemptsOfUser(account.getUsername());
            User user = userService.findUserOrAdmin(account.getUsername(), false)
                    .orElseThrow(() -> new UsernameNotFoundException("Internal error : " + account.getUsername() + " not found after checking password"));
            return new UsernamePasswordAuthenticationToken(user, password, AUTHORITIES);
        }
        throw FailedLoginAttemptsException(account);
    }
    
    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
    
    private BadCredentialsException FailedLoginAttemptsException(UserDetails account) {
    	int attempts = userService.incrementFailedAttemptsOfUser(account.getUsername());
        if (attempts >= MAX_FAILED_ATTEMPTS) {
            userService.lockUserAndResetFailedAttempts(account.getUsername());
            //TODO: quand un user est bloqué, ses chatrooms vont etre transferees à un autre user
            
            logger.info("Trops de tentatives malveillantes, votre compte est blouqé");
            return new BadCredentialsException("Trops de tentatives malveillantes, votre compte est blouqé");
        }
        logger.info("Mot de passe incorrect. Plus que {} tentatives avant blocage", MAX_FAILED_ATTEMPTS - attempts);
        return new BadCredentialsException("Mot de passe incorrect. Plus que " + (MAX_FAILED_ATTEMPTS - attempts) + " tentatives avant blocage");
    }
}
