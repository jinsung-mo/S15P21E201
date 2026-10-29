package com.gabolle.backend.itinerary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
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
import java.util.concurrent.atomic.AtomicInteger;

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

import com.gabolle.backend.itinerary.domain.ItineraryRepository;
import com.gabolle.backend.itinerary.domain.ItineraryVersion;
import com.gabolle.backend.itinerary.domain.StaleItineraryVersionException;
import com.gabolle.backend.recommendation.support.PostgresAvailableCondition;
import com.gabolle.backend.recommendation.support.TestDatabase;
import com.gabolle.testslice.ItinerarySliceApplication;

/**
 * {@code itineraries}·{@code itinerary_versions} 가 실제 PostgreSQL 위에서 도는지 —
 * S15P21E201-313.
 *
 * <p>🔴 <b>이 테스트가 증명하는 핵심</b>은 {@code UNIQUE (itinerary_id, version)} 위반이
 * 진짜 PostgreSQL 에서 {@link StaleItineraryVersionException}(409)으로 바뀌는가다.
 * H2 로는 이 계약을 진짜로 검증할 수 없다.
 */
@SpringBootTest(classes = ItinerarySliceApplication.class, properties = {
		"spring.profiles.active=db",
		"spring.jpa.hibernate.ddl-auto=none",
		"spring.flyway.enabled=true"
})
@ExtendWith(PostgresAvailableCondition.class)
class ItineraryPersistenceIntegrationTest {

	/** 동시 수정 테스트에서 8개 편집이 모두 DB 연결을 잡고 동시에 들어가게 한다. */
	private static final int CONCURRENT_EDITS = 8;

	@DynamicPropertySource
	static void datasourceProperties(DynamicPropertyRegistry registry) {
		TestDatabase.registerDatasource(registry);
		// TestDatabase 의 기본 상한(4)으로는 8개 중 4개가 연결을 기다리며 줄을 선다.
		// 이 클래스만 상한을 올려, 같은 판 번호를 노리는 편집이 실제로 겹치게 한다.
		registry.add("spring.datasource.hikari.maximum-pool-size", () -> String.valueOf(CONCURRENT_EDITS + 2));
	}

	@Autowired
	private ItineraryRepository itineraryRepository;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private DataSource dataSource;

	private String itineraryId;
	private String tripId;
	private String userId;

	@BeforeEach
	void seedItineraryAtVersion1() {
		userId = UUID.randomUUID().toString();
		tripId = UUID.randomUUID().toString();
		itineraryId = UUID.randomUUID().toString();
		OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

		jdbcTemplate.update(
				"INSERT INTO app_user (user_id, display_name, language, personalization_mode, status, created_at, updated_at) "
						+ "VALUES (?, 'test', 'ko', 'EXPLICIT_ONLY', 'ACTIVE', ?, ?)",
				UUID.fromString(userId), now, now);
		jdbcTemplate.update(
				"INSERT INTO trip (trip_id, owner_user_id, start_date, end_date, party_size, created_at, updated_at) "
						+ "VALUES (?, ?, '2026-09-06', '2026-09-08', 1, ?, ?)",
				UUID.fromString(tripId), UUID.fromString(userId), now, now);
		jdbcTemplate.update(
				"INSERT INTO itineraries (itinerary_id, trip_id, latest_version, created_at) VALUES (?, ?, 1, ?)",
				UUID.fromString(itineraryId), UUID.fromString(tripId), now);
	}

	private ItineraryVersion versionCandidate(int version, Integer baseVersion) {
		Instant now = Instant.now().truncatedTo(ChronoUnit.MICROS);
		return new ItineraryVersion(
				UUID.randomUUID().toString(), itineraryId, version, baseVersion,
				ItineraryVersion.Operation.REPLACE_ITEM, userId, "req_edit_" + UUID.randomUUID(),
				new ItineraryVersion.Versions(null, null, null, null, null), now);
	}

	@Test
	void appendMovesLatestVersionPointer() {
		itineraryRepository.appendVersion(versionCandidate(2, 1), List.of(), List.of(), List.of());

		var itinerary = itineraryRepository.findById(itineraryId).orElseThrow();
		assertThat(itinerary.latestVersion()).isEqualTo(2);

		var saved = itineraryRepository.findVersion(itineraryId, 2).orElseThrow();
		assertThat(saved.operation()).isEqualTo(ItineraryVersion.Operation.REPLACE_ITEM);
		assertThat(saved.createdBy()).isEqualTo(userId);
	}

