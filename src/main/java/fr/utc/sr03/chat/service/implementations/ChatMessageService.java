package fr.utc.sr03.chat.service.implementations;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.time.DateUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import fr.utc.sr03.chat.dao.ChatMessageRepository;
import fr.utc.sr03.chat.dto.ChatMsgDTO;
import fr.utc.sr03.chat.dto.UserDTO;
import fr.utc.sr03.chat.model.ChatMessage;
import fr.utc.sr03.chat.model.User;
import fr.utc.sr03.chat.service.interfaces.ChatMessageServiceInt;
import static fr.utc.sr03.chat.service.utils.ConstantValues.DefaultPageSize_Messages;
import static fr.utc.sr03.chat.service.utils.ConstantValues.MSG_CONTENT;
import static fr.utc.sr03.chat.service.utils.ConstantValues.MSG_DATE_SIGN;
import static fr.utc.sr03.chat.service.utils.ConstantValues.MSG_LATEST_DATE_SIGN;
import static fr.utc.sr03.chat.service.utils.ConstantValues.DateSignFormat;
import static fr.utc.sr03.chat.service.utils.ConstantValues.ContentTimeStampFormat;

@Component
public class ChatMessageService implements ChatMessageServiceInt {
	
	/*
	 * @Autowired private MongoClient mongoClient;
	 */
	
	@Autowired
	private ChatMessageRepository chatMessageRepository;
	
	
	
	private Pageable getPageableSetting(int page, int size) {
		return PageRequest.of(page,size, Sort.by(Sort.Direction.DESC, "timestamp"));
	}

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
		return setResMsgList(initialRes);
	}
	
	@Override
	public List<ChatMsgDTO> getChatMessagesByChatroomIdByPage(long chatroomId, int page){
		List<ChatMessage> initialRes = chatMessageRepository.findByChatroomId(chatroomId, 
				getPageableSetting(page, DefaultPageSize_Messages)).getContent();
		return setResMsgList(initialRes);
	}
	
	private List<ChatMsgDTO> setResMsgList(List<ChatMessage> initialList){
		List<ChatMsgDTO> res = new ArrayList<>();
		ChatMsgDTO latestDateSign = new ChatMsgDTO();
		int currentIndex = 0;
		for(int i = initialList.size()-1; i >= 0 ; i--) { 
			 ChatMessage msg = initialList.get(i); 
			 if(i == initialList.size()-1 || !(DateUtils.isSameDay(msg.getTimestamp(),initialList.get(i + 1).getTimestamp()))) { 
				 ChatMsgDTO dateSign = setDateSignMsg(currentIndex, DateSignFormat.format(msg.getTimestamp()));
				 latestDateSign.setMessageType(MSG_LATEST_DATE_SIGN);
				 latestDateSign.setTimestamp(dateSign.getTimestamp());
				 res.add(dateSign); 
				 currentIndex++; 
			}
			res.add(setContentMsg(currentIndex, msg)); 
			currentIndex++; 
		}
		if(!initialList.isEmpty()) {
			latestDateSign.setIndex(currentIndex);
			res.add(latestDateSign);
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
		msgDTO.setUserId(msg.getUser().getId());
		msgDTO.setUsername(msg.getUser().getFirstName() + " " + msg.getUser().getLastName());
		msgDTO.setMessage(msg.getContent());
		msgDTO.setTimestamp(ContentTimeStampFormat.format(msg.getTimestamp()));
		msgDTO.setSentByUser(msg.getUser().getId() == 
				((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId()
		);
		msgDTO.setMessageType(MSG_CONTENT);
		return msgDTO;
	}

}
