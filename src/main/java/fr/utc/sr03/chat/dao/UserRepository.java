package fr.utc.sr03.chat.dao;

import fr.utc.sr03.chat.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByMailAndAdmin(String mail, boolean isAdmin);

    List<User> findByAdmin(boolean isAdmin);

    List<User> findByActive(boolean isActive);

    @Modifying
    @Query("update User u set u.active = ?2 where u.id = ?1")
    void updateActive(long userId, boolean status);

    @Modifying
    @Query("update User u set u.failedAttempts = ?2 where u.id = ?1")
    void updateFailedAttempts(long userId, int failedAttempts);

    @Modifying
    @Query("update User u set u.pwd = ?2 where u.id = ?1")
    void updatePwd(long userId, String pwd);

    Optional<User> findByMail(String email);
}
