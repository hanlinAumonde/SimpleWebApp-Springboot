package fr.utc.sr03.chat.service.implementations;

import fr.utc.sr03.chat.dao.ChatroomRepository;
import fr.utc.sr03.chat.model.Chatroom;
import fr.utc.sr03.chat.service.interfaces.ChatroomServiceInt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class ChatroomService implements ChatroomServiceInt {

    @Autowired
    private ChatroomRepository chatRoomRepository;

    @Override
    public Optional<Chatroom> findChatroom(long chatroomId) {
        return chatRoomRepository.findById(chatroomId);
    }

    @Override
    public void deleteChatRoom(long chatroomId) {
        chatRoomRepository.findById(chatroomId).ifPresent(chatRoom -> chatRoomRepository.delete(chatRoom));
    }

    @Transactional
    @Override
    public void setStatusOfChatroom(long chatroomId, boolean status) {
        chatRoomRepository.findById(chatroomId).ifPresent(chatroom -> chatRoomRepository.updateActive(chatroom.getId(), status));
    }

}
