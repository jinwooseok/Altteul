package com.c203.altteulbe.room.persistent.entity;

import java.time.LocalDateTime;

import com.c203.altteulbe.common.dto.BattleResult;
import com.c203.altteulbe.common.dto.Language;
import com.c203.altteulbe.common.entity.BaseCreatedEntity;
import com.c203.altteulbe.game.persistent.entity.Game;
import com.c203.altteulbe.user.persistent.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class SingleRoom extends BaseCreatedEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "single_room_id", nullable = false, updatable = false)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "game_id")
	private Game game;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	private int matchingOrder;

	@Column(columnDefinition = "TEXT")
	private String code;

	private Integer solvedTestcaseCount;

	@Enumerated(EnumType.STRING)
	private BattleResult battleResult;

	private Integer rewardPoint;
	private String lastExecuteTime;
	private String lastExecuteMemory;

	@Enumerated(EnumType.STRING)
	private Language lang;

	private boolean activation;
	private LocalDateTime finishTime;

	public static SingleRoom create(Game game, User user, int order) {
		return SingleRoom.builder()
						 .game(game)
						 .user(user)
						 .matchingOrder(order)
						 .activation(true)
						 .build();
	}
}
