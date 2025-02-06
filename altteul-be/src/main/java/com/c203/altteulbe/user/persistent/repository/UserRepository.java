package com.c203.altteulbe.user.persistent.repository;

import java.util.Optional;

import com.c203.altteulbe.user.persistent.entity.User;


public interface UserRepository {
	boolean existsByUsername(String username);

	boolean existsByNickname(String nickname);

	Optional<User> findByUsername(String username);
	Optional<User> findByProviderAndUsername(User.Provider provider, String username);
}
