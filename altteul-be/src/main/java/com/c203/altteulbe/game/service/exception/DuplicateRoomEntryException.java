package com.c203.altteulbe.game.service.exception;

import org.springframework.http.HttpStatus;

import com.c203.altteulbe.common.exception.BusinessException;

public class DuplicateRoomEntryException extends BusinessException {
	public DuplicateRoomEntryException() {
		super("이미 다른 방에 접속 중입니다.", HttpStatus.BAD_REQUEST);
	}
}
