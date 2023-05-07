package fr.utc.sr03.chat.service.interfaces;

import fr.utc.sr03.chat.model.ResetPasswordValidate;

import javax.swing.text.html.Option;
import java.util.Optional;

public interface ResetPasswordValidateServiceInt {
    Optional<ResetPasswordValidate> findValidateByToken(String token);

    void deleteValidate(ResetPasswordValidate resetPasswordValidate);
}
