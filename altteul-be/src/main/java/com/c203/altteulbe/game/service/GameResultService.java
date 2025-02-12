package com.c203.altteulbe.game.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.c203.altteulbe.common.dto.BattleType;
import com.c203.altteulbe.game.persistent.entity.Game;
import com.c203.altteulbe.game.persistent.repository.game.GameCustomRepository;
import com.c203.altteulbe.game.persistent.repository.game.GameRepository;
import com.c203.altteulbe.game.service.exception.GameNotFoundException;
import com.c203.altteulbe.game.service.exception.GameNotParticipatedException;
import com.c203.altteulbe.game.web.dto.result.response.GameResultResponseDto;
import com.c203.altteulbe.game.web.dto.result.response.TeamInfo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class GameResultService {
	private final GameRepository gameRepository;



	public GameResultResponseDto getGameResult(Long gameId, Long userId) {
		Game game = gameRepository.findWithAllMemberByGameId(gameId)
			.orElseThrow(GameNotFoundException::new);

		List<TeamInfo> teamInfos = extractTeamInfos(game);

		TeamInfo myTeam = teamInfos.stream()
			.filter(teamInfo -> teamInfo.getMembers().stream()
				.anyMatch(member -> member.getUserId().equals(userId))) // `anyMatch()`로 검사
			.findFirst()
			.orElseThrow(GameNotParticipatedException::new);


		// 모든 해당 팀을 리스트로 변환 후 추가
		List<TeamInfo> opponents = new ArrayList<>(teamInfos.stream()
			.filter(teamInfo -> teamInfo.getMembers().stream()
				.noneMatch(member -> member.getUserId().equals(userId))) // 조건 만족하는 모든 팀 필터링
			.toList());
		return GameResultResponseDto.from(game, myTeam, opponents);
	}

	private static List<TeamInfo> extractTeamInfos(Game game) {
		if (game.getBattleType() == BattleType.S) {
			// 개인방 정보 변환 (TeamInfo 형식으로 변환)
			return game.getSingleRooms().stream()
				.map(room -> TeamInfo.fromSingleRoom(room, game.getProblem().getTotalCount())) // 추가 인자 전달
				.toList();
		} else {
			// 팀방 정보 변환
			return game.getTeamRooms().stream()
				.map(room -> TeamInfo.fromTeamRoom(room, game.getProblem().getTotalCount()))
				.toList();
		}
	}
}
