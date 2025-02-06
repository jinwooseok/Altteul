package com.c203.altteulbe.user.service;

import org.springframework.stereotype.Service;

import com.c203.altteulbe.user.persistent.entity.User;
import com.c203.altteulbe.user.persistent.repository.UserJPARepository;
import com.c203.altteulbe.user.service.exception.NotFoundUserException;
import com.c203.altteulbe.user.service.exception.SelfSearchException;
import com.c203.altteulbe.user.web.dto.response.SearchUserResponseDto;

import com.c203.altteulbe.user.web.dto.response.UserProfileResponseDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
	private final UserJPARepository userRepository;

	public SearchUserResponseDto searchUser(Long userId, String nickname) {
		User user = userRepository.findByNickname(nickname).orElseThrow(NotFoundUserException::new);

	public UserProfileResponseDto getUserProfile(Long userId) {
		return new UserProfileResponseDto();
	}
		if (userId.equals(user.getUserId())) {
			throw new SelfSearchException();
		}

		return SearchUserResponseDto.from(user);
	}
}
