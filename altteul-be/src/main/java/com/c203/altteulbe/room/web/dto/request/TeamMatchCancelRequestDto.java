package com.c203.altteulbe.room.web.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TeamMatchCancelRequestDto {
	private Long userId;
	private Long roomId;
}
