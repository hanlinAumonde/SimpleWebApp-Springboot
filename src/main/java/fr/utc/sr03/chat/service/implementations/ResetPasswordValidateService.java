package fr.utc.sr03.chat.service.implementations;

import fr.utc.sr03.chat.dao.ResetPasswordValidateRespository;
import fr.utc.sr03.chat.model.ResetPasswordValidate;
import fr.utc.sr03.chat.service.interfaces.ResetPasswordValidateServiceInt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ResetPasswordValidateService implements ResetPasswordValidateServiceInt {

    @Autowired
    private ResetPasswordValidateRespository resetPasswordValidateRespository;
    @Override
    public Optional<ResetPasswordValidate> findValidateByToken(String token) {
        return resetPasswordValidateRespository.findByToken(token);
    }

    @Override
    public void deleteValidate(ResetPasswordValidate resetPasswordValidate) {
        resetPasswordValidateRespository.delete(resetPasswordValidate);
    }
}
