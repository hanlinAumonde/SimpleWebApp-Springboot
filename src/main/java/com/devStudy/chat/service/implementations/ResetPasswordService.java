package com.devStudy.chat.service.implementations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devStudy.chat.dao.ResetPasswordValidateRespository;
import com.devStudy.chat.dto.ResponseDTO;
import com.devStudy.chat.model.ResetPasswordValidate;
import com.devStudy.chat.model.User;
import com.devStudy.chat.service.interfaces.ResetPasswordServiceInt;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

@Service
public class ResetPasswordService implements ResetPasswordServiceInt {
	

    @Autowired
    private ResetPasswordValidateRespository resetPasswordValidateRespository;
    
    @Autowired
    private UserService userService;
    
    @Override
	public ResponseDTO sendResetPasswordEmail(String email, HttpServletRequest request) {
    	Optional<User> userFound = userService.findUserByEmail(email);
    	ResponseDTO res = new ResponseDTO();
		if (userFound.isPresent()) {
			User user = userFound.get();
			userService.sendResetPasswordEmail(user, request);
			res.setStatus("scuccess");
			res.setMsg("un mail de réinitialisation de mot de passe a été envoyé à l'adresse " + email
					+ ", veuillez cliquer sur le lien contenu dans le mail pour réinitialiser votre mot de passe");
		} else {
			res.setStatus("error");
			res.setMsg("aucun utilisateur n'est enregistré avec l'adresse " + email + ", veuillez réessayer");
		}
		return res;
	}
    
    /**
     * Cette méthode permet de trouver le validate correspondant au token passé en paramètre
     */
    @Override
    public Optional<ResetPasswordValidate> findValidateByToken(String token) {
    	UUID tokenUUID = UUID.fromString(token);
        return resetPasswordValidateRespository.findByToken(tokenUUID);
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

	@Override
	public boolean validateToken(String token) {
		Optional<ResetPasswordValidate> resetPasswordValidate = findValidateByToken(token);
		if (resetPasswordValidate.isPresent()) {
			return !resetPasswordValidate.get().isExpired();
		}
		return false;
	}

	@Transactional
	@Override
	public boolean resetPassword(String token, String password) {
		Optional<ResetPasswordValidate> resetPasswordValidate = findValidateByToken(token);
		if(resetPasswordValidate.isPresent()) {
			if(resetPasswordValidate.get().isExpired()) {
				return false;
			}else {
				User user = resetPasswordValidate.get().getUser();
				userService.resetPassword(user, password);
				deleteValidate(resetPasswordValidate.get());
				return true;
			}
		}
		return false;
	}
}
