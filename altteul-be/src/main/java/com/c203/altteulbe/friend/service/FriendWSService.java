package com.c203.altteulbe.friend.service;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.c203.altteulbe.websocket.dto.response.WebSocketResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class FriendWSService {
	private final SimpMessagingTemplate simpMessagingTemplate;

	public <T> void sendRequestMessage(Long userId, T responseDto) {
		log.info("Sending message to user: {}, message: {}", userId, responseDto);
		simpMessagingTemplate.convertAndSendToUser(
			userId.toString(), "/notification",
			WebSocketResponse.withData("친구 신청이 도착했습니다.", responseDto)
		);
	}

	public void sendFriendListUpdateMessage(Long userId) {
		simpMessagingTemplate.convertAndSendToUser(
			userId.toString(), "/friend/update", "UPDATED"
		);
	}
}
