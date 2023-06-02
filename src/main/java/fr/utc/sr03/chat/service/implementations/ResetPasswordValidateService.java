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
    /**
     * Cette méthode permet de trouver le validate correspondant au token passé en paramètre
     */
    @Override
    public Optional<ResetPasswordValidate> findValidateByToken(String token) {
        return resetPasswordValidateRespository.findByToken(token);
    }

    /**
     * Cette méthode permet de suppimer un validate après avoir changé le mot de passe
     */
    @Override
    public void deleteValidate(ResetPasswordValidate resetPasswordValidate) {
        resetPasswordValidateRespository.delete(resetPasswordValidate);
    }
}
