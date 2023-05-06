package fr.utc.sr03.chat.service.interfaces;

import fr.utc.sr03.chat.model.User;
import java.util.List;
import java.util.Optional;

public interface UserServiceInt {
    List<User> findAllUsers();
    boolean addUser(User user);
    List<User> findAllUsersNotAdmin();
    void deleteUserById(Long id);
    List<User> findAllInactiveUsers();
    void setStatusOfUser(long userId,boolean status);
    void setFailedAttemptsOfUser(long userId, int failedAttempts);
    void lockUserAndResetFailedAttempts(long userId);
    Optional<User> findUserOrAdmin(String email, boolean isAdmin);
}