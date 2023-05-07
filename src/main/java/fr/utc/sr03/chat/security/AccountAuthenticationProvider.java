package fr.utc.sr03.chat.security;

import fr.utc.sr03.chat.model.User;
import fr.utc.sr03.chat.service.implementations.ChatroomService;
import fr.utc.sr03.chat.service.implementations.UserChatroomRelationService;
import fr.utc.sr03.chat.service.implementations.UserService;
import fr.utc.sr03.chat.service.utils.WithoutPasswordEncorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.Optional;

@Component
public class AccountAuthenticationProvider implements AuthenticationProvider {

    private final Logger logger = LoggerFactory.getLogger(AccountAuthenticationProvider.class);
    private static final int MAX_FAILED_ATTEMPTS = 5;
    @Autowired
    @Lazy
    private PasswordEncoder passwordEncoder;

    @Resource
    private UserService userService;

    @Resource
    private ChatroomService chatroomService;

    @Resource
    private UserChatroomRelationService userChatroomRelationService;

    /*
      Ce méthode est appelée par le filtre d'authentification pour authentifier l'utilisateur.
    */
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        logger.info("Commence l'authentification");
        String userEmail = authentication.getName();
        String password = authentication.getCredentials().toString();
        logger.info("userEmail : " + userEmail);
        logger.info("password : " + password);

        Optional<User> admin = userService.findUserOrAdmin(userEmail, true);
        Optional<User> user = userService.findUserOrAdmin(userEmail, false);
        Optional<User> account;

        if (admin.isEmpty() && user.isEmpty()) {
            logger.info("Account not found");
            throw new UsernameNotFoundException("Account not found");
        }else if(admin.isEmpty()) {
            account = user;
        }else{
            account = admin;
        }

        if(!account.get().isActive()){
            logger.info("Account is not active");
            throw new LockedException("Account is locked");
        }

        Collection<? extends GrantedAuthority> AUTHORITIES = account.get().getAuthorities();

        if (passwordEncoder.matches(password, account.get().getPassword())) {
            userService.setFailedAttemptsOfUser(account.get().getId(),0);
            return new UsernamePasswordAuthenticationToken(account.get(), password, AUTHORITIES);
        } else {
            int attempts = account.get().getFailedAttempts() + 1;
            if (attempts >= MAX_FAILED_ATTEMPTS) {
                userService.lockUserAndResetFailedAttempts(account.get().getId());
                /*
                  modifier le status de tous les chatrooms appartenant(non invité) à cet utilisateur à false
                */
                if(!account.get().isAdmin()){
                    userChatroomRelationService.findRelationsOfUser(account.get().getId())
                            .forEach(userChatroomRelation ->
                                    chatroomService.setStatusOfChatroom(userChatroomRelation.getChatroomId(),false));
                }
                logger.info("Trops de tentatives malveillantes, votre compte est blouqé");
                throw new BadCredentialsException("Trops de tentatives malveillantes, votre compte est blouqé");
            }
            userService.setFailedAttemptsOfUser(account.get().getId(),attempts);
            logger.info("Wrong Password. Account will be locked after " + (MAX_FAILED_ATTEMPTS - attempts) + " more attempts");
            throw new BadCredentialsException("Wrong Password. Account will be locked after " + (MAX_FAILED_ATTEMPTS - attempts) + " more attempts");
        }
    }
    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}
