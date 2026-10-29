package com.gabolle.backend.trip;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import com.zaxxer.hikari.HikariDataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import com.gabolle.backend.recommendation.support.PostgresAvailableCondition;
import com.gabolle.backend.recommendation.support.TestDatabase;
import com.gabolle.backend.trip.domain.PersonalizationScope;
import com.gabolle.backend.trip.domain.PreferenceSnapshot;
import com.gabolle.backend.trip.domain.Trip;
import com.gabolle.backend.trip.domain.TripConstraint;
import com.gabolle.backend.trip.domain.TripMember;
import com.gabolle.backend.trip.domain.TripRepository;
import com.gabolle.testslice.TripSliceApplication;

/**
 * 여행·멤버·취향 스냅샷·제약·멱등 키가 실제 PostgreSQL 에 저장·복원되는지 —
 * S15P21E201-461.
 *
 * 2026-09-04 — 고지혁 님이 운영에서 preference_answer=0 · constraint_answer=0 을
 * 실측해 찾은 유실을 고친 뒤 범위를 넓혔다. H2 가 아니라 진짜 PostgreSQL 을 쓰는
 * 이유는 TIMESTAMPTZ·UUID·JSONB·CHECK 제약이 PostgreSQL 에서만 진짜로 검증되기
 * 때문이다.
 */
@SpringBootTest(classes = TripSliceApplication.class, properties = {
		"spring.profiles.active=db",
		"spring.jpa.hibernate.ddl-auto=none",
		"spring.flyway.enabled=true"
})
@ExtendWith(PostgresAvailableCondition.class)
class TripPersistenceIntegrationTest {

	/** 동시 요청 테스트에서 8개 요청이 모두 DB 연결을 잡고 동시에 들어가게 한다. */
	private static final int CONCURRENT_REQUESTS = 8;

	@DynamicPropertySource
	static void datasourceProperties(DynamicPropertyRegistry registry) {
		TestDatabase.registerDatasource(registry);
		// TestDatabase 의 기본 상한(4)으로는 8개 중 4개가 연결을 기다리며 줄을 선다.
		// 이 클래스만 상한을 올려, 같은 키의 INSERT 가 실제로 겹치게 한다.
		registry.add("spring.datasource.hikari.maximum-pool-size", () -> String.valueOf(CONCURRENT_REQUESTS + 2));
	}

	@Autowired
	private TripRepository tripRepository;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private DataSource dataSource;

	private String userId;

	@BeforeEach
	void seedOwner() {
		userId = UUID.randomUUID().toString();
		OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
		jdbcTemplate.update(
				"INSERT INTO app_user (user_id, display_name, language, personalization_mode, status, created_at, updated_at) "
						+ "VALUES (?, 'test', 'ko', 'EXPLICIT_ONLY', 'ACTIVE', ?, ?)",
				UUID.fromString(userId), now, now);
	}

	private Trip newTrip(String tripId, Instant now) {
		return new Trip(tripId, userId,
				LocalDate.of(2026, 9, 6), LocalDate.of(2026, 9, 8),
				35.1587, 129.1604, 300000, 2,
				"MORNING_TO_EVENING", "Asia/Seoul", now);
	}

	@Test
	void tripRoundTripsThroughPostgres() {
		Instant now = Instant.now().truncatedTo(ChronoUnit.MICROS);
		String tripId = UUID.randomUUID().toString();

		Trip trip = newTrip(tripId, now);
		TripMember owner = TripMember.owner(UUID.randomUUID().toString(), tripId, userId, now);
		PreferenceSnapshot snapshot = new PreferenceSnapshot(
				UUID.randomUUID().toString(), tripId, 1, List.of(), PersonalizationScope.TRIP, List.of(), now);

		tripRepository.save(trip, List.of(), owner, snapshot);

		Trip found = tripRepository.findById(tripId).orElseThrow();

		assertThat(found.tripId()).isEqualTo(tripId);
		assertThat(found.createdBy()).isEqualTo(userId);
		assertThat(found.startDate()).isEqualTo(LocalDate.of(2026, 9, 6));
		assertThat(found.finishDate()).isEqualTo(LocalDate.of(2026, 9, 8));
		assertThat(found.originLat()).isEqualTo(35.1587);
		assertThat(found.originLng()).isEqualTo(129.1604);
		assertThat(found.budgetKrw()).isEqualTo(300000);
		assertThat(found.partySize()).isEqualTo(2);
		assertThat(found.timeWindow()).isEqualTo("MORNING_TO_EVENING");
		assertThat(found.timezone()).isEqualTo("Asia/Seoul");
		assertThat(found.status()).isEqualTo(Trip.Status.PLANNING);
		assertThat(found.createdAt()).isEqualTo(now);
		assertThat(found.updatedAt()).isEqualTo(now);
		assertThat(found.deletedAt()).isNull();
	}

