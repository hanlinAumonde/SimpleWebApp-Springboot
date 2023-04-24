package fr.utc.sr03.chat.dao;

import fr.utc.sr03.chat.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByMail(String mail);

    Optional<User> findByMailAndAdmin(String mail, boolean isAdmin);

    List<User> findByAdmin(boolean isAdmin);

    //List<User> findByAdminAndActive(boolean isAdmin, boolean isActive);

    List<User> findByActive(boolean isActive);
}
