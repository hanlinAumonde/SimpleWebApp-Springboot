package fr.utc.sr03.chat.dao;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import fr.utc.sr03.chat.model.ChatMessage;

public interface ChatMessageRepository extends MongoRepository<ChatMessage, ObjectId> {
	@Query(sort = "{ timestamp : 1 }")
	List<ChatMessage> findByChatroomId(long chatroomId);
}
