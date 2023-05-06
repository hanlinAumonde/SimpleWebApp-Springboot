package fr.utc.sr03.chat.dao;

import fr.utc.sr03.chat.model.Chatroom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ChatroomRepository extends JpaRepository<Chatroom, Long> {
    Optional<Chatroom> findById(long chatroomId);

    @Modifying
    @Query("update Chatroom c set c.active = ?2 where c.id = ?1")
    void updateActive(long chatroomId, boolean status);
}
