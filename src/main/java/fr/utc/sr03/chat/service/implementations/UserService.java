package fr.utc.sr03.chat.service.implementations;

import fr.utc.sr03.chat.dao.ResetPasswordValidateRespository;
import fr.utc.sr03.chat.dao.UserRepository;
import fr.utc.sr03.chat.model.ResetPasswordValidate;
import fr.utc.sr03.chat.model.User;
import fr.utc.sr03.chat.service.interfaces.UserServiceInt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService implements UserServiceInt {

    @Autowired
    @Lazy
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Resource
    private EmailService emailService;

    @Autowired
    private ResetPasswordValidateRespository resetPasswordValidateRespository;

    @Override
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User getLoggedUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @Override
    public boolean addUser(User user) {
        List<User> users = userRepository.findAll();
        for(User u : users){
            if(u.equals(user)){
                return false;
            }
        }
        user.setPwd(passwordEncoder.encode(user.getPwd()));
        userRepository.save(user);
        return true;
    }

    @Override
    public List<User> findAllUsersNotAdmin() {
        return userRepository.findByAdmin(false);
    }

    @Transactional
    @Override
    public void deleteUserById(Long id) {
        User user = userRepository.findById(id).get();
        userRepository.delete(user);
    }

    @Override
    public List<User> findAllInactiveUsers() {
        return userRepository.findByActive(false);
    }

    @Transactional
    @Override
    public void setStatusOfUser(long userId,boolean status) {
        userRepository.updateActive(userId,status);
    }

    @Transactional
    @Override
    public void setFailedAttemptsOfUser(long userId, int failedAttempts) {
        userRepository.updateFailedAttempts(userId,failedAttempts);
    }

    @Transactional
    @Override
    public void lockUserAndResetFailedAttempts(long userId) {
        userRepository.updateActive(userId,false);
        userRepository.updateFailedAttempts(userId,0);
    }

    @Override
    public Optional<User> findUserByEmail(String email) {
        return userRepository.findByMail(email);
    }

    @Override
    public Optional<User> findUserById(long userId) {
        return userRepository.findById(userId);
    }

    @Override
    public Optional<User> findUserOrAdmin(String email, boolean isAdmin) {
        return userRepository.findByMailAndAdmin(email, isAdmin);
    }

    @Override
    public void sendResetPasswordEmail(User user, HttpServletRequest request) {
        String token = UUID.randomUUID().toString();
        ResetPasswordValidate resetPasswordValidate = new ResetPasswordValidate(token, user);
        resetPasswordValidateRespository.save(resetPasswordValidate);
        String ResetPasswordLink = "http://" + request.getServerName() + ":" + request.getServerPort()
                + "/reset-password/reset-password-form?token=" + token;
        String subject = "Reset Password";
        String content = "Bonjour " + user.getFirstName() + ",\n\n"
                + "Cliquer sur le lien ci-dessous pour réinitialiser votre mot de passe :\n"
                + ResetPasswordLink + "\n\n"
                + "Attention : ce lien n'est valide que pendant 1 heure\n\n"
                + "Bien cordialement,\n"
                + "Chat Team";
        emailService.sendSimpleMessage(user.getMail(), subject, content);
    }

    @Transactional
    @Override
    public void resetPassword(User user, String password) {
        userRepository.updatePwd(user.getId(), passwordEncoder.encode(password));
    }

    @Override
    public boolean checkUserLoginStatus() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken));
    }

}
