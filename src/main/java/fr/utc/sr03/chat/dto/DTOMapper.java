package fr.utc.sr03.chat.dto;

import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

import fr.utc.sr03.chat.model.Chatroom;
import fr.utc.sr03.chat.model.User;

/**
 * Cette classe permet de convertir un objet User en UserDTO, et un objet Chatroom en ChatroomDTO
 */
public class DTOMapper {
	private static final DateTimeFormatter ISO_LOCAL_DATETIME_MINUTES = 
	        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
	
    public static UserDTO toUserDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setMail(user.getMail());
        return dto;
    }

    public static ChatroomDTO toChatroomDTO(Chatroom chatroom, boolean isActif) {
        ChatroomDTO dto = new ChatroomDTO();
        dto.setId(chatroom.getId());
        dto.setTitre(chatroom.getTitre());
        dto.setDescription(chatroom.getDescription());
        dto.setIsActif(isActif);
        return dto;
    }
    
	public static ChatroomWithOwnerAndStatusDTO toChatroomWithOwnerAndStatusDTO(Chatroom chatroom, User owner, boolean status) {
		ChatroomWithOwnerAndStatusDTO dto = new ChatroomWithOwnerAndStatusDTO();
		dto.setId(chatroom.getId());
		dto.setTitre(chatroom.getTitre());
		dto.setDescription(chatroom.getDescription());
		dto.setOwner(DTOMapper.toUserDTO(owner));
		dto.setIsActif(status);
		return dto;
	}
	
	public static ModifyChatroomDTO toModifyChatroomDTO(Chatroom chatroom) {
		ModifyChatroomDTO dto = new ModifyChatroomDTO();
		dto.setId(chatroom.getId());
		dto.setTitre(chatroom.getTitre());
		dto.setDescription(chatroom.getDescription());
		dto.setStartDate(chatroom.getHoraireCommence().format(ISO_LOCAL_DATETIME_MINUTES));
		dto.setDuration((int) ChronoUnit.DAYS.between(chatroom.getHoraireCommence(), chatroom.getHoraireTermine()));
		dto.setIsActif(chatroom.isActive() && !chatroom.hasNotStarted());
		return dto;
	}
}