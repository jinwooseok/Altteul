package com.c203.altteulbe.room.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.c203.altteulbe.common.annotation.DistributedLock;
import com.c203.altteulbe.common.dto.BattleType;
import com.c203.altteulbe.common.utils.RedisKeys;
import com.c203.altteulbe.room.persistent.repository.team.TeamRoomRedisRepository;
import com.c203.altteulbe.room.service.exception.CannotLeaveRoomException;
import com.c203.altteulbe.room.service.exception.DuplicateRoomEntryException;
import com.c203.altteulbe.room.service.exception.UserNotInRoomException;
import com.c203.altteulbe.room.web.dto.request.RoomRequestDto;
import com.c203.altteulbe.room.web.dto.response.RoomEnterResponseDto;
import com.c203.altteulbe.room.web.dto.response.RoomLeaveResponseDto;
import com.c203.altteulbe.user.persistent.entity.User;
import com.c203.altteulbe.user.persistent.repository.UserJPARepository;
import com.c203.altteulbe.user.service.exception.NotFoundUserException;
import com.c203.altteulbe.user.web.dto.response.UserInfoResponseDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/*
 * 팀전 대기방 입장 처리
 * 동일 유저의 중복 요청 방지 및 동시성 제어를 위해 userId를 키로 갖는 락을 생성
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TeamRoomService {
	private final RedisTemplate<String, String> redisTemplate;
	private final UserJPARepository userJPARepository;
	private final TeamRoomRedisRepository teamRoomRedisRepository;
	private final RoomWebSocketService roomWebSocketService;
	private final RoomValidator validator;

	@DistributedLock(key="#requestDto.userId")
	@Transactional
	public RoomEnterResponseDto enterTeamRoom(RoomRequestDto requestDto) {
		User user = userJPARepository.findByUserId(requestDto.getUserId())
									.orElseThrow(()->new NotFoundUserException());

		// 유저가 이미 방에 존재하는지 검증
		if (validator.isUserInAnyRoom(user.getUserId(), BattleType.T)) {
			throw new DuplicateRoomEntryException();
		}

		// 입장 가능한 대기방 조회
		Long existingRoomId = teamRoomRedisRepository.getAvailableRoom();

		// 입장 가능한 대기방이 있는 경우 유저 저장 (API 응답 + WebSocket 전송)
		if (existingRoomId != null) {
			RoomEnterResponseDto responseDto = teamRoomRedisRepository.insertUserToExistingRoom(existingRoomId, user);

			// 웹소켓 메시지 브로드캐스트
			roomWebSocketService.sendWebSocketMessage(responseDto.getRoomId().toString(), "ENTER", responseDto, BattleType.T);
			return responseDto;
		}

		// 입장 가능한 대기방이 없는 경우 대기방 생성 후 유저 저장 (API 응답)
		RoomEnterResponseDto responseDto = teamRoomRedisRepository.createRedisTeamRoom(user);
		return responseDto;
	}

	/**
	 * 팀전 대기방 퇴장 처리
	 */
	@DistributedLock(key = "#requestDto.userId")
	@Transactional
	public void leaveTeamRoom(RoomRequestDto requestDto) {
		Long userId = requestDto.getUserId();

		// 유저가 속한 방 조회
		Long roomId = teamRoomRedisRepository.getRoomIdByUser(userId);
		if (roomId == null) {
			throw new UserNotInRoomException();
		}

		// 퇴장하는 유저 정보 조회
		User user = userJPARepository.findByUserId(userId)
			.orElseThrow(()->new NotFoundUserException());

		UserInfoResponseDto leftUserDto = UserInfoResponseDto.fromEntity(user);

		// 방 상태 확인
		String roomStatusKey = RedisKeys.TeamRoomStatus(roomId);
		String status = redisTemplate.opsForValue().get(roomStatusKey);

		if (!"waiting".equals(status)) {
			throw new CannotLeaveRoomException();
		}

		// Redis에서 퇴장하는 유저 삭제
		String roomUsersKey = RedisKeys.TeamRoomUsers(roomId);
		redisTemplate.opsForList().remove(roomUsersKey, 0, userId.toString());
		redisTemplate.delete(RedisKeys.userTeamRoom(userId));

		// 퇴장 후 방에 남은 유저가 없는 경우 관련 데이터 삭제
		List<String> remainingUserIds = redisTemplate.opsForList().range(roomUsersKey, 0, -1);

		if (remainingUserIds == null || remainingUserIds.isEmpty()) {
			teamRoomRedisRepository.deleteRedisTeamRoom(roomId);
			return;
		}

		// 방장 조회
		Long leaderId = Long.parseLong(remainingUserIds.get(0));

		// 남은 유저들 정보 반환
		List<User> remainingUsers = getUserByIds(remainingUserIds);
		List<UserInfoResponseDto> remainingUserDtos = UserInfoResponseDto.fromEntities(remainingUsers);

		RoomLeaveResponseDto responseDto = RoomLeaveResponseDto.toResponse(
			roomId, leaderId, leftUserDto, remainingUserDtos
		);
		// WebSocket 메시지 브로드캐스트
		roomWebSocketService.sendWebSocketMessage(roomId.toString(), "LEAVE", responseDto, BattleType.S);
	}

	// userId 리스트로 User 엔티티 조회
	private List<User> getUserByIds(List<String> userIds) {
		return userJPARepository.findByUserIdIn(
			userIds.stream().map(Long::parseLong).collect(Collectors.toList())
		);
	}
}