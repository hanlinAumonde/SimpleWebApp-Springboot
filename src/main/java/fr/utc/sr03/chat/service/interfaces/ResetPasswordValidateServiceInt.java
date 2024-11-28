package fr.utc.sr03.chat.service.interfaces;

import fr.utc.sr03.chat.model.ResetPasswordValidate;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ResetPasswordValidateServiceInt {
    Optional<ResetPasswordValidate> findValidateByToken(UUID token);

    List<ResetPasswordValidate> findValidatesByUserId(long userId);

    void deleteValidate(ResetPasswordValidate resetPasswordValidate);
}
