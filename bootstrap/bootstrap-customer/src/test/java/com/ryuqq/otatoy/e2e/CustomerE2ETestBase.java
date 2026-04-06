package com.ryuqq.otatoy.e2e;

import com.ryuqq.otatoy.bootstrap.customer.CustomerApplication;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeId;
import com.ryuqq.otatoy.persistence.inventory.entity.InventoryJpaEntity;
import com.ryuqq.otatoy.persistence.inventory.repository.InventoryJpaRepository;
import com.ryuqq.otatoy.persistence.redis.adapter.InventoryRedisAdapter;
import com.ryuqq.otatoy.persistence.partner.entity.PartnerJpaEntity;
import com.ryuqq.otatoy.persistence.pricing.entity.RateJpaEntity;
import com.ryuqq.otatoy.persistence.pricing.entity.RatePlanJpaEntity;
import com.ryuqq.otatoy.persistence.pricing.entity.RateRuleJpaEntity;
import com.ryuqq.otatoy.persistence.pricing.repository.RateJpaRepository;
import com.ryuqq.otatoy.persistence.pricing.repository.RatePlanJpaRepository;
import com.ryuqq.otatoy.persistence.pricing.repository.RateRuleJpaRepository;
import com.ryuqq.otatoy.persistence.property.entity.PropertyJpaEntity;
import com.ryuqq.otatoy.persistence.property.repository.PropertyJpaRepository;
import com.ryuqq.otatoy.persistence.propertytype.entity.PropertyTypeJpaEntity;
import com.ryuqq.otatoy.persistence.reservation.repository.ReservationItemJpaRepository;
import com.ryuqq.otatoy.persistence.reservation.repository.ReservationJpaRepository;
import com.ryuqq.otatoy.persistence.reservation.repository.ReservationLineJpaRepository;
import com.ryuqq.otatoy.persistence.reservation.repository.ReservationSessionJpaRepository;
import com.ryuqq.otatoy.persistence.roomtype.entity.RoomTypeJpaEntity;
import com.ryuqq.otatoy.persistence.roomtype.repository.RoomTypeJpaRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;

