package com.c203.altteulbe.room.persistent.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.c203.altteulbe.common.dto.BattleResult;
import com.c203.altteulbe.common.dto.Language;
import com.c203.altteulbe.common.entity.BaseCreatedEntity;
import com.c203.altteulbe.game.persistent.entity.Game;
import com.c203.altteulbe.game.persistent.entity.item.ItemHistory;

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
import jakarta.persistence.OneToMany;
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
public class TeamRoom extends BaseCreatedEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "team_room_id", nullable = false, updatable = false)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "game_id")
	private Game game;

	@OneToMany(mappedBy = "teamRoom")
	private List<UserTeamRoom> userTeamRooms = new ArrayList<>();

	@OneToMany(mappedBy = "teamRoom")
	private List<ItemHistory> itemHistories = new ArrayList<>();

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

	public static TeamRoom create(Game game) {
		return TeamRoom.builder()
					   .game(game)
					   .activation(true)
					   .build();
	}
}
