package com.c203.altteulbe.chat.web.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.c203.altteulbe.chat.service.ChatroomService;
import com.c203.altteulbe.chat.web.dto.response.ChatroomResponseDto;
import com.c203.altteulbe.common.response.ApiResponse;
import com.c203.altteulbe.common.response.ApiResponseEntity;
import com.c203.altteulbe.common.response.ResponseBody;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ChatroomController {
	private final ChatroomService chatroomService;


	// 채팅방 목록 조회
	@GetMapping("/chatroom")
	@PreAuthorize("isAuthenticated()")
	public ApiResponseEntity<ResponseBody.Success<List<ChatroomResponseDto>>> getChatroomList(
		@AuthenticationPrincipal Long id) {
		return ApiResponse.success(chatroomService.getAllChatrooms(id), HttpStatus.OK);
	}

	// 채팅방 단일 조회(쓰진 않지만 혹시 몰라서 만들어놓음)
	@GetMapping("chatroom/{chatroomId}")
	public ApiResponseEntity<ResponseBody.Success<ChatroomResponseDto>> getChatroom(
		@PathVariable(value = "chatroomId") Long chatroomId,
		@AuthenticationPrincipal Long id) {
		return ApiResponse.success(chatroomService.getChatroom(chatroomId, id), HttpStatus.OK);
	}
}
