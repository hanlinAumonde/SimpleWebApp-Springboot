package fr.utc.sr03.chat.service.utils;

import fr.utc.sr03.chat.model.User;

public class UserMapper {
    public static UserDTO toDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setMail(user.getMail());
        return dto;
    }
}
