package com.devStudy.chat.service.utils.Events;

import java.util.List;

import org.springframework.context.ApplicationEvent;

import com.devStudy.chat.dto.UserDTO;

public class ChangeChatroomMemberEvent extends ApplicationEvent {
	
	private static final long serialVersionUID = 8672267835667153312L;
	
	private long chatroomId;
	
	private List<UserDTO> addedMembers;
	
	private List<UserDTO> removedMembers;

	public ChangeChatroomMemberEvent(long chatroomId, List<UserDTO> addedMembers, List<UserDTO> removedMembers) {
		super(chatroomId);
		this.chatroomId = chatroomId;
		this.addedMembers = addedMembers;
		this.removedMembers = removedMembers;
	}
	
	public long getChatroomId() {
		return chatroomId;
	}
	
	public List<UserDTO> getAddedMembers() {
		return addedMembers;
	}
	
	public List<UserDTO> getRemovedMembers() {
		return removedMembers;
	}

}