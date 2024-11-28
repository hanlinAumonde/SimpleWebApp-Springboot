package fr.utc.sr03.chat.dao;

import fr.utc.sr03.chat.model.ResetPasswordValidate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ResetPasswordValidateRespository extends JpaRepository<ResetPasswordValidate, Long> {

    Optional<ResetPasswordValidate> findByToken(UUID token);

    List<ResetPasswordValidate> findByUserId(long userId);
}
