package fr.utc.sr03.chat.service.interfaces;

import fr.utc.sr03.chat.dto.UserDTO;
import fr.utc.sr03.chat.model.User;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import javax.servlet.http.HttpServletRequest;
import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

public interface UserServiceInt {

    UserDTO getLoggedUser();
    Page<User> findAllUsersByPage(int page);

    boolean addUser(User user);

    Page<User> findAllUsersNotAdminByPage(int page);

    Page<UserDTO> findAllOtherUsersNotAdminByPage(int page, long userId);

    Page<UserDTO> findUsersInvitedToChatroomByPage(long chatroomId, int page);

    Page<UserDTO> findUsersNotInvitedToChatroomByPage(long chatroomId, int page);

    void deleteUserById(Long id);

    Page<User> findAllInactiveUsersByPage(int page);

    void setStatusOfUser(long userId,boolean status);

    void setFailedAttemptsOfUser(long userId, int failedAttempts);

    void lockUserAndResetFailedAttempts(long userId);

    Optional<User> findUserByEmail(String email);

    Optional<User> findUserById(long userId);

    Optional<User> findUserOrAdmin(String email, boolean isAdmin);

    void sendResetPasswordEmail(User user, HttpServletRequest request);

    void resetPassword(User user, String password);

    boolean checkUserLoginStatus();
}