package fr.utc.sr03.chat.service.interfaces;

import java.util.Date;
import java.util.List;

import fr.utc.sr03.chat.dto.ChatMsgDTO;
import fr.utc.sr03.chat.dto.UserDTO;

public interface ChatMessageServiceInt {
	
	public void saveMsgIntoCollection(long chatroomId, UserDTO sender, String content, Date timestamp);
	
	public List<ChatMsgDTO> getChatMessagesByChatroomId(long chatroomId);
	
	public List<ChatMsgDTO> getChatMessagesByChatroomIdByPage(long chatroomId, int page);
}
