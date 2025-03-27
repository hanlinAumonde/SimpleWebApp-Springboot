package com.devStudy.chat.service.implementations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.mail.MailException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devStudy.chat.dao.UserRepository;
import com.devStudy.chat.dto.CreateCompteDTO;
import com.devStudy.chat.dto.DTOMapper;
import com.devStudy.chat.dto.UserDTO;
import com.devStudy.chat.model.User;
import com.devStudy.chat.service.interfaces.UserServiceInt;

import java.util.*;

import static com.devStudy.chat.service.utils.ConstantValues.*;

@Service
public class UserService implements UserServiceInt, UserDetailsService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);
	
	@Value("${chatroomApp.FrontEndURL}")
	private String FrontEndURL;

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final JwtTokenService tokenService;
    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public UserService(PasswordEncoder passwordEncoder, UserRepository userRepository, EmailService emailService, JwtTokenService tokenService, RabbitTemplate rabbitTemplate) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.tokenService = tokenService;
        this.rabbitTemplate = rabbitTemplate;
    }
    
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
            if(Objects.equals(u.getMail(), user.getMail())){
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
     * Cette méthode permet de mise à jour le nombre d'essais de connexion d'un utilisateur
     */
    @Transactional
    @Override
    public int incrementFailedAttemptsOfUser(String userEmail) throws NoSuchElementException {
    	int failedAttempts = findUserOrAdmin(userEmail, false).orElseThrow().getFailedAttempts();
        userRepository.updateFailedAttempts(userEmail,failedAttempts+1);
        return failedAttempts+1;
    }

    /**
     * Cette méthode permet de bloquer un utilisateur et de réinitialiser le nombre d'essais de connexion
     */
    @Transactional
    @Override
    public void lockUserAndResetFailedAttempts(String userEmail) {
        userRepository.updateActive(userEmail,false);
        resetFailedAttemptsOfUser(userEmail);
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

    @Override
    public Map<String,String> sendResetPwdEmailRequestToMQ(String email){
        Map<String,String> map = new HashMap<>();
        try{
            rabbitTemplate.send(RABBITMQ_EXCHANGE_NAME,ROUTING_KEY_RET_PASSWORD,new Message(email.getBytes("UTF-8")));
            map.put("status","request-sent");
            map.put("msg", """
                            Demande envoyée, si vous avez un compte avec cet email,
                            vous recevrez un email de réinitialisation de mot de passe
                            Veuillez reessayer dans 60s si vous n'avez pas reçu l'email
                          """);
        }catch (Exception e){
            LOGGER.error("sendResetPwdEmailRequestToMQ error",e);
            map.put("status","error");
            map.put("msg","Erreur lors de l'envoi de la demande de réinitialisation de mot de passe");
        }
        return map;
    }

    /**
     * Cette méthode permet d'envoyer un mail de réinitialisation de mot de passe
     */
    @RabbitListener(queues = RABBITMQ_QUEUE_Q1)
    public void sendResetPasswordEmail(String email) {
    	try {
	    	if(findUserOrAdmin(email, false).isPresent()) {
	    		String jwtToken = tokenService.generateJwtToken(email);
	    		String ResetPasswordLink = String.format("%s/reset-password?token=%s", FrontEndURL, jwtToken);
                LOGGER.info("Reset Password Link : {}", ResetPasswordLink);
	            String subject = "Reset Password";
	            String content = String.format(
                        """
                                Bonjour,
                                
                                Cliquer sur le lien ci-dessous pour réinitialiser votre mot de passe :
                                %s
                                
                                Attention : ce lien n'est valide que pendant une demi-heure
                                
                                Bien cordialement,
                                Chat Team"""
	            		, ResetPasswordLink);
	            emailService.sendSimpleMessage(email, subject, content);
	    	}
		}catch (MailException mailException) {
            LOGGER.error("Error while sending email to {}", email, mailException);
        }
    }

    /**
     * Cette méthode permet de réinitialiser le mot de passe d'un utilisateur
     */
    @Transactional
    @Override
    public boolean resetPassword(String jwtToken, String password) {
		String email = tokenService.validateTokenAndGetEmail(jwtToken);
		if (!email.isEmpty()) {
			userRepository.updatePwd(email, passwordEncoder.encode(password));
			return true;
		}
		return false;
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

	@Override
	public UserDetails loadUserByUsername(String userEmail) throws UsernameNotFoundException {
		Optional<User> account = findUserOrAdmin(userEmail, false);
		if (account.isEmpty()) {
			LOGGER.info("Identifiants incorrects");
			throw new UsernameNotFoundException("Identifiants incorrects");
		}

        if(!account.get().isActive()){
            LOGGER.info("Compté bloqué");
            throw new UsernameNotFoundException("Compté bloqué");
        }
        return account.get();
	}

	@Transactional
	@Override
	public void resetFailedAttemptsOfUser(String username) {
        userRepository.updateFailedAttempts(username, 0);		
	}

}
