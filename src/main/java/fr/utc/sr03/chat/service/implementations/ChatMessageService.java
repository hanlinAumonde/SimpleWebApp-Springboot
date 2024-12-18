package fr.utc.sr03.chat.service.implementations;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.time.DateUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import fr.utc.sr03.chat.dao.ChatMessageRepository;
import fr.utc.sr03.chat.dto.ChatMsgDTO;
import fr.utc.sr03.chat.dto.UserDTO;
import fr.utc.sr03.chat.model.ChatMessage;
import fr.utc.sr03.chat.model.User;
import fr.utc.sr03.chat.service.interfaces.ChatMessageServiceInt;

@Component
public class ChatMessageService implements ChatMessageServiceInt {
	
	/*
	 * @Autowired private MongoClient mongoClient;
	 */
	
	@Autowired
	private ChatMessageRepository chatMessageRepository;
	
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	
	private static final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
	
	private static final String MSG_DATE_SIGN = "dateSign";
	private static final String MSG_CONTENT = "content";

	@Override
	public void saveMsgIntoCollection(long chatroomId, UserDTO sender, String content, Date timestamp) {
		ChatMessage chatMessage = new ChatMessage();
		chatMessage.setChatroomId(chatroomId);
		chatMessage.setUser(sender);
		chatMessage.setContent(content);
		chatMessage.setTimestamp(timestamp);
		chatMessageRepository.insert(chatMessage);
	}

	@Override
	public List<ChatMsgDTO> getChatMessagesByChatroomId(long chatroomId) {
		List<ChatMessage> initialRes = chatMessageRepository.findByChatroomId(chatroomId);
		List<ChatMsgDTO> res = new ArrayList<>();
		int currentIndex = 0;
		for(int i = 0; i < initialRes.size(); i++) {
			ChatMessage msg = initialRes.get(i);
			if(i == 0 || !(DateUtils.isSameDay(msg.getTimestamp(), initialRes.get(i - 1).getTimestamp()))) {
				res.add(setDateSignMsg(currentIndex,dateFormat.format(msg.getTimestamp())));
				currentIndex++;
			}
			res.add(setContentMsg(currentIndex, msg));
			currentIndex++;
		}
		return res;
	}
	
	private ChatMsgDTO setDateSignMsg(int index, String date) {
		ChatMsgDTO msgDTO = new ChatMsgDTO();
		msgDTO.setIndex(index);
		msgDTO.setTimestamp(date);
		msgDTO.setMessageType(MSG_DATE_SIGN);
		return msgDTO;
	}
	
	private ChatMsgDTO setContentMsg(int index, ChatMessage msg) {
		ChatMsgDTO msgDTO = new ChatMsgDTO();
		msgDTO.setIndex(index);
		msgDTO.setUsername(msg.getUser().getFirstName() + " " + msg.getUser().getLastName());
		msgDTO.setMessage(msg.getContent());
		msgDTO.setTimestamp(timeFormat.format(msg.getTimestamp()));
		msgDTO.setSentByUser(msg.getUser().getId() == 
				((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId()
		);
		msgDTO.setMessageType(MSG_CONTENT);
		return msgDTO;
	}

}
