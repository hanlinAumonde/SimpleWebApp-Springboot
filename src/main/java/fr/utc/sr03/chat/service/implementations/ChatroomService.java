package fr.utc.sr03.chat.service.implementations;

import fr.utc.sr03.chat.dao.ChatroomRepository;
import fr.utc.sr03.chat.dao.UserChatroomRelationRepository;
import fr.utc.sr03.chat.dao.UserRepository;
import fr.utc.sr03.chat.dto.ChatroomDTO;
import fr.utc.sr03.chat.dto.ChatroomRequestDTO;
import fr.utc.sr03.chat.dto.ChatroomWithOwnerAndStatusDTO;
import fr.utc.sr03.chat.dto.DTOMapper;
import fr.utc.sr03.chat.dto.ModifyChatroomDTO;
import fr.utc.sr03.chat.dto.ModifyChatroomRequestDTO;
import fr.utc.sr03.chat.dto.UserDTO;
import fr.utc.sr03.chat.model.Chatroom;
import fr.utc.sr03.chat.model.User;
import fr.utc.sr03.chat.model.UserChatroomRelation;
import fr.utc.sr03.chat.service.interfaces.ChatroomServiceInt;
import fr.utc.sr03.chat.service.utils.Events.ChangeChatroomMemberEvent;
import fr.utc.sr03.chat.service.utils.Events.RemoveChatroomEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ChatroomService implements ChatroomServiceInt {

    private final Logger logger = LoggerFactory.getLogger(ChatroomService.class);

    @Resource
    private UserChatroomRelationService userChatroomRelationService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ChatroomRepository chatRoomRepository;

    @Autowired
    private UserChatroomRelationRepository userChatroomRelationRepository;
    
    @Autowired
    private ApplicationEventPublisher publisher;

    /**
     * Cette méthode permet de trouver le chatroom correspondant à l'id passé en paramètre
     */
    @Override
    public Optional<ModifyChatroomDTO> findChatroom(long chatroomId) {
    	Optional<ModifyChatroomDTO> chatroom = chatRoomRepository.findById(chatroomId).map(DTOMapper::toModifyChatroomDTO);
        return chatroom;
    }

    /**
     * Cette méthode permet de créer un chatroom,
     * si il y a des conflits avec un chatroom existant, on retourne un chatroom vide et affichier un message d'erreur
     */
    @Transactional
    @Override
    public boolean createChatroom(ChatroomRequestDTO chatroomRequestDTO, long userId) {
        try {
            Chatroom chatroom = new Chatroom();
            chatroom.setTitre(chatroomRequestDTO.getTitre());
            chatroom.setDescription(chatroomRequestDTO.getDescription());
            chatroom.setActive(true);

            LocalDateTime dateStart = LocalDateTime.parse(chatroomRequestDTO.getStartDate());
            LocalDateTime dateEnd = dateStart.plusDays(
                    chatroomRequestDTO.getDuration() > 0 ? chatroomRequestDTO.getDuration() : 1
            );
            chatroom.setHoraireCommence(dateStart);
            chatroom.setHoraireTermine(dateEnd);

            List<Chatroom> allChatrooms = chatRoomRepository.findAll();
            for (Chatroom c : allChatrooms) {
                if (c.equals(chatroom)) {
                    return false;
                }
            }
            chatRoomRepository.save(chatroom);
            Chatroom chatroomAdded = chatRoomRepository.findByTitreAndDescriptionAndHoraireCommenceAndHoraireTermine(
                    chatroomRequestDTO.getTitre(),
                    chatroomRequestDTO.getDescription(),
                    dateStart,
                    dateEnd
            ).get();
            userChatroomRelationService.addRelation(userId, chatroomAdded.getId(), true);
            for (UserDTO user : chatroomRequestDTO.getUsersInvited()) {
                userChatroomRelationService.addRelation(user.getId(), chatroomAdded.getId(), false);
            }
            return true;
        } catch (Exception e) {
            logger.error("Error while creating chatroom : " + e.getMessage());
            return false;
        }
    }

    /**
     * Cette méthode permet de trouver les chatrooms crées/joints par un utilisateur en Page(size = 5)
     */
    @Transactional(readOnly = true)
    private Page<Chatroom> getChatroomsOwnedOrJoinedOfUserByPage(long userId, boolean isOwner, int page, int size) {
        Pageable pageable = PageRequest.of(page,size, Sort.sort(Chatroom.class).by(Chatroom::getTitre).ascending());
        return chatRoomRepository.findChatroomsOwnedOrJoinedOfUserByPage(userId,isOwner,pageable);
    }
    
    @Override
	public Page<ChatroomDTO> getChatroomsOwnedOfUserByPage(long userId, int page, int size) {
		Page<Chatroom> chatrooms = getChatroomsOwnedOrJoinedOfUserByPage(userId, true, page, size);
		return chatrooms.map(chatroom -> {
			return DTOMapper.toChatroomDTO(chatroom, chatroom.isActive() && !chatroom.hasNotStarted());
		});
	}
    
    @Transactional(readOnly = true)
    @Override
	public Page<ChatroomWithOwnerAndStatusDTO> getChatroomsJoinedOfUserByPage(long userId, boolean isOwner, int page, int size) {
    	Page<Chatroom> chatrooms = getChatroomsOwnedOrJoinedOfUserByPage(userId, isOwner, page, size);
    	return chatrooms.map(chatroom -> {
    		User owner = userRepository.findById(
    				userChatroomRelationService.findOwnerOfChatroom(chatroom.getId()).get().getUserId()
    		).get();
    		// etape 2 : recuperer le status du chatroom
    		boolean isActive = chatroom.isActive() && !chatroom.hasNotStarted();
    		return DTOMapper.toChatroomWithOwnerAndStatusDTO(chatroom, owner, isActive);
    	});
    }

    /**
     * Cette méthode permet de trouver les chatrooms crées/joints par un utilisateur
     */
    @Transactional(readOnly = true)
    @Override
    public List<UserDTO> getAllUsersInChatroom(long chatroomId){
        List<User> allUsersInChatroom = new ArrayList<>();
        for(UserChatroomRelation user : userChatroomRelationRepository.findByChatroomId(chatroomId)){
            userRepository.findById(user.getUserId()).ifPresent(allUsersInChatroom::add);
        }
        return allUsersInChatroom.stream().map(DTOMapper::toUserDTO).toList();
    }

    /**
     * Cette méthode permet de supprimer un chatroom
     */
    @Transactional
    @Override
    public boolean deleteChatRoom(long chatroomId) {
        try{
            userChatroomRelationRepository.deleteAll(userChatroomRelationRepository.findByChatroomId(chatroomId));
            chatRoomRepository.deleteById(chatroomId);
            publisher.publishEvent(new RemoveChatroomEvent(chatroomId));
            return true;
        }catch (Exception e){
            logger.error("Error while deleting chatroom with id " + chatroomId + " : " + e.getMessage());
            return false;
        }
    }

    /**
     * Cette méthode permet de changer le status d'un chatroom
     */
    @Transactional
    @Override
    public void setStatusOfChatroom(long chatroomId, boolean status) {
        chatRoomRepository.findById(chatroomId).ifPresent(chatroom -> chatRoomRepository.updateActive(chatroom.getId(), status));
    }

    /**
     * Cette méthode permet de uninivter un utilisateur d'un chatroom
     */
    @Transactional
    @Override
    public boolean deleteUserInvited(long chatroomId, long userId){
        try{
            userChatroomRelationRepository.delete(
                userChatroomRelationRepository.findByChatroomIdAndUserId(chatroomId, userId).get()
            );
            UserDTO user = userRepository.findById(userId).map(DTOMapper::toUserDTO).get();
            publisher.publishEvent(new ChangeChatroomMemberEvent(chatroomId, List.of(), List.of(user)));
            return true;
        }catch (Exception e){
            logger.error("Error while deleting user with id " + userId + " from chatroom with id " + chatroomId + " : " + e.getMessage());
            return false;
        }
    }

    /**
     * Cette méthode permet de mettre à jour un chatroom
     */
    @Transactional
    @Override
    public boolean updateChatroom(ModifyChatroomRequestDTO chatroomRequestDTO, long chatroomId) {
        try{
            Chatroom chatroom = chatRoomRepository.findById(chatroomId).get();
            boolean isChanged = false;
            if(!chatroom.getTitre().equals(chatroomRequestDTO.getTitre())){
                chatroom.setTitre(chatroomRequestDTO.getTitre());
                isChanged = true;
            }
            if(!chatroom.getDescription().equals(chatroomRequestDTO.getDescription())){
                chatroom.setDescription(chatroomRequestDTO.getDescription());
                isChanged = true;
            }
            if(!Objects.equals(chatroomRequestDTO.getStartDate(), "")){
                if(!chatroom.getHoraireCommence().equals(LocalDateTime.parse(chatroomRequestDTO.getStartDate()))){
                    chatroom.setHoraireCommence(LocalDateTime.parse(chatroomRequestDTO.getStartDate()));
                    isChanged = true;
                }
            }
            LocalDateTime dateEnd = chatroom.getHoraireCommence().plusDays(
                    chatroomRequestDTO.getDuration()
            );
            if(!chatroom.getHoraireTermine().equals(dateEnd)){
                chatroom.setHoraireTermine(dateEnd);
                isChanged = true;
            }
            // ajouter les utilisateurs invités
			for (UserDTO user : chatroomRequestDTO.getListAddedUsers()) {
				if (userChatroomRelationRepository.findByChatroomIdAndUserId(chatroomId, user.getId()).isEmpty()) {
					userChatroomRelationService.addRelation(user.getId(), chatroomId, false);
					isChanged = true;
				}
			}
			// supprimer les utilisateurs invités
			for (UserDTO user : chatroomRequestDTO.getListRemovedUsers()) {
				Optional<UserChatroomRelation> relation = userChatroomRelationRepository.findByChatroomIdAndUserId(chatroomId, user.getId());
				if (relation.isPresent() && !relation.get().isOwned()) {
					userChatroomRelationRepository.delete(relation.get());
					isChanged = true;
				}
			}
            if(isChanged){
                chatRoomRepository.save(chatroom);
            }
            publisher.publishEvent(
            		new ChangeChatroomMemberEvent(chatroomId,
												  chatroomRequestDTO.getListAddedUsers(),
												  chatroomRequestDTO.getListRemovedUsers()
		    ));
            return true;
        }catch (RuntimeException e){
            logger.error("Error while updating chatroom with id " + chatroomId + " : " + e.getMessage());
            return false;
        }
    }

    /**
     * Cette méthode permet de vérifier si un utilisateur est propriétaire d'un chatroom
     */
    @Override
    public boolean checkUserIsOwnerOfChatroom(long userId, long chatroomId) {
        try {
            return userChatroomRelationRepository.findByUserIdAndChatroomIdAndOwned(userId, chatroomId, true).isPresent();
        } catch (RuntimeException e) {
            logger.error("Error while checking if user with id " + userId + " is owner of chatroom with id " + chatroomId + " : " + e.getMessage());
            return false;
        }
    }

}
