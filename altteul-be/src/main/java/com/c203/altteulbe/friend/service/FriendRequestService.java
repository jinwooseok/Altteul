package com.c203.altteulbe.friend.service;

import java.time.Duration;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.c203.altteulbe.common.dto.RequestStatus;
import com.c203.altteulbe.friend.persistent.repository.FriendRequestRepository;
import com.c203.altteulbe.friend.web.dto.response.FriendRequestResponseDto;
import com.c203.altteulbe.user.persistent.repository.UserJPARepository;
import com.c203.altteulbe.user.service.exception.NotFoundUserException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.lettuce.core.RedisConnectionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class FriendRequestService {
	private final FriendRequestRepository friendRequestRepository;
	private final RedisTemplate<String, String> redisTemplate;
	private final ObjectMapper objectMapper;
	private final UserJPARepository userJPARepository;

	private static final String FRIEND_REQUEST_CACHE_KEY = "friend:request:pending:";
	private static final Duration CACHE_TTL = Duration.ofMinutes(5);

	@Transactional
	public Page<FriendRequestResponseDto> getPendingRequestsFromRedis(Long userId, int page, int size) {
		String cacheKey = FRIEND_REQUEST_CACHE_KEY + userId + ":" + page + ":" + size;

		String cachedValue = redisTemplate.opsForValue().get(cacheKey);
		if (cachedValue != null) {
			try {
				return objectMapper.readValue(cachedValue, new TypeReference<Page<FriendRequestResponseDto>>() {
				});
			} catch (RedisConnectionException e) {
				log.error("Redis 연결 실패: {}", e.getMessage());
				return getPendingFriendRequestsFromDB(userId, page, size);
			} catch (JsonProcessingException e) {
				log.error("캐시 역직렬화 에러: {}", e.getMessage());
				invalidateRequestCache(userId);
			}
		}

		userJPARepository.findByUserId(userId).orElseThrow(() -> {
			log.error("유저 찾기 실패");
			return new NotFoundUserException();
		});
		Page<FriendRequestResponseDto> requests = getPendingFriendRequestsFromDB(
			userId, page, size);

		try {
			String value = objectMapper.writeValueAsString(requests);
			redisTemplate.opsForValue().set(cacheKey, value, CACHE_TTL);
		} catch (JsonProcessingException e) {
			log.error("캐시 직렬화 에러: {}", e.getMessage());
		}
		return requests;
	}

	private Page<FriendRequestResponseDto> getPendingFriendRequestsFromDB(Long userId, int page, int size) {
		Page<FriendRequestResponseDto> requests = friendRequestRepository.findAllByToUserIdAndRequestStatus(
			userId,
			RequestStatus.P,
			PageRequest.of(page, size)
		).map(FriendRequestResponseDto::from);
		return requests;
	}

	public void invalidateRequestCache(Long userId) {
		Set<String> keys = redisTemplate.keys(FRIEND_REQUEST_CACHE_KEY + userId + ":*");
		if (keys != null) {
			redisTemplate.delete(keys);
		}
	}

}
