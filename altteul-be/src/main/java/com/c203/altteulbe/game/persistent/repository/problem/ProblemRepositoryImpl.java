package com.c203.altteulbe.game.persistent.repository.problem;

import static com.c203.altteulbe.game.persistent.entity.QLangLimit.*;
import static com.c203.altteulbe.game.persistent.entity.QProblem.*;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.c203.altteulbe.common.dto.Language;
import com.c203.altteulbe.game.persistent.entity.Problem;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ProblemRepositoryImpl implements ProblemRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	@Override
	public List<Long> findAllProblemIds() {
		return queryFactory
						.select(problem.id)
						.from(problem)
						.fetch();
	}

	@Override
	public Optional<Problem> findWithLangByProblemIdAndLang(Long problemId, Language language) {
		return Optional.ofNullable(
			queryFactory
				.selectFrom(problem)
				.leftJoin(langLimit).on(problem.id.eq(langLimit.problem.id)).fetchJoin()
				.where(problem.id.eq(problemId).and(langLimit.lang.eq(language)))
				.fetchOne()
		);
	}
}
