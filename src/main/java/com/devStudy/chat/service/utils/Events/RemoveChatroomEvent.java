package com.devStudy.chat.service.utils.Events;

import org.springframework.context.ApplicationEvent;

public class RemoveChatroomEvent extends ApplicationEvent {
	
	private static final long serialVersionUID = -4118480440900563692L;
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