/**
 * Customer E2E 테스트 공통 베이스 클래스.
 * Testcontainers(MySQL + Redis)를 사용하여 실제 서버를 기동하고 TestRestTemplate으로 HTTP 호출을 수행한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
@SpringBootTest(
    classes = CustomerApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@Tag("e2e")
@ActiveProfiles("test")
public abstract class CustomerE2ETestBase {

    // --- Testcontainers (Singleton 패턴) ---

    @SuppressWarnings("resource")
    static final MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
        .withDatabaseName("ota_test")
        .withUsername("root")
        .withPassword("root")
        .withCommand(
            "--character-set-server=utf8mb4",
            "--collation-server=utf8mb4_unicode_ci",
            "--lower-case-table-names=1",
            "--max-connections=300"
        )
        .withReuse(true);

    @SuppressWarnings("resource")
    protected static final GenericContainer<?> redis = new GenericContainer<>(
            DockerImageName.parse("redis:7.2-alpine"))
        .withExposedPorts(6379)
        .withReuse(true);

    static {
        mysql.start();
        redis.start();
    }

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        // MySQL
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
        registry.add("spring.datasource.hikari.maximum-pool-size", () -> "10");
        registry.add("spring.datasource.hikari.minimum-idle", () -> "2");

        // Flyway
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.flyway.locations", () -> "classpath:db/migration");

        // JPA
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");
        registry.add("spring.jpa.properties.hibernate.dialect", () -> "org.hibernate.dialect.MySQLDialect");

        // Redis (Redisson)
        String redisAddress = "redis://" + redis.getHost() + ":" + redis.getMappedPort(6379);
        registry.add("spring.redis.redisson.config",
            () -> "singleServerConfig:\n  address: \"" + redisAddress + "\"");
    }

    // --- 인프라 빈 주입 ---

    @LocalServerPort
    protected int port;

    @Autowired
    protected TestRestTemplate restTemplate;

    @Autowired
    protected EntityManager entityManager;

    @Autowired
    protected TransactionTemplate transactionTemplate;

    // --- JPA Repositories ---
    @Autowired
    protected PropertyJpaRepository propertyJpaRepository;
    @Autowired
    protected RoomTypeJpaRepository roomTypeJpaRepository;
    @Autowired
    protected RatePlanJpaRepository ratePlanJpaRepository;
    @Autowired
    protected RateRuleJpaRepository rateRuleJpaRepository;
    @Autowired
    protected RateJpaRepository rateJpaRepository;
    @Autowired
    protected InventoryJpaRepository inventoryJpaRepository;
    @Autowired
    protected ReservationSessionJpaRepository reservationSessionJpaRepository;
    @Autowired
    protected ReservationJpaRepository reservationJpaRepository;
    @Autowired
    protected ReservationLineJpaRepository reservationLineJpaRepository;
    @Autowired
    protected ReservationItemJpaRepository reservationItemJpaRepository;
    @Autowired
    protected InventoryRedisAdapter inventoryRedisAdapter;

    @BeforeEach
    void cleanUp() {
        transactionTemplate.executeWithoutResult(status -> {
            // FK 역순 삭제
            reservationItemJpaRepository.deleteAll();
            reservationLineJpaRepository.deleteAll();
            reservationJpaRepository.deleteAll();
            reservationSessionJpaRepository.deleteAll();
            rateJpaRepository.deleteAll();
            rateRuleJpaRepository.deleteAll();
            inventoryJpaRepository.deleteAll();
            ratePlanJpaRepository.deleteAll();
            roomTypeJpaRepository.deleteAll();
            propertyJpaRepository.deleteAll();

            entityManager.createQuery("DELETE FROM PropertyTypeJpaEntity").executeUpdate();
            entityManager.createQuery("DELETE FROM PartnerJpaEntity").executeUpdate();
            entityManager.flush();
        });
    }

    // --- 공통 헬퍼: 사전 데이터 삽입 ---

    protected Long insertPartner(String name) {
        return transactionTemplate.execute(status -> {
            PartnerJpaEntity partner = PartnerJpaEntity.create(
                null, name, "ACTIVE", Instant.now(), Instant.now(), null);
            entityManager.persist(partner);
            entityManager.flush();
            return partner.getId();
        });
    }

    protected Long insertPropertyType(String code, String name) {
        return transactionTemplate.execute(status -> {
            PropertyTypeJpaEntity pt = PropertyTypeJpaEntity.create(
                null, code, name, name + " 설명", Instant.now(), Instant.now(), null);
            entityManager.persist(pt);
            entityManager.flush();
            return pt.getId();
        });
    }

    protected Long insertProperty(Long partnerId, Long propertyTypeId, String name) {
        return transactionTemplate.execute(status -> {
            PropertyJpaEntity property = PropertyJpaEntity.create(
                null, partnerId, null, propertyTypeId,
                name, "설명", "서울시 강남구", 37.5, 127.0,
                "강남", "서울", "ACTIVE", null,
                Instant.now(), Instant.now(), null);
            entityManager.persist(property);
            entityManager.flush();
            return property.getId();
        });
    }

    protected Long insertRoomType(Long propertyId, String name) {
        return transactionTemplate.execute(status -> {
            RoomTypeJpaEntity roomType = RoomTypeJpaEntity.create(
                null, propertyId, name, "설명",
                new BigDecimal("33.00"), "10평", 2, 4, 5,
                "15:00", "11:00", "ACTIVE",
                Instant.now(), Instant.now(), null);
            entityManager.persist(roomType);
            entityManager.flush();
            return roomType.getId();
        });
    }

    protected Long insertRatePlan(Long roomTypeId, String name) {
        return transactionTemplate.execute(status -> {
            RatePlanJpaEntity ratePlan = RatePlanJpaEntity.create(
                null, roomTypeId, name,
                "DIRECT", null, true, false, 3,
                "3일 전 무료 취소", "PREPAY",
                Instant.now(), Instant.now(), null);
            entityManager.persist(ratePlan);
            entityManager.flush();
            return ratePlan.getId();
        });
    }

    protected Long insertRateRule(Long ratePlanId, LocalDate start, LocalDate end, BigDecimal basePrice) {
        return transactionTemplate.execute(status -> {
            RateRuleJpaEntity rateRule = RateRuleJpaEntity.create(
                null, ratePlanId, start, end,
                basePrice, null, null, null, null,
                Instant.now(), Instant.now(), null);
            entityManager.persist(rateRule);
            entityManager.flush();
            return rateRule.getId();
        });
    }

    protected Long insertRate(Long ratePlanId, LocalDate rateDate, BigDecimal price) {
        return transactionTemplate.execute(status -> {
            RateJpaEntity rate = RateJpaEntity.create(
                null, ratePlanId, rateDate, price,
                Instant.now(), Instant.now(), null);
            entityManager.persist(rate);
            entityManager.flush();
            return rate.getId();
        });
    }

    protected Long insertInventory(Long roomTypeId, LocalDate date, int total, int available) {
        return transactionTemplate.execute(status -> {
            InventoryJpaEntity inv = InventoryJpaEntity.create(
                null, roomTypeId, date, total, available, false, 0,
                Instant.now(), Instant.now(), null);
            entityManager.persist(inv);
            entityManager.flush();
            return inv.getId();
        });
    }

    /**
     * 예약 테스트에 필요한 전체 사전 데이터를 한 번에 삽입한다.
     * @return 삽입된 ID들을 담은 레코드
     */
    protected ReservationTestData setupReservationData(int inventoryCount) {
        Long partnerId = insertPartner("예약 테스트 파트너");
        Long propertyTypeId = insertPropertyType("HOTEL", "호텔");
        Long propertyId = insertProperty(partnerId, propertyTypeId, "예약 테스트 호텔");
        Long roomTypeId = insertRoomType(propertyId, "스탠다드");
        Long ratePlanId = insertRatePlan(roomTypeId, "기본 요금제");

        LocalDate checkIn = LocalDate.now().plusDays(30);
        LocalDate checkOut = checkIn.plusDays(1);
        BigDecimal price = new BigDecimal("100000.00");

        insertRateRule(ratePlanId, checkIn, checkOut, price);
        Long rateId = insertRate(ratePlanId, checkIn, price);
        Long inventoryId = insertInventory(roomTypeId, checkIn, inventoryCount, inventoryCount);

        // Redis 재고 초기화 — 예약 시 Redis Lua 스크립트가 먼저 차감하므로 필수
        inventoryRedisAdapter.initializeStock(
            RoomTypeId.of(roomTypeId),
            Map.of(checkIn, inventoryCount)
        );

        return new ReservationTestData(
            partnerId, propertyId, roomTypeId, ratePlanId,
            rateId, inventoryId, checkIn, checkOut, price);
    }

    protected record ReservationTestData(
        Long partnerId, Long propertyId, Long roomTypeId, Long ratePlanId,
        Long rateId, Long inventoryId, LocalDate checkIn, LocalDate checkOut, BigDecimal price
    ) {}

    protected String baseUrl() {
        return "http://localhost:" + port;
    }

    // --- 공통 HTTP 헬퍼 ---

    protected ResponseEntity<Map<String, Object>> postJson(String path, Object body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return restTemplate.exchange(
            baseUrl() + path,
            HttpMethod.POST,
            new HttpEntity<>(body, headers),
            new ParameterizedTypeReference<>() {});
    }

    protected ResponseEntity<Map<String, Object>> postJsonWithHeaders(String path, Object body, HttpHeaders extraHeaders) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.addAll(extraHeaders);
        return restTemplate.exchange(
            baseUrl() + path,
            HttpMethod.POST,
            new HttpEntity<>(body, headers),
            new ParameterizedTypeReference<>() {});
    }

    protected ResponseEntity<Map<String, Object>> patchJson(String path, Object body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return restTemplate.exchange(
            baseUrl() + path,
            HttpMethod.PATCH,
            new HttpEntity<>(body, headers),
            new ParameterizedTypeReference<>() {});
    }
}
