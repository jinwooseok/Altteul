package com.c203.altteulbe.room.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

//import com.c203.altteulbe.common.annotation.DistributedLock;
import com.c203.altteulbe.common.dto.BattleType;
import com.c203.altteulbe.common.utils.RedisKeys;
import com.c203.altteulbe.game.persistent.entity.Game;
import com.c203.altteulbe.game.persistent.entity.Problem;
import com.c203.altteulbe.game.persistent.entity.Testcase;
import com.c203.altteulbe.game.persistent.repository.game.GameJPARepository;
import com.c203.altteulbe.game.persistent.repository.problem.ProblemRepository;
import com.c203.altteulbe.game.persistent.repository.testcase.TestcaseRepository;
import com.c203.altteulbe.game.service.exception.GameCannotStartException;
import com.c203.altteulbe.game.service.exception.NotEnoughUserException;
import com.c203.altteulbe.game.service.exception.ProblemNotFoundException;
import com.c203.altteulbe.game.web.dto.response.GameStartForProblemDto;
import com.c203.altteulbe.game.web.dto.response.GameStartForTestcaseDto;
import com.c203.altteulbe.room.persistent.entity.TeamRoom;
import com.c203.altteulbe.room.persistent.entity.UserTeamRoom;
import com.c203.altteulbe.room.persistent.repository.team.TeamRoomRedisRepository;
import com.c203.altteulbe.room.persistent.repository.team.TeamRoomRepository;
import com.c203.altteulbe.room.persistent.repository.team.UserTeamRoomRepository;
import com.c203.altteulbe.room.service.exception.CannotLeaveRoomException;
import com.c203.altteulbe.room.service.exception.CannotMatchingException;
import com.c203.altteulbe.room.service.exception.DuplicateRoomEntryException;
import com.c203.altteulbe.room.service.exception.NotRoomLeaderException;
import com.c203.altteulbe.room.service.exception.UserNotInRoomException;
import com.c203.altteulbe.room.web.dto.request.RoomGameStartRequestDto;
import com.c203.altteulbe.room.web.dto.request.RoomRequestDto;
import com.c203.altteulbe.room.web.dto.response.RoomEnterResponseDto;
import com.c203.altteulbe.room.web.dto.response.RoomLeaveResponseDto;
import com.c203.altteulbe.room.web.dto.response.TeamMatchResponseDto;
import com.c203.altteulbe.room.web.dto.response.TeamRoomGameStartResponseDto;
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
	private final TeamRoomRepository teamRoomRepository;
	private final UserTeamRoomRepository userTeamRoomRepository;
	private final ProblemRepository problemRepository;
	private final TestcaseRepository testcaseRepository;
	private final GameJPARepository gameRepository;
	private final RoomWebSocketService roomWebSocketService;
	private final RoomValidator validator;

	//@DistributedLock(key="#requestDto.userId")
	public RoomEnterResponseDto enterTeamRoom(RoomRequestDto requestDto) {
		User user = userJPARepository.findByUserId(requestDto.getUserId())
			.orElseThrow(()->new NotFoundUserException());

		// 유저가 이미 방에 존재하는지 검증
		if (validator.isUserInAnyRoom(user.getUserId(), BattleType.S)) {
			throw new DuplicateRoomEntryException();
		}
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
	//@DistributedLock(key = "#requestDto.userId")
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
		String status = teamRoomRedisRepository.getRoomStatus(roomId);

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
		roomWebSocketService.sendWebSocketMessage(roomId.toString(), "LEAVE", responseDto, BattleType.T);
	}

	/**
	 * 팀전 매칭 시작
	 */
	//@DistributedLock(key = "requestDto.roomId")
	public void startTeamMatch(RoomGameStartRequestDto requestDto) {
		Long roomId = requestDto.getRoomId();
		Long leaderId = requestDto.getLeaderId();

		// 방장 여부, 인원 수 충족 여부, 대기 중 여부 검증
		if (!validator.isRoomLeader(roomId, leaderId, BattleType.T)) throw new NotRoomLeaderException();
		if (!validator.isEnoughUsers(roomId, BattleType.T)) throw new NotEnoughUserException();
		if (!teamRoomRedisRepository.getRoomStatus(roomId).equals("waiting")) throw new CannotMatchingException();

		// matching 상태로 변경
		redisTemplate.opsForValue().set(RedisKeys.TeamRoomStatus(roomId), "matching");

		// 매칭 중 상태 전송 후 TEAM_MATCHING_ROOMS에 추가 → 스케줄러가 인식 가능
		roomWebSocketService.sendWebSocketMessage(roomId.toString(), "MATCHING", "대전할 상대를 찾고있어요.", BattleType.T);
		redisTemplate.opsForZSet().add(RedisKeys.TEAM_MATCHING_ROOMS, roomId.toString(), System.currentTimeMillis());

		log.info("팀전 매칭 시작 : roomId = {}", roomId);
	}

	/*
	 * 스케줄러가 매칭할 팀을 찾은 후 실행되는 작업
	 */
	public void afterTeamMatch(String roomId1, String roomId2) {

		// Redis에서 두 팀을 매칭 중 상태에서 제거
		redisTemplate.opsForZSet().remove(RedisKeys.TEAM_MATCHING_ROOMS, roomId1);
		redisTemplate.opsForZSet().remove(RedisKeys.TEAM_MATCHING_ROOMS, roomId2);

		// 해당 메시지를 전송받으면 "/sub/team/room/{matchId}"를 구독시켜야 함
		roomWebSocketService.sendWebSocketMessage(roomId1, "MATCHED", "매칭이 완료되었습니다.", BattleType.T);
		roomWebSocketService.sendWebSocketMessage(roomId2, "MATCHED", "매칭이 완료되었습니다.", BattleType.T);
		String matchId = generateMatchId(roomId1, roomId2);
		log.info("matchId = {}", matchId);

		// 각 팀의 유저 정보를 가져오는 메소드 호출
		TeamMatchResponseDto teamMatchDto = getTeamMatchResponseDto(Long.parseLong(roomId1), Long.parseLong(roomId2));

		// 두 팀의 정보를 websocket으로 전송 후 카운팅 시작
		roomWebSocketService.sendWebSocketMessage(matchId, "COUNTING", teamMatchDto, BattleType.T);
		startCountingTeam(Long.parseLong(roomId1), Long.parseLong(roomId2), matchId, teamMatchDto);
	}


	/*
	 * 두 팀에 대한 카운팅 시작
	 */
	private void startCountingTeam(Long roomId1, Long roomId2, String matchId, TeamMatchResponseDto teamMatchDto) {
		if (!validator.isRoomMatching(roomId1)) throw new GameCannotStartException();
		if (!validator.isRoomMatching(roomId2)) throw new GameCannotStartException();
		if (!validator.isEnoughUsers(roomId1, BattleType.T)) throw new NotEnoughUserException();
		if (!validator.isEnoughUsers(roomId2, BattleType.T)) throw new NotEnoughUserException();

		// Redis에서 두 팀을 대기 중 상태에서 제거
		redisTemplate.opsForZSet().remove(RedisKeys.TEAM_WAITING_ROOMS, roomId1.toString());
		redisTemplate.opsForZSet().remove(RedisKeys.TEAM_WAITING_ROOMS, roomId2.toString());

		// 두 팀의 상태를 counting으로 변경
		redisTemplate.opsForValue().set(RedisKeys.TeamRoomStatus(roomId1), "counting");
		redisTemplate.opsForValue().set(RedisKeys.TeamRoomStatus(roomId2), "counting");

		// 카운트다운 시작 → Scheduler가 인식
		redisTemplate.opsForValue().set(RedisKeys.TeamRoomCountdown(matchId), "5");
	}

	/**
	 * 팀전 게임 시작 처리
	 */
	@Transactional
	public void startGameAfterCountDown(String matchId, Long roomId1, Long roomId2) {
		// 최소 인원 수 검증
		if (!validator.isEnoughUsers(roomId1, BattleType.T)) {
			roomWebSocketService.sendWebSocketMessage(matchId, "COUNTING_CANCEL", "최소 인원 수가 미달되었습니다.", BattleType.T);
			return;
		}
		if (!validator.isEnoughUsers(roomId1, BattleType.T)) {
			roomWebSocketService.sendWebSocketMessage(matchId, "COUNTING_CANCEL", "최소 인원 수가 미달되었습니다.", BattleType.T);
			return;
		}

		// 문제 및 테스트케이스 조회
		List<Long> problemIds = problemRepository.findAllProblemIds();
		if (problemIds.isEmpty()) {
			throw new ProblemNotFoundException();
		}
		Long randomProblemId = problemIds.get(new Random().nextInt(problemIds.size()));
		Problem problemEntity = problemRepository.findById(randomProblemId)
												 .orElseThrow(ProblemNotFoundException::new);

		List<Testcase> testcaseEntities = testcaseRepository.findTestcasesByProblemId(problemEntity.getId());

		// DB에 Game 저장
		Game game = Game.create(problemEntity, BattleType.T);
		gameRepository.save(game);

		// DB에 TeamRoom 저장
		TeamRoom teamRoom1 = TeamRoom.create(game);
		teamRoomRepository.save(teamRoom1);
		saveUserTeamRooms(roomId1, teamRoom1);

		TeamRoom teamRoom2 = TeamRoom.create(game);
		teamRoomRepository.save(teamRoom2);
		saveUserTeamRooms(roomId2, teamRoom2);

		// websocket으로 전송할 데이터 준비
		RoomEnterResponseDto team1Dto = getRoomEnterResponseDto(roomId1);
		RoomEnterResponseDto team2Dto = getRoomEnterResponseDto(roomId2);

		GameStartForProblemDto problem = GameStartForProblemDto.from(problemEntity);
		List<GameStartForTestcaseDto> testcases = testcaseEntities.stream()
														.map(GameStartForTestcaseDto::from)
														.collect(Collectors.toList());

		TeamRoomGameStartResponseDto responseDto = TeamRoomGameStartResponseDto.from(game.getId(), team1Dto,
																					 team2Dto, problem, testcases);
		redisTemplate.opsForValue().set(RedisKeys.TeamRoomStatus(roomId1), "gaming");
		redisTemplate.opsForValue().set(RedisKeys.TeamRoomStatus(roomId2), "gaming");

		roomWebSocketService.sendWebSocketMessage(matchId, "GAME_START", responseDto, BattleType.T);
		log.info("각 팀에게 게임 시작 메시지 전송 완료");
	}

	/**
	 * Redis에서 유저 ID를 조회하고, DB에 UserTeamRoom을 저장하는 메소드
	 */
	@Transactional
	private void saveUserTeamRooms(Long roomId, TeamRoom teamRoom) {
		// Redis에서 유저 ID 조회
		String roomUsersKey = RedisKeys.TeamRoomUsers(roomId);
		List<String> userIds1 = redisTemplate.opsForList().range(roomUsersKey, 0, -1);

		List<Long> userIdList = userIds1.stream().map(Long::parseLong).collect(Collectors.toList());
		List<User> usersFromDb = userJPARepository.findByUserIdIn(userIdList);

		// 조회된 User들을 Map으로 변환 (ID → User)
		Map<Long, User> userMap = usersFromDb.stream()
			.collect(Collectors.toMap(User::getUserId, user -> user));

		// userIds1의 순서대로 User 리스트 정렬
		List<User> users = userIdList.stream()
			.map(userMap::get)
			.filter(Objects::nonNull)
			.collect(Collectors.toList());

		// DB에 UserTeamRoom 저장
		for (int i = 0; i < users.size(); i++) {
			User user = users.get(i);
			UserTeamRoom userTeamRoom = UserTeamRoom.create(teamRoom, user, i);
			userTeamRoomRepository.save(userTeamRoom);
		}
	}

	/**
	 * 각 팀의 유저 정보를 조회하여 TeamMatchResponseDto로 변환하는 메소드
	 */
	private TeamMatchResponseDto getTeamMatchResponseDto(Long roomId1, Long roomId2) {
		RoomEnterResponseDto responseDto1 = getRoomEnterResponseDto(roomId1);
		RoomEnterResponseDto responseDto2 = getRoomEnterResponseDto(roomId2);
		return TeamMatchResponseDto.toDto(responseDto1, responseDto2);
	}


	/**
	 * 특정 팀의 유저 정보를 조회하여 RoomEnterResponseDto로 변환하는 메소드
	 */
	private RoomEnterResponseDto getRoomEnterResponseDto(Long roomId) {
		String roomUsersKey = RedisKeys.TeamRoomUsers(roomId);
		String leaderId = redisTemplate.opsForList().index(roomUsersKey, 0);
		List<String> userIds = redisTemplate.opsForList().range(roomUsersKey, 0, -1);
		return teamRoomRedisRepository.convertToRoomEnterResponseDto(roomId, leaderId, userIds);
	}

	/**
	 * 매칭되는 순간부터 두 팀을 함께 관리하기 위한 pk 생성 메소드
	 */
	public String generateMatchId(String team1Id, String team2Id) {
		List<String> teamIds = Arrays.asList(team1Id, team2Id);
		Collections.sort(teamIds);
		return teamIds.get(0) + "-" + teamIds.get(1);
	}


	// userId 리스트로 User 엔티티 조회
	private List<User> getUserByIds(List<String> userIds) {
		return userJPARepository.findByUserIdIn(
			userIds.stream().map(Long::parseLong).collect(Collectors.toList())
		);
	}
}