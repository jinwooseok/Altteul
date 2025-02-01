package com.c203.altteulbe.friend.web.dto.response;

import com.c203.altteulbe.common.dto.RequestStatus;
import com.c203.altteulbe.friend.persistent.entity.FriendRequest;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FriendRequestResponseDto {

	@NotNull
	public Long friendRequestId;

	@NotNull
	public Long fromUserId;

	@NotNull
	public String fromUserNickname;

	@NotNull
	public String fromUserProfileImg;

	@NotNull
	public RequestStatus requestStatus;

	public static FriendRequestResponseDto from(FriendRequest friendRequest) {
		return FriendRequestResponseDto.builder()
			.friendRequestId(friendRequest.getId())
			.fromUserId(friendRequest.getFrom().getUserId())
			.fromUserNickname(friendRequest.getFrom().getNickname())
			.fromUserProfileImg(friendRequest.getFrom().getProfileImg())
			.requestStatus(friendRequest.getRequestStatus())
			.build();
	}
}
