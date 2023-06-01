package fr.utc.sr03.chat.service.interfaces;

import fr.utc.sr03.chat.model.User;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import javax.servlet.http.HttpServletRequest;
import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

public interface UserServiceInt {

    User getLoggedUser();
    //List<User> findAllUsers();
    Page<User> findAllUsersByPage(int page, int size);

    boolean addUser(User user);

    //List<User> findAllUsersNotAdmin();
    Page<User> findAllUsersNotAdminByPage(int page, int size);

    Page<User> findAllOtherUsersNotAdminByPage(int page, int size, long userId);

    Page<User> findUsersInvitedToChatroomByPage(long chatroomId, int page, int size);

    Page<User> findUsersNotInvitedToChatroomByPage(long chatroomId, int page, int size);

    void deleteUserById(Long id);

    //List<User> findAllInactiveUsers();
    Page<User> findAllInactiveUsersByPage(int page, int size);

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