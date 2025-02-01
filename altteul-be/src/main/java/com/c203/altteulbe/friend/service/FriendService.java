package com.c203.altteulbe.friend.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.c203.altteulbe.common.exception.BusinessException;
import com.c203.altteulbe.friend.persistent.entity.Friendship;
import com.c203.altteulbe.friend.persistent.repository.FriendRepository;
import com.c203.altteulbe.friend.web.dto.response.FriendResponseDto;
import com.c203.altteulbe.friend.web.dto.response.FriendStatusUpdateResponseDto;
import com.c203.altteulbe.user.persistent.repository.UserRepository;
import com.c203.altteulbe.user.service.exception.NotFoundUserException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FriendService {

	private final FriendRepository friendRepository;
	private final UserStatusService userStatusService;
	private final UserRepository userRepository;
	private final SimpMessagingTemplate messagingTemplate;

	@Transactional(readOnly = true)
	public Page<FriendResponseDto> getFriendsList(Long userId, int page, int size) {
		userRepository.findByUserId(userId)
			.orElseThrow(NotFoundUserException::new);

		Pageable pageable = PageRequest.of(page, size);
		Page<Friendship> friendsPage = friendRepository.findAllByIdUserId(userId, pageable);
		if (friendsPage.isEmpty()) {
			return Page.empty(pageable);
		}
		return friendsPage
			.map(friendship -> FriendResponseDto.from(
				friendship,
				userStatusService.isUserOnline(friendship.getFriend().getUserId())
			));
	}
}
