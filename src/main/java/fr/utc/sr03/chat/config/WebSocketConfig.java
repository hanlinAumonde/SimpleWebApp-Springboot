package fr.utc.sr03.chat.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import fr.utc.sr03.chat.service.utils.ChatHandShakeInterceptor;
import fr.utc.sr03.chat.websocket.ChatWebSocketHandler;
import static fr.utc.sr03.chat.service.utils.ConstantValues.CHAT_ENDPOINT;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
		
	private ChatWebSocketHandler chatWebSocketHandler;
	
	@Autowired
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
