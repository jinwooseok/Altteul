package com.c203.altteulbe.friend.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.data.redis.connection.StringRedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.c203.altteulbe.common.exception.BusinessException;
import com.c203.altteulbe.common.utils.RedisKeys;
import com.c203.altteulbe.user.service.exception.NotFoundUserException;

import io.lettuce.core.RedisConnectionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserStatusService {
	private final RedisTemplate<String, String> redisTemplate;

	// 유저 온라인 상태 설정
	public void setUserOnline(Long userId) {
		validateUserId(userId);
		try {
			String key = RedisKeys.getUserStatusKey(userId);
			redisTemplate.opsForValue().set(key, "online", 60, TimeUnit.SECONDS);
		} catch (RedisConnectionException e) {
			log.error("Redis 연결 실패: {}", e.getMessage());
			throw new BusinessException("Redis 연결에 실패했습니다.", HttpStatus.SERVICE_UNAVAILABLE);
		} catch (Exception e) {
			log.error("사용자 온라인 상태 설정 중 오류 발생: {}", e.getMessage());
			throw new BusinessException("사용자 상태 업데이트에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	// 유저 오프라인 상태로 변경
	public void setUserOffline(Long userId) {
		validateUserId(userId);
		try {
			String key = RedisKeys.getUserStatusKey(userId);
			redisTemplate.delete(key);
		} catch (RedisConnectionException e) {
			log.error("Redis 연결 실패: {}", e.getMessage());
			throw new BusinessException("Redis 연결에 실패했습니다.", HttpStatus.SERVICE_UNAVAILABLE);
		} catch (Exception e) {
			log.error("사용자 오프라인 상태 설정 중 오류 발생: {}", e.getMessage());
			throw new BusinessException("사용자 상태 업데이트에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	// 유저 온라인 상태 확인
	public boolean isUserOnline(Long userId) {
		validateUserId(userId);
		try {
			String key = RedisKeys.getUserStatusKey(userId);
			return Boolean.TRUE.equals(redisTemplate.hasKey(key));
		} catch (RedisConnectionException e) {
			log.error("Redis 연결 실패: {}", e.getMessage());
			return false;
		} catch (Exception e) {
			log.error("사용자 상태 확인 중 오류 발생: {}", e.getMessage());
			return false;
		}
	}

	// 사용자들의 온라인 상태 한 번에 확인하기
	public Map<Long, Boolean> getBulkOnlineStatus(List<Long> userIds) {
		// Redis 파이프라인을 사용하여 여러 Redis 명령을 한 번에 처리하는 방법
		List<Object> results = redisTemplate.executePipelined((RedisCallback<Object>)connection -> {
			StringRedisConnection stringConn = (StringRedisConnection)connection;
			userIds.forEach(id ->
				// exists 명령은 Long 타입의 값을 반환
				// 각 키에 대해 Long 타입의 1 또는 0 값을 반환
				stringConn.exists(RedisKeys.getUserStatusKey(id))); // redis에 해당 키가 있는지 확인 (존재하면 온라인)
			return null;
		});
		// // 결과를 userIds에 맞게 매핑하여 Map으로 반환
		return IntStream
			.range(0, userIds.size())
			.boxed()
			.collect(Collectors.toMap(
				userIds::get, // // userId를 키로 사용
				i -> ((Long)results.get(i)) > 0 // Redis 결과를 Long으로 캐스팅 후 존재 여부로 처리
			));
	}

	private void validateUserId(Long userId) {
		if (userId == null) {
			throw new NotFoundUserException();
		}
	}
}
