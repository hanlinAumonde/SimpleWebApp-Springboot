package com.devStudy.chat.config;

import static com.devStudy.chat.service.utils.ConstantValues.CHAT_ENDPOINT;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import com.devStudy.chat.service.utils.ChatHandShakeInterceptor;
import com.devStudy.chat.websocket.ChatWebSocketHandler;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
	private static  final Logger logger = LoggerFactory.getLogger(WebSocketConfig.class);
		
	private final ChatWebSocketHandler chatWebSocketHandler;

	@Autowired
	public WebSocketConfig(ChatWebSocketHandler chatWebSocketHandler) {
		this.chatWebSocketHandler = chatWebSocketHandler;
	}

	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		logger.info("Registering WebSocket handlers with endpoint: {}", CHAT_ENDPOINT);
		registry.addHandler(chatWebSocketHandler, CHAT_ENDPOINT)
            	.addInterceptors(new ChatHandShakeInterceptor())
            	.setAllowedOrigins("*");
		logger.info("WebSocket handlers registered successfully");
	}
}