	@Test
	void unknownTripIdIsEmpty() {
		assertThat(tripRepository.findById(UUID.randomUUID().toString())).isEmpty();
	}

	@Test
	void ownerIsPersistedAsMember() {
		Instant now = Instant.now().truncatedTo(ChronoUnit.MICROS);
		String tripId = UUID.randomUUID().toString();
		Trip trip = newTrip(tripId, now);
		TripMember owner = TripMember.owner(UUID.randomUUID().toString(), tripId, userId, now);
		PreferenceSnapshot snapshot = new PreferenceSnapshot(
				UUID.randomUUID().toString(), tripId, 1, List.of(), PersonalizationScope.TRIP, List.of(), now);

		tripRepository.save(trip, List.of(), owner, snapshot);

		List<TripMember> members = tripRepository.findMembers(tripId);
		assertThat(members).hasSize(1);
		assertThat(members.get(0).userId()).isEqualTo(userId);
		assertThat(members.get(0).role()).isEqualTo(TripMember.Role.OWNER);
	}

	@Test
	void preferenceAnswerAndConstraintRoundTrip() {
		Instant now = Instant.now().truncatedTo(ChronoUnit.MICROS);
		String tripId = UUID.randomUUID().toString();
		Trip trip = newTrip(tripId, now);
		TripMember owner = TripMember.owner(UUID.randomUUID().toString(), tripId, userId, now);

		// 🔴 dimension 은 ck_preference_answer_dimension 이 허용하는 8종 중 하나여야
		//    한다 — "pace" 는 그 목록에 없어 CHECK 위반으로 CI 에서 걸렸다(도커 없는
		//    로컬에서는 이 테스트가 스킵돼 못 잡았다). value 도 valid JSON 이어야
		//    한다 — JSONB 컬럼은 따옴표 없는 문자열을 안 받는다.
		PreferenceSnapshot snapshot = new PreferenceSnapshot(
				UUID.randomUUID().toString(), tripId, 1,
				List.of(new PreferenceSnapshot.PreferenceAnswer(
						"SLOPE_PREFERENCE", "\"RELAXED\"", PreferenceSnapshot.AnswerStatus.SELECTED)),
				PersonalizationScope.TRIP, List.of(), now);

		TripConstraint constraint = new TripConstraint(
				UUID.randomUUID().toString(), tripId, "MOBILITY", "MAX_WALKING_METERS",
				TripConstraint.Severity.HARD, "LTE", null, 5000.0,
				TripConstraint.EvidenceStatus.NEEDS_REVIEW, TripConstraint.AnswerStatus.SELECTED,
				PersonalizationScope.TRIP, null);

		tripRepository.save(trip, List.of(constraint), owner, snapshot);

		PreferenceSnapshot foundSnapshot = tripRepository.findLatestSnapshot(tripId).orElseThrow();
		assertThat(foundSnapshot.version()).isEqualTo(1);
		var slope = foundSnapshot.answers().stream()
				.filter(a -> a.dimension().equals("SLOPE_PREFERENCE")).findFirst().orElseThrow();
		assertThat(slope.valueJson()).isEqualTo("\"RELAXED\"");
		assertThat(slope.status()).isEqualTo(PreferenceSnapshot.AnswerStatus.SELECTED);

		List<TripConstraint> foundConstraints = tripRepository.findConstraints(tripId);
		assertThat(foundConstraints).hasSize(1);
		TripConstraint found = foundConstraints.get(0);
		assertThat(found.type()).isEqualTo("MOBILITY");
		assertThat(found.constraintKey()).isEqualTo("MAX_WALKING_METERS");
		assertThat(found.severity()).isEqualTo(TripConstraint.Severity.HARD);
		assertThat(found.threshold()).isEqualTo(5000.0);
		assertThat(found.answerStatus()).isEqualTo(TripConstraint.AnswerStatus.SELECTED);
	}

