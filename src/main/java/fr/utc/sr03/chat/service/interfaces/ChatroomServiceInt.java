package fr.utc.sr03.chat.service.interfaces;

import fr.utc.sr03.chat.model.Chatroom;
import fr.utc.sr03.chat.model.User;
import fr.utc.sr03.chat.service.utils.ChatroomRequestDTO;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

public interface ChatroomServiceInt {
    Optional<Chatroom> findChatroom(long chatroomId);

    Chatroom createChatroom(ChatroomRequestDTO chatroomRequestDTO, long userId);

    Page<Chatroom> getChatroomsOwnedOrJoinedOfUserByPage(long userId, boolean isOwner, int page, int size);

    List<User> getAllUsersInChatroom(long chatroomId);

    boolean deleteChatRoom(long chatRoomId);

    void setStatusOfChatroom(long chatRoomId,boolean status);

    boolean deleteUserInvited(long chatroomId, long userId);

    boolean updateChatroom(ChatroomRequestDTO chatroomRequestDTO, long chatroomId);

    boolean checkUserIsOwnerOfChatroom(long userId, long chatroomId);
}
