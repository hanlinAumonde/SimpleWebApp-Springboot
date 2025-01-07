package com.devStudy.chat.service.utils;

import java.util.Map;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

public class ChatHandShakeInterceptor implements HandshakeInterceptor {

	@SuppressWarnings("unchecked")
	@Override
	public boolean beforeHandshake(ServerHttpRequest request, 
								   ServerHttpResponse response, 
								   WebSocketHandler wsHandler,
								   Map<String, Object> attributes) throws Exception {
		// Get URI
		if(request instanceof ServletServerHttpRequest) {
			ServletServerHttpRequest servletServerHttpRequest = (ServletServerHttpRequest) request;
			// Get URI template variables
			Map<String, String> uriTemplateVars = (Map<String, String>)
					servletServerHttpRequest.getServletRequest().getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
			if(uriTemplateVars != null) {
				String chatroomId = uriTemplateVars.get("chatroomId");
				String userId = uriTemplateVars.get("userId");
				
				// Put URI template variables in attributes
				attributes.put("chatroomId", Long.parseLong(chatroomId));
				attributes.put("userId", Long.parseLong(userId));
			}
		}
		return true;
	}

	@Override
	public void afterHandshake(ServerHttpRequest request, 
							   ServerHttpResponse response, 
							   WebSocketHandler wsHandler, 
							   Exception exception) {}

}
