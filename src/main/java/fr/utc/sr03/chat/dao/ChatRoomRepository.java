package fr.utc.sr03.chat.dao;

import fr.utc.sr03.chat.model.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    Optional<ChatRoom> findById(long chatroomId);
}
