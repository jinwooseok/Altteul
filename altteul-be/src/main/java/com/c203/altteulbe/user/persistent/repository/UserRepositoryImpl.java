package com.c203.altteulbe.user.persistent.repository;

import static com.c203.altteulbe.ranking.persistent.entity.QTier.*;
import static com.c203.altteulbe.ranking.persistent.entity.QTodayRanking.*;
import static com.c203.altteulbe.user.persistent.entity.QUser.*;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.c203.altteulbe.user.persistent.entity.User;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

	private final JPAQueryFactory jpaQueryFactory;

	@Override
	public boolean existsByUsername(String username) {
		Integer fetchOne = jpaQueryFactory
			.selectOne()
			.from(user)
			.where(user.username.eq(username))
			.fetchFirst();
		return fetchOne != null;
	}

	@Override
	public boolean existsByNickname(String nickname) {
		Integer fetchOne = jpaQueryFactory
			.selectOne()
			.from(user)
			.where(user.nickname.eq(nickname))
			.fetchFirst();
		return fetchOne != null;
	}

	@Override
	public Optional<User> findByUsername(String username) {
		return Optional.ofNullable(jpaQueryFactory
			.selectFrom(user)
			.where(user.username.eq(username))
			.fetchOne()
		);
	}

	@Override
	public Optional<User> findByProviderAndUsername(User.Provider provider, String username) {
		System.out.println(provider);
		return Optional.ofNullable(jpaQueryFactory
			.selectFrom(user)
			.where(user.username.eq(username)
				.and(user.provider.eq(provider)))
			.fetchOne()
		);
	}

	public Optional<User> findWithRankingByUserId(Long userId) {
		return Optional.ofNullable(
			jpaQueryFactory
				.selectFrom(user)
				.leftJoin(user.todayRanking, todayRanking).fetchJoin()
				.leftJoin(user.tier, tier).fetchJoin()
				.where(user.userId.eq(userId))
				.fetchOne()
		);
	}

}