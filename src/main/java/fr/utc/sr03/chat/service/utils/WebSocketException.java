package fr.utc.sr03.chat.service.utils;

public class WebSocketException extends RuntimeException {
	public WebSocketException(String message) {
		super(message);
	}
	
	public WebSocketException(String message, Throwable cause) {
		super(message, cause);
	}

}
