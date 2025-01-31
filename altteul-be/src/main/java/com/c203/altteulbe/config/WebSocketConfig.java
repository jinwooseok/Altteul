package com.c203.altteulbe.config;

import java.util.Optional;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import com.c203.altteulbe.common.security.utils.JWTUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
@Slf4j
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
	private final JWTUtil jwtUtil;

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		registry.addEndpoint("/ws")
			.setAllowedOrigins("http://localhost:3000", "http://localhost:80")
			.withSockJS();
	}

	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {
		registry.enableSimpleBroker("/sub");
		registry.setApplicationDestinationPrefixes("/pub");
	}

	// websocket 연결 전에 jwt 토큰으로 인증 처리
	@Override
	public void configureClientInboundChannel(ChannelRegistration registration) {
		registration.interceptors(new ChannelInterceptor() {
			@Override
			public Message<?> preSend(Message<?> message, MessageChannel channel) {
				StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
				if (StompCommand.CONNECT == accessor.getCommand()) {
					Optional<String> tokenOptional = Optional.ofNullable(
						accessor.getFirstNativeHeader("Authorization"));
					String jwtToken = tokenOptional
						.filter(token -> token.startsWith("Bearer "))
						.map(token -> token.substring(7))
						.filter(token -> !jwtUtil.isExpired(token))
						.orElseThrow(() -> new RuntimeException("유효하지 않은 토큰 입니다."));
					Long userId = jwtUtil.getId(jwtToken);
					accessor.setUser(userId::toString);
				}
				return message;
			}
		});
	}
}
