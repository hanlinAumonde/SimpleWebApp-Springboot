package fr.utc.sr03.chat.service.implementations;

import fr.utc.sr03.chat.controller.LoginController;
import fr.utc.sr03.chat.dao.ChatroomRepository;
import fr.utc.sr03.chat.dao.UserChatroomRelationRepository;
import fr.utc.sr03.chat.dao.UserRepository;
import fr.utc.sr03.chat.model.Chatroom;
import fr.utc.sr03.chat.model.User;
import fr.utc.sr03.chat.model.UserChatroomRelation;
import fr.utc.sr03.chat.service.interfaces.ChatroomServiceInt;
import fr.utc.sr03.chat.service.utils.ChatroomRequestDTO;
import fr.utc.sr03.chat.service.utils.UserDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChatroomService implements ChatroomServiceInt {

    private final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Resource
    private UserChatroomRelationService userChatroomRelationService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ChatroomRepository chatRoomRepository;

    @Autowired
    private UserChatroomRelationRepository userChatroomRelationRepository;

    @Override
    public Optional<Chatroom> findChatroom(long chatroomId) {
        return chatRoomRepository.findById(chatroomId);
    }

    @Transactional
    @Override
    public Chatroom createChatroom(ChatroomRequestDTO chatroomRequestDTO, long userId) {
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
                    return chatroom;
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
            return chatroomAdded;
        } catch (Exception e) {
            logger.error("Error while creating chatroom : " + e.getMessage());
            return new Chatroom();
        }
    }

    @Transactional
    @Override
    public List<Chatroom> getChatroomsOwnedOrJoinedByUser(long userId, boolean isOwner) {
        List<Chatroom> Chatrooms = new ArrayList<>();
        List<UserChatroomRelation> ChatroomsOwnedOrJoined = userChatroomRelationRepository.findByUserIdAndOwned(userId, isOwner);
        for(UserChatroomRelation owned : ChatroomsOwnedOrJoined){
            Chatroom ChatroomTemp = findChatroom(owned.getChatroomId()).get();
            LocalDateTime currentDate = LocalDateTime.now();
            if(ChatroomTemp.getHoraireTermine().isAfter(currentDate)){
                Chatrooms.add(ChatroomTemp);
            }
        }
        return Chatrooms;
    }

    @Transactional
    @Override
    public List<User> getUsersInvitedToChatroom(long chatroomId) {
        List<User> usersInvited = new ArrayList<>();
        List<UserChatroomRelation> usersInvitedRelation = userChatroomRelationService.findUsersInvitedToChatroom(chatroomId);
        for(UserChatroomRelation userInvited : usersInvitedRelation){
            usersInvited.add(
                   userRepository.findById(userInvited.getUserId()).get()
            );
        }
        return usersInvited;
    }

    @Transactional
    @Override
    public List<User> getUsersNotInvitedToChatroom(long chatroomId){
        List<User> allUsers = userRepository.findByAdmin(false);
        List<User> usersInvited = getUsersInvitedToChatroom(chatroomId);
        Set<Long> idOfUsersInvited = usersInvited.stream().map(User::getId).collect(Collectors.toSet());
        return allUsers.stream().filter(
                user -> !idOfUsersInvited.contains(user.getId())
        ).collect(Collectors.toList());
    }

    @Transactional
    @Override
    public boolean deleteChatRoom(long chatroomId) {
        try{
            userChatroomRelationRepository.deleteAll(userChatroomRelationRepository.findByChatroomId(chatroomId));
            chatRoomRepository.deleteById(chatroomId);
            return true;
        }catch (Exception e){
            logger.error("Error while deleting chatroom with id " + chatroomId + " : " + e.getMessage());
            return false;
        }
    }

    @Transactional
    @Override
    public void setStatusOfChatroom(long chatroomId, boolean status) {
        chatRoomRepository.findById(chatroomId).ifPresent(chatroom -> chatRoomRepository.updateActive(chatroom.getId(), status));
    }

    @Transactional
    @Override
    public boolean deleteUserInvited(long chatroomId, long userId){
        try{
            userChatroomRelationRepository.delete(
                userChatroomRelationRepository.findByChatroomIdAndUserId(chatroomId, userId).get()
            );
            return true;
        }catch (Exception e){
            logger.error("Error while deleting user with id " + userId + " from chatroom with id " + chatroomId + " : " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean updateChatroom(ChatroomRequestDTO chatroomRequestDTO, long chatroomId) {
        try{
            Chatroom chatroom = chatRoomRepository.findById(chatroomId).get();
            boolean isChanged = false;
            if(!chatroom.getTitre().equals(chatroomRequestDTO.getTitre())){
                chatroom.setTitre(chatroomRequestDTO.getTitre());
                isChanged = true;
            }
            logger.info("point 1" + chatroomRequestDTO.getTitre() + "|" + chatroom.getTitre());
            if(!chatroom.getDescription().equals(chatroomRequestDTO.getDescription())){
                chatroom.setDescription(chatroomRequestDTO.getDescription());
                isChanged = true;
            }
            logger.info("point 2" + chatroomRequestDTO.getDescription() + "|" + chatroom.getDescription());
            if(!Objects.equals(chatroomRequestDTO.getStartDate(), "")){
                if(!chatroom.getHoraireCommence().equals(LocalDateTime.parse(chatroomRequestDTO.getStartDate()))){
                    chatroom.setHoraireCommence(LocalDateTime.parse(chatroomRequestDTO.getStartDate()));
                    isChanged = true;
                }
            }
            logger.info("point 3" + chatroomRequestDTO.getStartDate() + "|" + chatroom.getHoraireCommence());
            LocalDateTime dateEnd = chatroom.getHoraireCommence().plusDays(
                    chatroomRequestDTO.getDuration()
            );
            if(!chatroom.getHoraireTermine().equals(dateEnd)){
                chatroom.setHoraireTermine(dateEnd);
                isChanged = true;
            }
            logger.info("point 4" + dateEnd + "|" + chatroom.getHoraireTermine());
            logger.info(isChanged + "");
            if(isChanged){
                chatRoomRepository.save(chatroom);
            }
            logger.info("point 5");
            return isChanged;
        }catch (Exception e){
            logger.error("Error while updating chatroom with id " + chatroomId + " : " + e.getMessage());
            return false;
        }
    }

}
