package com.ryuqq.otatoy.e2e;

import com.ryuqq.otatoy.bootstrap.extranet.ExtranetApplication;
import com.ryuqq.otatoy.persistence.inventory.entity.InventoryJpaEntity;
import com.ryuqq.otatoy.persistence.inventory.repository.InventoryJpaRepository;
import com.ryuqq.otatoy.persistence.partner.entity.PartnerJpaEntity;
import com.ryuqq.otatoy.persistence.pricing.entity.RateJpaEntity;
import com.ryuqq.otatoy.persistence.pricing.entity.RatePlanJpaEntity;
import com.ryuqq.otatoy.persistence.pricing.entity.RateRuleJpaEntity;
import com.ryuqq.otatoy.persistence.pricing.repository.RateJpaRepository;
import com.ryuqq.otatoy.persistence.pricing.repository.RatePlanJpaRepository;
import com.ryuqq.otatoy.persistence.pricing.repository.RateRuleJpaRepository;
import com.ryuqq.otatoy.persistence.property.entity.PropertyJpaEntity;
import com.ryuqq.otatoy.persistence.property.repository.PropertyAmenityJpaRepository;
import com.ryuqq.otatoy.persistence.property.repository.PropertyAttributeValueJpaRepository;
import com.ryuqq.otatoy.persistence.property.repository.PropertyJpaRepository;
import com.ryuqq.otatoy.persistence.property.repository.PropertyPhotoJpaRepository;
import com.ryuqq.otatoy.persistence.propertytype.entity.PropertyTypeAttributeJpaEntity;
import com.ryuqq.otatoy.persistence.propertytype.entity.PropertyTypeJpaEntity;
import com.ryuqq.otatoy.persistence.roomtype.entity.RoomTypeJpaEntity;
import com.ryuqq.otatoy.persistence.roomtype.repository.RoomTypeBedJpaRepository;
import com.ryuqq.otatoy.persistence.roomtype.repository.RoomTypeJpaRepository;
import com.ryuqq.otatoy.persistence.roomtype.repository.RoomTypeViewJpaRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
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

/**
 * Extranet E2E 테스트 공통 베이스 클래스.
 * Testcontainers(MySQL + Redis)를 사용하여 실제 서버를 기동하고 TestRestTemplate으로 HTTP 호출을 수행한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
@SpringBootTest(
    classes = ExtranetApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@Tag("e2e")
@ActiveProfiles("test")
public abstract class ExtranetE2ETestBase {

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
    static final GenericContainer<?> redis = new GenericContainer<>(
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
        registry.add("spring.datasource.hikari.maximum-pool-size", () -> "5");
        registry.add("spring.datasource.hikari.minimum-idle", () -> "1");

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
    protected PropertyPhotoJpaRepository propertyPhotoJpaRepository;
    @Autowired
    protected PropertyAmenityJpaRepository propertyAmenityJpaRepository;
    @Autowired
    protected PropertyAttributeValueJpaRepository propertyAttributeValueJpaRepository;
    @Autowired
    protected RoomTypeJpaRepository roomTypeJpaRepository;
    @Autowired
    protected RoomTypeBedJpaRepository roomTypeBedJpaRepository;
    @Autowired
    protected RoomTypeViewJpaRepository roomTypeViewJpaRepository;
    @Autowired
    protected RatePlanJpaRepository ratePlanJpaRepository;
    @Autowired
    protected RateRuleJpaRepository rateRuleJpaRepository;
    @Autowired
    protected RateJpaRepository rateJpaRepository;
    @Autowired
    protected InventoryJpaRepository inventoryJpaRepository;

    @BeforeEach
    void cleanUp() {
        transactionTemplate.executeWithoutResult(status -> {
            // FK 역순 삭제
            rateJpaRepository.deleteAll();
            rateRuleJpaRepository.deleteAll();
            inventoryJpaRepository.deleteAll();
            ratePlanJpaRepository.deleteAll();
            roomTypeBedJpaRepository.deleteAll();
            roomTypeViewJpaRepository.deleteAll();
            roomTypeJpaRepository.deleteAll();
            propertyAttributeValueJpaRepository.deleteAll();
            propertyAmenityJpaRepository.deleteAll();
            propertyPhotoJpaRepository.deleteAll();
            propertyJpaRepository.deleteAll();

            // partner, property_type은 마스터 데이터이므로 JPQL로 삭제
            entityManager.createQuery("DELETE FROM PropertyTypeAttributeJpaEntity").executeUpdate();
            entityManager.createQuery("DELETE FROM PropertyTypeJpaEntity").executeUpdate();
            entityManager.createQuery("DELETE FROM PartnerJpaEntity").executeUpdate();
            entityManager.flush();
        });
    }

    // --- 공통 헬퍼: 사전 데이터 삽입 ---

    /**
     * 파트너를 생성하고 ID를 반환한다.
     */
    protected Long insertPartner(String name) {
        return transactionTemplate.execute(status -> {
            PartnerJpaEntity partner = PartnerJpaEntity.create(
                null, name, "ACTIVE", Instant.now(), Instant.now(), null);
            entityManager.persist(partner);
            entityManager.flush();
            return partner.getId();
        });
    }

    /**
     * 숙소 유형을 생성하고 ID를 반환한다.
     */
    protected Long insertPropertyType(String code, String name) {
        return transactionTemplate.execute(status -> {
            PropertyTypeJpaEntity pt = PropertyTypeJpaEntity.create(
                null, code, name, name + " 설명", Instant.now(), Instant.now(), null);
            entityManager.persist(pt);
            entityManager.flush();
            return pt.getId();
        });
    }

    /**
     * 숙소 유형 속성을 생성하고 ID를 반환한다.
     */
    protected Long insertPropertyTypeAttribute(Long propertyTypeId, String key, String name) {
        return transactionTemplate.execute(status -> {
            PropertyTypeAttributeJpaEntity attr = PropertyTypeAttributeJpaEntity.create(
                null, propertyTypeId, key, name, "STRING", false, 0,
                Instant.now(), Instant.now(), null);
            entityManager.persist(attr);
            entityManager.flush();
            return attr.getId();
        });
    }

    /**
     * 숙소를 직접 DB에 삽입하고 ID를 반환한다.
     */
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

    /**
     * 객실 유형을 직접 DB에 삽입하고 ID를 반환한다.
     */
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

    /**
     * 요금 정책을 직접 DB에 삽입하고 ID를 반환한다.
     */
    protected Long insertRatePlan(Long roomTypeId, String name) {
        return transactionTemplate.execute(status -> {
            RatePlanJpaEntity ratePlan = RatePlanJpaEntity.create(
                null, roomTypeId, name,
                "DIRECT", null, true, false, 3,
                "3일 전 무료 취소", "PREPAID",
                Instant.now(), Instant.now(), null);
            entityManager.persist(ratePlan);
            entityManager.flush();
            return ratePlan.getId();
        });
    }

    /**
     * 요금 규칙을 직접 DB에 삽입하고 ID를 반환한다.
     */
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

    /**
     * 날짜별 확정 요금을 직접 DB에 삽입하고 ID를 반환한다.
     */
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

    /**
     * 재고를 직접 DB에 삽입하고 ID를 반환한다.
     */
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
     * 기본 URL을 구성한다.
     */
    protected String baseUrl() {
        return "http://localhost:" + port;
    }
}
