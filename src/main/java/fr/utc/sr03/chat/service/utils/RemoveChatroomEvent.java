package fr.utc.sr03.chat.service.utils;

import org.springframework.context.ApplicationEvent;

public class RemoveChatroomEvent extends ApplicationEvent {
	
	private long chatroomId;
	
	public RemoveChatroomEvent(long chatroomId) {
		super(chatroomId);
		this.chatroomId = chatroomId;
	}
	
	public long getEventMsg() {
		return this.chatroomId;
	}
	
	public void setEventMsg(long chatroomId) {
		this.chatroomId = chatroomId;
	}
	
}
