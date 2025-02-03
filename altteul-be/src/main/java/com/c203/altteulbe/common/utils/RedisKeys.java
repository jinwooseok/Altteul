package com.c203.altteulbe.common.utils;

public class RedisKeys {

	// 방 ID 자동 증가 카운터 (개인전 + 팀전)
	public static final String ROOM_ID_COUNTER = "room:both:id_counter";

	// 개인전 대기 중인 방 목록
	public static final String SINGLE_WAITING_ROOMS = "room:single:waiting_rooms";

	// 친구 관계
	private static final String FRIEND_RELATION_CACHE = "friendRelation";

	// 친구 요청
	private static final String FRIEND_REQUEST_CACHE = "friendRequests";

	// 친구 리스트
	private static final String FRIEND_LIST_CACHE = "friendList";

	// 유저 상태
	private static final String USER_STATUS = "user_status";

	// 개인전 방 상태
	public static String SingleRoomStatus(Long roomId) {
		return "room:single:" + roomId + ":status";
	}

	// 개인전 방에 속한 유저 목록
	public static String SingleRoomUsers(Long roomId) {
		return "room:single:" + roomId + ":users";
	}

	// 특정 유저가 속한 개인전 방 정보
	public static String userSingleRoom(Long userId) {
		return "user:" + userId + ":single_room";
	}

	// 개인전 방 카운팅 관리
	public static String SingleRoomCountdown(Long roomId) {
		return "room:single:" + roomId + ":countdown";
	}

	// 친구 요청 키
	public static String geFriendRequestKey(Long userId) {
		return FRIEND_REQUEST_CACHE + ":" + userId;
	}

	// 친구 관계 키
	public static String getFriendRelationKey(Long userId) {
		return FRIEND_RELATION_CACHE + ":" + userId;
	}

	// 유저 상태 키
	public static String getUserStatusKey(Long userId) {
		return USER_STATUS + ":" + userId;
	}

	// 친구 리스트 키
	public static String getFriendListKey(Long userId) {
		return FRIEND_LIST_CACHE + ":" + userId;
	}
}
