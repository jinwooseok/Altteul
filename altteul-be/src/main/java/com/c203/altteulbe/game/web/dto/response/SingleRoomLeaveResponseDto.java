package com.c203.altteulbe.game.web.dto.response;

import java.util.List;

import com.c203.altteulbe.user.web.dto.response.UserInfoResponseDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class SingleRoomLeaveResponseDto {
	private Long roomId;
	private Long leaderId;
	private UserInfoResponseDto leftUser;             // 떠난 유저
	private List<UserInfoResponseDto> remainingUsers; // 남은 유저

	public static SingleRoomLeaveResponseDto toResponse(Long roomId, Long leaderId,
														UserInfoResponseDto leftUser,
														List<UserInfoResponseDto> remainingUsers) {
		return SingleRoomLeaveResponseDto.builder()
										 .roomId(roomId)
										 .leaderId(leaderId)
										 .leftUser(leftUser)
										 .remainingUsers(remainingUsers)
										 .build();
	}

}
