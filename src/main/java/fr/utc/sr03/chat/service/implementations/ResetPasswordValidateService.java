package fr.utc.sr03.chat.service.implementations;

import fr.utc.sr03.chat.dao.ResetPasswordValidateRespository;
import fr.utc.sr03.chat.dao.UserRepository;
import fr.utc.sr03.chat.model.ResetPasswordValidate;
import fr.utc.sr03.chat.model.User;
import fr.utc.sr03.chat.service.interfaces.ResetPasswordValidateServiceInt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ResetPasswordValidateService implements ResetPasswordValidateServiceInt {

    @Autowired
    private ResetPasswordValidateRespository resetPasswordValidateRespository;
    
    /**
     * Cette méthode permet de trouver le validate correspondant au token passé en paramètre
     */
    @Override
    public Optional<ResetPasswordValidate> findValidateByToken(UUID token) {
        return resetPasswordValidateRespository.findByToken(token);
    }

    @Override
    public List<ResetPasswordValidate> findValidatesByUserId(long userId) {
        return resetPasswordValidateRespository.findByUserId(userId);
    }

    /**
     * Cette méthode permet de suppimer un validate après avoir changé le mot de passe
     */
    @Override
    @Transactional
    public void deleteValidate(ResetPasswordValidate resetPasswordValidate) {
    	resetPasswordValidate.getUser().getResetPasswordValidates().remove(resetPasswordValidate);
    	resetPasswordValidate.setUser(null);
    	
        resetPasswordValidateRespository.delete(resetPasswordValidate);
    }
}
