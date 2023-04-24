package fr.utc.sr03.chat.security;

import fr.utc.sr03.chat.dao.ChatRoomRepository;
import fr.utc.sr03.chat.dao.UserChatroomRelationRepository;
import fr.utc.sr03.chat.dao.UserRepository;
import fr.utc.sr03.chat.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Optional;

@Component
public class AccountAuthenticationProvider implements AuthenticationProvider {
    private static final int MAX_FAILED_ATTEMPTS = 5;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private UserChatroomRelationRepository userChatroomRelationRepository;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String userEmail = authentication.getName();
        String password = authentication.getCredentials().toString();

        Optional<User> admin = userRepository.findByMailAndAdmin(userEmail, true);
        Optional<User> user = userRepository.findByMailAndAdmin(userEmail, false);
        Optional<User> account;

        if (admin.isEmpty() && user.isEmpty()) {
            throw new UsernameNotFoundException("Account not found");
        }else if(admin.isEmpty()) {
            account = user;
        }else{
            account = admin;
        }

        if(!account.get().isActive()){
            throw new LockedException("Account is locked");
        }

        Collection<? extends GrantedAuthority> AUTHORITIES = account.get().getAuthorities();

        if (account.get().getPassword().equals(password)) {
            account.get().setFailedAttempts(0);
            userRepository.save(account.get());
            return new UsernamePasswordAuthenticationToken(userEmail, password, AUTHORITIES);
        } else {
            account.get().setFailedAttempts(account.get().getFailedAttempts() + 1);

            if (account.get().getFailedAttempts() >= MAX_FAILED_ATTEMPTS) {
                account.get().setActive(false);
                /**
                 * change the status of all chatrooms that owned by user to inactive
                 *
                if(!account.get().isAdmin()){
                    userChatroomRelationRepository.findByUserId(account.get().getId()).forEach(userChatroomRelation -> {
                        chatRoomRepository.findById(userChatroomRelation.getChatRoomId()).ifPresent(chatRoom -> {
                            chatRoom.setActive(false);
                            chatRoomRepository.save(chatRoom);
                        });
                    });
                }
                 */
                account.get().setFailedAttempts(0);
            }

            userRepository.save(account.get());
            throw new BadCredentialsException("Wrong Password. Account will be locked after " + (MAX_FAILED_ATTEMPTS - account.get().getFailedAttempts()) + " more attempts");
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}
