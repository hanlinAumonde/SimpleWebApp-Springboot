package fr.utc.sr03.chat.dao;

import fr.utc.sr03.chat.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByMailAndAdmin(String mail, boolean isAdmin);

    List<User> findByAdmin(boolean isAdmin);
    Page<User> findByAdmin(boolean isAdmin, Pageable pageable);

    //List<User> findByActive(boolean isActive);
    Page<User> findByActive(boolean isActive, Pageable pageable);

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

    @Query("select u from User u where u.id <> ?1 and u.admin = false")
    Page<User> findAllOtherUsersNotAdminByPage(long userId, Pageable pageable);

    @Query("select u from User u, UserChatroomRelation r where u.id = r.userId and r.chatroomId = ?1 and r.owned = false")
    Page<User> findUsersInvitedToChatroomByPage(long chatroomId, Pageable pageable);

    @Query("select u from User u where u.admin <> true and u.id not in (select r.userId from UserChatroomRelation r where r.chatroomId = ?1)")
    Page<User> findUsersNotInvitedToChatroomByPage(long chatroomId, Pageable pageable);
}
