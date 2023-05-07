package fr.utc.sr03.chat.service.interfaces;

import fr.utc.sr03.chat.model.Chatroom;
import java.util.Optional;

public interface ChatroomServiceInt {
    Optional<Chatroom> findChatroom(long chatroomId);

    void deleteChatRoom(long chatRoomId);

    void setStatusOfChatroom(long chatRoomId,boolean status);
}
