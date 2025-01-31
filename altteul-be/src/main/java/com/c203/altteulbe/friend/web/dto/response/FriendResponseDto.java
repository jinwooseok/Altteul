package com.c203.altteulbe.friend.web.dto.response;

import com.c203.altteulbe.friend.persistent.entity.Friendship;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@Getter
@NoArgsConstructor
public class FriendResponseDto {

	@NotNull
	public Long id;

	@NotNull(message = "Nickname is required")
	public String nickname;

	public String profileImg;

	public Boolean isOnline;

	public static FriendResponseDto from(Friendship friendship, boolean isOnline) {
		return new FriendResponseDto(
			friendship.getFriend().getUserId(),
			friendship.getFriend().getNickname(),
			friendship.getFriend().getProfileImg(),
			isOnline
		);
	}
}
