package fr.utc.sr03.chat.service.interfaces;

import fr.utc.sr03.chat.dto.ChatroomDTO;
import fr.utc.sr03.chat.dto.ChatroomRequestDTO;
import fr.utc.sr03.chat.dto.ChatroomWithOwnerAndStatusDTO;
import fr.utc.sr03.chat.dto.ModifyChatroomDTO;
import fr.utc.sr03.chat.dto.ModifyChatroomRequestDTO;
import fr.utc.sr03.chat.dto.UserDTO;
import fr.utc.sr03.chat.model.Chatroom;
import fr.utc.sr03.chat.model.User;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

public interface ChatroomServiceInt {
    Optional<ModifyChatroomDTO> findChatroom(long chatroomId);

    boolean createChatroom(ChatroomRequestDTO chatroomRequestDTO, long userId);

    Page<ChatroomDTO> getChatroomsOwnedOfUserByPage(long userId, int page);
    
    Page<ChatroomWithOwnerAndStatusDTO> getChatroomsJoinedOfUserByPage(long userId, boolean isOwner, int page);
    
    List<UserDTO> getAllUsersInChatroom(long chatroomId);

    boolean deleteChatRoom(long chatRoomId);

    void setStatusOfChatroom(long chatRoomId,boolean status);

    boolean deleteUserInvited(long chatroomId, long userId);

    boolean checkUserIsOwnerOfChatroom(long userId, long chatroomId);

	/**
	 * Cette méthode permet de mettre à jour un chatroom
	 */
	boolean updateChatroom(ModifyChatroomRequestDTO chatroomRequestDTO, long chatroomId);
}
