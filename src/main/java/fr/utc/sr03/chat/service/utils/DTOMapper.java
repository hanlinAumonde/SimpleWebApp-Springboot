package fr.utc.sr03.chat.service.utils;

import fr.utc.sr03.chat.model.Chatroom;
import fr.utc.sr03.chat.model.User;

public class DTOMapper {
    public static UserDTO toUserDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setMail(user.getMail());
        return dto;
    }

    public static ChatroomDTO toChatroomDTO(Chatroom chatroom) {
        ChatroomDTO dto = new ChatroomDTO();
        dto.setId(chatroom.getId());
        dto.setTitre(chatroom.getTitre());
        dto.setDescription(chatroom.getDescription());
        return dto;
    }
}