	@Test
	void codedAllergyConstraintRoundTrips() {
		Instant now = Instant.now().truncatedTo(ChronoUnit.MICROS);
		String tripId = UUID.randomUUID().toString();
		Trip trip = newTrip(tripId, now);
		TripMember owner = TripMember.owner(UUID.randomUUID().toString(), tripId, userId, now);
		PreferenceSnapshot snapshot = new PreferenceSnapshot(
				UUID.randomUUID().toString(), tripId, 1, List.of(), PersonalizationScope.TRIP, List.of(), now);

		TripConstraint allergy = new TripConstraint(
				UUID.randomUUID().toString(), tripId, "ALLERGY", "PEANUT",
				TripConstraint.Severity.HARD, "EXCLUDES", null, null,
				TripConstraint.EvidenceStatus.NEEDS_REVIEW, TripConstraint.AnswerStatus.SELECTED,
				PersonalizationScope.TRIP, null);

		tripRepository.save(trip, List.of(allergy), owner, snapshot);

		TripConstraint found = tripRepository.findConstraints(tripId).get(0);
		assertThat(found.type()).isEqualTo("ALLERGY");
		assertThat(found.constraintKey()).isEqualTo("PEANUT");
		assertThat(found.severity()).isEqualTo(TripConstraint.Severity.HARD);
	}

	@Test
	void sameIdempotencyKeyReturnsSameTrip() {
		Instant now = Instant.now().truncatedTo(ChronoUnit.MICROS);
		String tripId = UUID.randomUUID().toString();
		Trip trip = newTrip(tripId, now);
		TripMember owner = TripMember.owner(UUID.randomUUID().toString(), tripId, userId, now);
		PreferenceSnapshot snapshot = new PreferenceSnapshot(
				UUID.randomUUID().toString(), tripId, 1, List.of(), PersonalizationScope.TRIP, List.of(), now);

		var first = tripRepository.saveWithIdempotency(userId, "key_1", "fp_1", trip, List.of(), owner, snapshot);
		assertThat(first.created()).isTrue();

		String retryTripId = UUID.randomUUID().toString();
		Trip retryTrip = newTrip(retryTripId, now);
		TripMember retryOwner = TripMember.owner(UUID.randomUUID().toString(), retryTripId, userId, now);
		PreferenceSnapshot retrySnapshot = new PreferenceSnapshot(
				UUID.randomUUID().toString(), retryTripId, 1, List.of(), PersonalizationScope.TRIP, List.of(), now);

		var second = tripRepository.saveWithIdempotency(
				userId, "key_1", "fp_1", retryTrip, List.of(), retryOwner, retrySnapshot);

		assertThat(second.created()).isFalse();
		assertThat(second.trip().tripId()).isEqualTo(tripId);
		assertThat(tripRepository.findById(retryTripId)).isEmpty();
	}

	@Test
	void sameKeyDifferentFingerprintIsRejected() {
		Instant now = Instant.now().truncatedTo(ChronoUnit.MICROS);
		String tripId = UUID.randomUUID().toString();
		Trip trip = newTrip(tripId, now);
		TripMember owner = TripMember.owner(UUID.randomUUID().toString(), tripId, userId, now);
		PreferenceSnapshot snapshot = new PreferenceSnapshot(
				UUID.randomUUID().toString(), tripId, 1, List.of(), PersonalizationScope.TRIP, List.of(), now);

		tripRepository.saveWithIdempotency(userId, "key_2", "fp_a", trip, List.of(), owner, snapshot);

		String otherTripId = UUID.randomUUID().toString();
		Trip otherTrip = newTrip(otherTripId, now);
		TripMember otherOwner = TripMember.owner(UUID.randomUUID().toString(), otherTripId, userId, now);
		PreferenceSnapshot otherSnapshot = new PreferenceSnapshot(
				UUID.randomUUID().toString(), otherTripId, 1, List.of(), PersonalizationScope.TRIP, List.of(), now);

		assertThrows(TripRepository.IdempotencyKeyConflictException.class,
				() -> tripRepository.saveWithIdempotency(
						userId, "key_2", "fp_b", otherTrip, List.of(), otherOwner, otherSnapshot));
	}

