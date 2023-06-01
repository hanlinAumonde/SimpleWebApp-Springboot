package fr.utc.sr03.chat.dao;

import fr.utc.sr03.chat.model.Chatroom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Optional;

public interface ChatroomRepository extends JpaRepository<Chatroom, Long> {
    Optional<Chatroom> findById(long chatroomId);
    Optional<Chatroom> findByTitreAndDescriptionAndHoraireCommenceAndHoraireTermine(String titre, String description, LocalDateTime horaireCommence, LocalDateTime horaireTermine);

    @Modifying
    @Query("update Chatroom c set c.active = ?2 where c.id = ?1")
    void updateActive(long chatroomId, boolean status);

    @Query("select c from Chatroom c, UserChatroomRelation r where c.horaireTermine >= CURRENT_TIMESTAMP and c.id = r.chatroomId and r.userId = ?1 and r.owned = ?2")
    Page<Chatroom> findChatroomsOwnedOrJoinedOfUserByPage(long userId, boolean isOwner, Pageable pageable);
}
