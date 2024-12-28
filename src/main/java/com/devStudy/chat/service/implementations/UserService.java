package com.devStudy.chat.service.implementations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devStudy.chat.dao.ResetPasswordValidateRespository;
import com.devStudy.chat.dao.UserRepository;
import com.devStudy.chat.dto.CreateCompteDTO;
import com.devStudy.chat.dto.DTOMapper;
import com.devStudy.chat.dto.UserDTO;
import com.devStudy.chat.model.ResetPasswordValidate;
import com.devStudy.chat.model.User;
import com.devStudy.chat.service.interfaces.UserServiceInt;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import static com.devStudy.chat.service.utils.ConstantValues.DefaultPageSize_Users;
import static com.devStudy.chat.service.utils.ConstantValues.CreationSuccess;
import static com.devStudy.chat.service.utils.ConstantValues.CompteExist;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService implements UserServiceInt {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);
	
	@Value("${FrontEndURL}")
	private String FrontEndURL;

    @Autowired
    @Lazy
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Resource
    private EmailService emailService;

    @Autowired
    private ResetPasswordValidateRespository resetPasswordValidateRespository;
    
    private Pageable getPageableSetting(int page) {
    	var sortConds = Sort.sort(User.class).by(User::getFirstName).ascending()
	    			.and(Sort.sort(User.class).by(User::getLastName).ascending());
    	return PageRequest.of(page, DefaultPageSize_Users, sortConds);
    }

    /**
     * Cette méthode permet de trouver tous les utilisateurs en page
     */
    @Override
    public Page<User> findAllUsersByPage(int page) {
        return userRepository.findAll(this.getPageableSetting(page));
    }

    /**
     * Cette méthode permet de récupérer un utilisateur connecté
     */
    @Override
    public UserDTO getLoggedUser() {
        return DTOMapper.toUserDTO(
        	(User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()
        );
    }

    /**
     * Cette méthode permet d'ajouter un utilisateur
     * Si l'utilisateur existe déjà, on retourne false, sinon on l'ajoute et on retourne true
     */
    @Transactional
    @Override
    public CreateCompteDTO addUser(CreateCompteDTO user) {
        List<User> users = userRepository.findAll();
        for(User u : users){
            if(u.getMail() == user.getMail()){
                user.setCreateMsg(CompteExist);
                return user;
            }
        }
        User newUser = new User();
        newUser.setFirstName(user.getFirstName());
        newUser.setLastName(user.getLastName());
        newUser.setMail(user.getMail());
        newUser.setPwd(passwordEncoder.encode(user.getPassword()));
        newUser.setAdmin(false);
        userRepository.save(newUser);
        user.setCreateMsg(CreationSuccess);
        return user;
    }

    /**
     * Cette méthode permet de trouver les utilisateurs qui ne sont pas administrateurs
     * Elle est utilisée dans la page d'administration - suppression d'un utilisateur
     */
    @Override
    public Page<User> findAllUsersNotAdminByPage(int page){
        return userRepository.findByAdmin(false,this.getPageableSetting(page));
    }

    /**
     * Cette méthode permet de trouver les utilisateurs qui ne sont pas administrateurs et qui ne sont pas l'utilisateur connecté
     * Elle est utilisée dans la page User pour donner l'utilisateur une liste des utilisateurs qu'il peut ajouter à un chatroom
     */
    @Override
    public Page<UserDTO> findAllOtherUsersNotAdminByPage(int page, long userId){
        return userRepository.findAllOtherUsersNotAdminByPage(userId,this.getPageableSetting(page))
        		.map(DTOMapper::toUserDTO);
    }

    /**
     * Cette méthode permet de trouver les utilisateurs invités à un chatroom
     */
    @Override
    public Page<UserDTO> findUsersInvitedToChatroomByPage(long chatroomId, int page) {
        return userRepository.findUsersInvitedToChatroomByPage(chatroomId,this.getPageableSetting(page))
        		.map(DTOMapper::toUserDTO);
    }

    /**
     * Cette méthode permet de trouver les utilisateurs qui ne sont pas invités à un chatroom
     */
    @Override
    public Page<UserDTO> findUsersNotInvitedToChatroomByPage(long chatroomId, long userId, int page) {
        return userRepository.findUsersNotInvitedToChatroomByPage(chatroomId, userId, this.getPageableSetting(page))
        		.map(DTOMapper::toUserDTO);
    }

    /**
     * Cette méthode permet de supprimer un utilisateur
     */
    @Transactional
    @Override
    public void deleteUserById(Long id) {
        User user = userRepository.findById(id).get();
        userRepository.delete(user);
    }

    /**
     * Cette méthode permet de trouver tous les utilisateurs désactivés
     */
    @Override
    public Page<User> findAllInactiveUsersByPage(int page) {
        return userRepository.findByActive(false,this.getPageableSetting(page));
    }

    /**
     * Cette méthode permet de mise à jour le statut d'un utilisateur
     */
    @Transactional
    @Override
    public void setStatusOfUser(long userId,boolean status) {
        userRepository.updateActive(userId,status);
    }

    /**
     * Cette méthode permet de mise à jour le nombre d'essais de connexion d'un utilisateur
     */
    @Transactional
    @Override
    public void setFailedAttemptsOfUser(long userId, int failedAttempts) {
        userRepository.updateFailedAttempts(userId,failedAttempts);
    }

    /**
     * Cette méthode permet de bloquer un utilisateur et de réinitialiser le nombre d'essais de connexion
     */
    @Transactional
    @Override
    public void lockUserAndResetFailedAttempts(long userId) {
        userRepository.updateActive(userId,false);
        userRepository.updateFailedAttempts(userId,0);
    }

    /**
     * Cette méthode permet de trouver un utilisateur par son email
     */
    @Override
    public Optional<User> findUserByEmail(String email) {
        return userRepository.findByMail(email);
    }

    /**
     * Cette méthode permet de trouver un utilisateur par son id
     */
    @Override
    public Optional<User> findUserById(long userId) {
        return userRepository.findById(userId);
    }

    /**
     * Cette méthode permet de trouver un utilisateur qui est administrateur ou non
     */
    @Override
    public Optional<User> findUserOrAdmin(String email, boolean isAdmin) {
        return userRepository.findByMailAndAdmin(email, isAdmin);
    }

    /**
     * Cette méthode permet de construire un email de réinitialisation de mot de passe et de l'envoyer à l'utilisateur
     */
    @Override
    public void sendResetPasswordEmail(User user, HttpServletRequest request) {
        UUID token = UUID.randomUUID();
        ResetPasswordValidate resetPasswordValidate = new ResetPasswordValidate(token, user);
        resetPasswordValidateRespository.save(resetPasswordValidate);
        String ResetPasswordLink = FrontEndURL
                + "/reset-password?token=" + token.toString();
        LOGGER.info("Reset Password Link : " + ResetPasswordLink);
        String subject = "Reset Password";
        String content = "Bonjour " + user.getFirstName() + ",\n\n"
                + "Cliquer sur le lien ci-dessous pour réinitialiser votre mot de passe :\n"
                + ResetPasswordLink + "\n\n"
                + "Attention : ce lien n'est valide que pendant 1 heure\n\n"
                + "Bien cordialement,\n"
                + "Chat Team";
        emailService.sendSimpleMessage(user.getMail(), subject, content);
    }

    /**
     * Cette méthode permet de réinitialiser le mot de passe d'un utilisateur
     */
    @Transactional
    @Override
    public void resetPassword(User user, String password) {
        userRepository.updatePwd(user.getId(), passwordEncoder.encode(password));
    }

    /**
     * Cette méthode permet de vérifier si un utilisateur est encore connecté
     */
    @Override
    public boolean checkUserLoginStatus() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null &&
                auth.isAuthenticated() &&
                !(auth instanceof AnonymousAuthenticationToken)) &&
                userRepository.findById(((User) auth.getPrincipal()).getId()).isPresent();
    }

}