	/**
	 * 같은 키의 요청 8개가 <b>동시에</b> 들어와도 실제 PostgreSQL 에서 여행은 하나만 생긴다.
	 *
	 * <p>{@code TripCreationTest} 의 동시성 테스트는 메모리 저장소라 {@code ON CONFLICT} 를 거치지
	 * 않는다. 여기서는 8개 트랜잭션이 같은 (user_id, idempotency_key) 를 동시에 INSERT 하고,
	 * 진 쪽이 예외 없이 이긴 쪽의 여행을 돌려받는지까지 본다.
	 */
	@Test
	void concurrentSameKeyCreatesOneTripOnPostgres() throws Exception {
		assertThat(dataSource.unwrap(HikariDataSource.class).getMaximumPoolSize())
				.as("8개 요청이 연결을 기다리지 않고 동시에 DB 에 들어가야 한다")
				.isGreaterThanOrEqualTo(CONCURRENT_REQUESTS);

		Instant now = Instant.now().truncatedTo(ChronoUnit.MICROS);
		CountDownLatch startTogether = new CountDownLatch(1);
		List<String> candidateTripIds = new ArrayList<>();
		List<Future<TripRepository.SaveOutcome>> futures = new ArrayList<>();
		List<TripRepository.SaveOutcome> outcomes = new ArrayList<>();

		ExecutorService pool = Executors.newFixedThreadPool(CONCURRENT_REQUESTS);
		try {
			for (int i = 0; i < CONCURRENT_REQUESTS; i++) {
				String tripId = UUID.randomUUID().toString();
				candidateTripIds.add(tripId);
				Trip trip = newTrip(tripId, now);
				TripMember owner = TripMember.owner(UUID.randomUUID().toString(), tripId, userId, now);
				PreferenceSnapshot snapshot = new PreferenceSnapshot(
						UUID.randomUUID().toString(), tripId, 1, List.of(), PersonalizationScope.TRIP, List.of(), now);
				futures.add(pool.submit(() -> {
					startTogether.await();
					return tripRepository.saveWithIdempotency(
							userId, "key_concurrent", "fp_same", trip, List.of(), owner, snapshot);
				}));
			}
			startTogether.countDown();
			for (Future<TripRepository.SaveOutcome> future : futures) {
				outcomes.add(future.get(30, TimeUnit.SECONDS)); // 진 쪽이 예외를 던지면 여기서 실패한다
			}
		} finally {
			pool.shutdownNow();
		}

		List<TripRepository.SaveOutcome> created = outcomes.stream().filter(TripRepository.SaveOutcome::created).toList();
		assertThat(created).as("새로 만든 요청은 정확히 하나").hasSize(1);
		String winnerTripId = created.get(0).trip().tripId();
		assertThat(outcomes).as("나머지 7개도 같은 여행을 돌려받는다")
				.allSatisfy(outcome -> assertThat(outcome.trip().tripId()).isEqualTo(winnerTripId));

		Integer keyRows = jdbcTemplate.queryForObject(
				"SELECT count(*) FROM trip_idempotency WHERE user_id = ? AND idempotency_key = 'key_concurrent'",
				Integer.class, UUID.fromString(userId));
		assertThat(keyRows).isEqualTo(1);
		assertThat(candidateTripIds.stream().filter(id -> tripRepository.findById(id).isPresent()))
				.as("8개 후보 중 DB 에 남은 여행은 이긴 쪽 하나")
				.containsExactly(winnerTripId);
	}
}
