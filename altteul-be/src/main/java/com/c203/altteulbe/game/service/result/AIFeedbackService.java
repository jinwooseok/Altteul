package com.c203.altteulbe.game.service.result;

import java.util.Map;

import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.boot.actuate.logging.LoggersEndpoint;
import org.springframework.stereotype.Service;

import com.c203.altteulbe.common.dto.BattleType;
import com.c203.altteulbe.common.dto.Language;
import com.c203.altteulbe.game.persistent.entity.Game;
import com.c203.altteulbe.game.persistent.entity.problem.Problem;
import com.c203.altteulbe.game.persistent.repository.game.GameRepository;
import com.c203.altteulbe.game.persistent.repository.problem.ProblemRepository;
import com.c203.altteulbe.game.service.exception.GameNotFoundException;
import com.c203.altteulbe.game.web.dto.result.request.AIFeedbackRequestDto;
import com.c203.altteulbe.game.web.dto.result.response.AIFeedbackResponse;
import com.c203.altteulbe.room.persistent.entity.Room;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AIFeedbackService {
	private final ChatModel chatModel;
	private final GameRepository gameRepository;
	private final LoggersEndpoint loggersEndpoint;

	public AIFeedbackResponse getEvaluation(AIFeedbackRequestDto request) {
		// 유저 프롬프트 템플릿 로드 및 변수 설정

		Game game = gameRepository.findWithRoomAndProblemByGameIdAndTeamId(request.getGameId(), request.getTeamId())
			.orElseThrow(GameNotFoundException::new);

		String userPromptTemplate;

		if (game.getBattleType() == BattleType.S) {
			if (game.getSingleRooms().get(0).getCode() == null) throw new NullPointerException();
			userPromptTemplate = getPrompt(game.getProblem(), game.getSingleRooms().get(0));
		} else {
			if (game.getTeamRooms().get(0).getCode() == null) throw new NullPointerException();
			userPromptTemplate = getPrompt(game.getProblem(), game.getTeamRooms().get(0));
		}

		System.out.println(userPromptTemplate);

		PromptTemplate userTemplate = new PromptTemplate(userPromptTemplate);
		String userCommand = userTemplate.render();

		String systemPromptTemplate = """
    너는 알고리즘 최적화 전문가야. 내가 제공하는 코드의 시간 복잡도와 공간 복잡도를 분석하고, 더 효율적인 방법이 있다면 설명해줘. 가능한 경우 코드의 시간 복잡도를 줄이기 위한 대안을 제시해줘.
	
    결과는 이런식으로 한국말로 도출해줘:
    {
        "feedback": [
            {
                "code": "%s",
                "description": "%s"
            }
        ],
        "algorithmType": ["%s"],
        "summary": "%s"
    }
    """;

		String systemCommand = String.format(systemPromptTemplate, "O(n^2) -> O(n log n)", "버블 정렬 대신 퀵 정렬 사용", "정렬 알고리즘", "퀵 정렬이 더 성능이 좋음");
		// 메시지 생성
		Message userMessage = new UserMessage(userCommand);
		Message systemMessage = new SystemMessage(systemCommand);

		// API 호출
		String response = chatModel.call(userMessage, systemMessage);
		log.info(response);
		return AIFeedbackResponse
			.builder()
			.content(response)
			.build();
	}

	private String getPrompt(Problem problem, Room room) {
		String mdLanguage;
		switch (room.getLang()) {
			case JV -> mdLanguage = "java";
			case PY -> mdLanguage = "python";
			default -> throw new IllegalStateException("Unexpected value: " + room.getLang());
		}
		return """
        문제 설명: %s
        유저 코드:
        ```%s
        %s
        ```
        위의 코드에 대해 개선할 점을 리뷰해주세요. 코드 스타일, 최적화, 버그 가능성을 포함해서 상세한 피드백을 작성해주세요.
        """.formatted(problem.getDescription(), mdLanguage, room.getCode());
	}
}