	/**
	 * 🔴 S15P21E201-662 — 판 번호는 비어 있는데 <b>포인터</b>가 그 사이 움직인 경우.
	 *
	 * <p>다른 세션이 최신 포인터를 3으로 옮겨 놓은 상태에서 baseVersion 1 로 2번 판을
	 * 만들려 하면, {@code itinerary_versions} INSERT 는 통과한다(2번 판은 아직 없으니까).
	 * 막는 것은 {@code UPDATE ... WHERE latest_version = 1} 이 0행을 반영하는 것뿐이다.
	 * 그 조건을 빼면 이 테스트가 빨개진다 — 판 번호 UNIQUE 는 <b>번호</b>를 지키는 제약이지
	 * <b>포인터</b>를 지키는 제약이 아니다.
	 *
	 * <p>그리고 되돌려지는지도 함께 본다. 실패한 편집이 2번 판 행을 남기면 "판은 있는데
	 * 아무도 안 가리키는" 찌꺼기가 된다.
	 */
	@Test
	void pointerMovedElsewhereIsRejectedAndRollsBack() {
		jdbcTemplate.update("UPDATE itineraries SET latest_version = 3 WHERE itinerary_id = ?",
				UUID.fromString(itineraryId));

		assertThrows(StaleItineraryVersionException.class,
				() -> itineraryRepository.appendVersion(versionCandidate(2, 1), List.of(), List.of(), List.of()));

		Integer rows = jdbcTemplate.queryForObject(
				"SELECT count(*) FROM itinerary_versions WHERE itinerary_id = ? AND version = 2",
				Integer.class, UUID.fromString(itineraryId));
		assertThat(rows).isZero();
		assertThat(itineraryRepository.findById(itineraryId).orElseThrow().latestVersion()).isEqualTo(3);
	}

	@Test
	void duplicateVersionIsRejectedAsConflict() {
		itineraryRepository.appendVersion(versionCandidate(2, 1), List.of(), List.of(), List.of());

		assertThrows(StaleItineraryVersionException.class,
				() -> itineraryRepository.appendVersion(versionCandidate(2, 1), List.of(), List.of(), List.of()));

		// 🔴 진 쪽 시도로 포인터가 어긋나지 않는다 — 여전히 2다.
		assertThat(itineraryRepository.findById(itineraryId).orElseThrow().latestVersion()).isEqualTo(2);
	}

	/**
	 * 같은 1번 판을 바탕으로 한 편집 8개가 <b>동시에</b> 들어와도 실제 PostgreSQL 에서 하나만
	 * 성공하고 나머지는 {@link StaleItineraryVersionException}(409)으로 거절된다.
	 *
	 * <p>{@code ItineraryVersionConflictTest} 의 동시성 테스트는 메모리 저장소다. 여기서는 판 번호
	 * {@code ON CONFLICT} 와 포인터 조건부 UPDATE 가 동시에 겹칠 때 다른 예외(교착 등) 없이
	 * 409 로만 갈리는지, 판이 건너뛰어지지 않는지를 본다.
	 */
	@Test
	void concurrentEditsFromSameBaseOnlyOneWinsOnPostgres() throws Exception {
		assertThat(dataSource.unwrap(HikariDataSource.class).getMaximumPoolSize())
				.as("8개 편집이 연결을 기다리지 않고 동시에 DB 에 들어가야 한다")
				.isGreaterThanOrEqualTo(CONCURRENT_EDITS);

		CountDownLatch startTogether = new CountDownLatch(1);
		AtomicInteger succeeded = new AtomicInteger();
		AtomicInteger rejected = new AtomicInteger();
		List<Future<Void>> futures = new ArrayList<>();

		ExecutorService pool = Executors.newFixedThreadPool(CONCURRENT_EDITS);
		try {
			for (int i = 0; i < CONCURRENT_EDITS; i++) {
				ItineraryVersion candidate = versionCandidate(2, 1);
				futures.add(pool.submit(() -> {
					startTogether.await();
					try {
						itineraryRepository.appendVersion(candidate, List.of(), List.of(), List.of());
						succeeded.incrementAndGet();
					} catch (StaleItineraryVersionException expected) {
						rejected.incrementAndGet();
					}
					return null;
				}));
			}
			startTogether.countDown();
			for (Future<Void> future : futures) {
				future.get(30, TimeUnit.SECONDS); // 409 가 아닌 예외가 나면 여기서 실패한다
			}
		} finally {
			pool.shutdownNow();
		}

		assertThat(succeeded.get()).as("정확히 하나만 성공").isEqualTo(1);
		assertThat(rejected.get()).as("나머지는 전부 409").isEqualTo(CONCURRENT_EDITS - 1);
		assertThat(itineraryRepository.findById(itineraryId).orElseThrow().latestVersion()).isEqualTo(2);

		Integer version2Rows = jdbcTemplate.queryForObject(
				"SELECT count(*) FROM itinerary_versions WHERE itinerary_id = ? AND version = 2",
				Integer.class, UUID.fromString(itineraryId));
		Integer version3Rows = jdbcTemplate.queryForObject(
				"SELECT count(*) FROM itinerary_versions WHERE itinerary_id = ? AND version = 3",
				Integer.class, UUID.fromString(itineraryId));
		assertThat(version2Rows).isEqualTo(1);
		assertThat(version3Rows).as("판이 건너뛰어지지 않는다").isZero();
	}
}
