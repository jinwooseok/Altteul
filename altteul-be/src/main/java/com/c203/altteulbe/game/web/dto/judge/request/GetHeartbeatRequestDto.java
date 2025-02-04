package com.c203.altteulbe.game.web.dto.judge.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetHeartbeatRequestDto {
	private int cpu;
	private int memory;
}
