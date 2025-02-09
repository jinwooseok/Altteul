package com.c203.altteulbe.game.web.dto.record.response;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.c203.altteulbe.common.dto.BattleResult;
import com.c203.altteulbe.common.dto.Language;
import com.c203.altteulbe.room.persistent.entity.SingleRoom;
import com.c203.altteulbe.room.persistent.entity.TeamRoom;
import com.c203.altteulbe.room.persistent.entity.UserTeamRoom;
import com.c203.altteulbe.user.persistent.entity.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamInfo {
	private Long teamId;
	private BattleResult gameResult;
	private Language lang;
	private int totalHeadCount;
	private String executeTime;
	private String executeMemory;
	private Integer bonusPoint;
	private String duration;
	private String code;
	private LocalDateTime createdAt;
	private List<TeamMember> members;

	// ✅ TeamRoom 변환 메서드
	public static TeamInfo fromTeamRoom(TeamRoom room) {
		String duration;
		if (room.getFinishTime() == null) {
			duration = "진행중";
		} else {
			duration = fromDurationToMinuteAndSecond(Duration.between(room.getCreatedAt(), room.getFinishTime()));
		}

		return TeamInfo.builder()
			.teamId(room.getId())
			.gameResult(room.getBattleResult())
			.lang(room.getLang())
			.totalHeadCount(room.getUserTeamRooms().size())
			.executeTime(room.getLastExecuteTime())
			.executeMemory(room.getLastExecuteMemory())
			.bonusPoint(room.getRewardPoint())
			.duration(duration)
			.code(room.getCode())
			.createdAt(room.getCreatedAt()) // 정렬용 필드
			.members(room.getUserTeamRooms().stream()
				.map(TeamMember::fromUserTeamRoom)
				.collect(Collectors.toList()))
			.build();
	}

	// ✅ TeamRoom 변환 메서드
	public static TeamInfo fromSingleRoom(SingleRoom room) {
		String duration;
		if (room.getFinishTime() == null) {
			duration = "진행중";
		} else {
			duration = fromDurationToMinuteAndSecond(Duration.between(room.getCreatedAt(), room.getFinishTime()));
		}
		System.out.println(TeamMember.fromUser(room.getUser()).userId);
		return TeamInfo.builder()
			.teamId(room.getId())
			.gameResult(room.getBattleResult())
			.lang(room.getLang())
			.totalHeadCount(1)
			.executeTime(room.getLastExecuteTime())
			.executeMemory(room.getLastExecuteMemory())
			.bonusPoint(room.getRewardPoint())
			.duration(duration)
			.code(room.getCode())
			.createdAt(room.getCreatedAt()) // 정렬용 필드
			.members(Collections.singletonList( // 개인방 유저 1명만
				TeamMember.fromUser(room.getUser())
			))
			.build();
	}

	@ToString
	@Getter
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class TeamMember {
		private Long userId;
		private String nickname;
		private String profileImage;
		private Long rank;
		private Long tierId;

		public static TeamMember fromUserTeamRoom(UserTeamRoom userTeamRoom) {
			return TeamMember.builder()
				.userId(userTeamRoom.getUser().getUserId())
				.nickname(userTeamRoom.getUser().getNickname())
				.profileImage(userTeamRoom.getUser().getProfileImg())
				.rank(userTeamRoom.getUser().getTodayRanking().getId())
				.tierId(userTeamRoom.getUser().getTier().getId())
				.build();
		}

		public static TeamMember fromUser(User user) {
			return TeamMember.builder()
				.userId(user.getUserId())
				.nickname(user.getNickname())
				.profileImage(user.getProfileImg())
				.rank(user.getTodayRanking().getId())
				.tierId(user.getTier().getId())
				.build();
		}
	}

	private static String fromDurationToMinuteAndSecond(Duration duration) {

		// 분 & 초 추출
		long minutes = duration.toMinutes();
		long seconds = duration.getSeconds() % 60; // 남은 초 계산

		// 출력
		return minutes + "분 " + seconds + "초";
	}
}
