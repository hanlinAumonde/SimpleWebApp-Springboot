package fr.utc.sr03.chat.dao;

import fr.utc.sr03.chat.model.ResetPasswordValidate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ResetPasswordValidateRespository extends JpaRepository<ResetPasswordValidate, Long> {

    Optional<ResetPasswordValidate> findByToken(String token);
}
