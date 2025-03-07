package com.devStudy.chat.service.utils;

import java.util.Map;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriTemplate;

import static com.devStudy.chat.service.utils.ConstantValues.CHAT_URI_PATTERN;

public class ChatHandShakeInterceptor implements HandshakeInterceptor {

	@Override
	public boolean beforeHandshake(ServerHttpRequest request, 
								   ServerHttpResponse response, 
								   WebSocketHandler wsHandler,
								   Map<String, Object> attributes) {
		// Get URI template variables
		UriTemplate uriTemplate = new UriTemplate(CHAT_URI_PATTERN);
		Map<String, String> uriTemplateVars = uriTemplate.match(request.getURI().toString());
		if(!uriTemplateVars.isEmpty()) {
			String chatroomId = uriTemplateVars.get("chatroomId");
			String userId = uriTemplateVars.get("userId");
			
			// Put URI template variables in attributes
			attributes.put("chatroomId", Long.parseLong(chatroomId));
			attributes.put("userId", Long.parseLong(userId));
		}
		return true;
	}

	@Override
	public void afterHandshake(ServerHttpRequest request, 
							   ServerHttpResponse response, 
							   WebSocketHandler wsHandler, 
							   Exception exception) {}

}
