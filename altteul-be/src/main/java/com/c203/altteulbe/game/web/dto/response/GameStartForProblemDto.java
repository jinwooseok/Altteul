package com.c203.altteulbe.game.web.dto.response;

import com.c203.altteulbe.game.persistent.entity.Problem;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class GameStartForProblemDto {
	private Long problemId;
	private String problemTitle;
	private String description;

	public static GameStartForProblemDto from(Problem problem) {
		return GameStartForProblemDto.builder()
									 .problemId(problem.getId())
									 .problemTitle(problem.getProblemTitle())
									 .description(problem.getDescription())
									 .build();
	}
}
