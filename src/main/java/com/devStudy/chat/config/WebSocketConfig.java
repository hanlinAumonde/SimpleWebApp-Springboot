package com.devStudy.chat.config;

import static com.devStudy.chat.service.utils.ConstantValues.CHAT_ENDPOINT;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import com.devStudy.chat.service.utils.ChatHandShakeInterceptor;
import com.devStudy.chat.websocket.ChatWebSocketHandler;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
		
	private ChatWebSocketHandler chatWebSocketHandler;
	
	public WebSocketConfig(ChatWebSocketHandler chatWebSocketHandler) {
		this.chatWebSocketHandler = chatWebSocketHandler;
	}

	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(chatWebSocketHandler, CHAT_ENDPOINT)
            	.addInterceptors(new ChatHandShakeInterceptor())
            	.setAllowedOrigins("*");
	}

}
