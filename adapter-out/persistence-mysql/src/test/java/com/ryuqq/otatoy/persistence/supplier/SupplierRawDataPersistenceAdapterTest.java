package com.ryuqq.otatoy.persistence.supplier;

import com.ryuqq.otatoy.domain.supplier.SupplierApiType;
import com.ryuqq.otatoy.domain.supplier.SupplierRawData;
import com.ryuqq.otatoy.domain.supplier.SupplierRawDataId;
import com.ryuqq.otatoy.domain.supplier.SupplierRawDataStatus;
import com.ryuqq.otatoy.domain.supplier.SupplierId;
import com.ryuqq.otatoy.domain.supplier.SupplierTaskType;
import com.ryuqq.otatoy.persistence.MySqlTestContainerConfig;
import com.ryuqq.otatoy.persistence.config.JpaConfig;
import com.ryuqq.otatoy.persistence.config.QueryDslConfig;
import com.ryuqq.otatoy.persistence.supplier.adapter.SupplierRawDataCommandAdapter;
import com.ryuqq.otatoy.persistence.supplier.adapter.SupplierRawDataQueryAdapter;
import com.ryuqq.otatoy.persistence.supplier.entity.SupplierJpaEntity;
import com.ryuqq.otatoy.persistence.supplier.mapper.SupplierRawDataEntityMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SupplierRawData Persistence Adapter 통합 테스트.
 * Testcontainers MySQL + Flyway 기반으로 RawData 저장/조회 및 상태 기반 필터를 검증한다.
 *
 * @author persistence-mysql-test-designer
 * @since 2026-04-06
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
        JpaConfig.class,
        QueryDslConfig.class,
        SupplierRawDataCommandAdapter.class,
        SupplierRawDataQueryAdapter.class,
        SupplierRawDataEntityMapper.class
})
class SupplierRawDataPersistenceAdapterTest extends MySqlTestContainerConfig {

    @Autowired
    private SupplierRawDataCommandAdapter commandAdapter;

    @Autowired
    private SupplierRawDataQueryAdapter queryAdapter;

    @Autowired
    private EntityManager entityManager;

    private Long supplierId;

    @BeforeEach
    void setUp() {
        // FK 선행 데이터: Supplier
        Instant now = Instant.now();
        SupplierJpaEntity supplier = SupplierJpaEntity.create(
                null, "TestSupplier", "테스트공급자", "테스트컴퍼니", "홍길동", "123-45-67890",
                "서울시 강남구", "02-1234-5678", "test@test.com", "https://test.com/terms",
                "ACTIVE", now, now, null
        );
        entityManager.persist(supplier);
        entityManager.flush();
        supplierId = supplier.getId();
    }

    @Nested
    @DisplayName("PT-1: Domain <-> Entity 매핑 정합성")
    class MappingIntegrity {

        @Test
        @DisplayName("SupplierRawData 저장 후 조회 시 모든 필드가 원본과 동일하다")
        void shouldMapAllFieldsCorrectly() {
            // given
            Instant now = Instant.now();
            String payload = "{\"properties\":[{\"name\":\"Hotel A\"}]}";
            SupplierRawData original = SupplierRawData.forNew(
                    SupplierId.of(supplierId), SupplierTaskType.PROPERTY_CONTENT, SupplierApiType.MOCK, payload, now
            );

            // when
            Long savedId = commandAdapter.persist(original);
            List<SupplierRawData> found = queryAdapter.findBySupplierIdAndStatus(
                    SupplierId.of(supplierId), SupplierRawDataStatus.FETCHED
            );

            // then
            assertThat(found).isNotEmpty();
            SupplierRawData result = found.stream()
                    .filter(rd -> rd.rawPayload().equals(payload))
                    .findFirst()
                    .orElseThrow();

            assertThat(result.id().value()).isEqualTo(savedId);
            assertThat(result.supplierId().value()).isEqualTo(supplierId);
            assertThat(result.rawPayload()).isEqualTo(payload);
            assertThat(result.status()).isEqualTo(SupplierRawDataStatus.FETCHED);
            assertThat(result.fetchedAt()).isNotNull();
            assertThat(result.processedAt()).isNull();
        }
    }

    @Nested
    @DisplayName("PT-2: CRUD 동작 검증")
    class CrudOperations {

        @Test
        @DisplayName("persist 후 findBySupplierIdAndStatus로 FETCHED 상태 데이터를 조회할 수 있다")
        void shouldPersistAndFindByStatus() {
            // given
            Instant now = Instant.now();
            SupplierRawData rawData = SupplierRawData.forNew(
                    SupplierId.of(supplierId), SupplierTaskType.PROPERTY_CONTENT, SupplierApiType.MOCK, "{\"data\":\"test\"}", now
            );

            // when
            Long savedId = commandAdapter.persist(rawData);
            List<SupplierRawData> fetchedList = queryAdapter.findBySupplierIdAndStatus(
                    SupplierId.of(supplierId), SupplierRawDataStatus.FETCHED
            );

            // then
            assertThat(savedId).isNotNull();
            assertThat(fetchedList).anyMatch(rd -> rd.id().value().equals(savedId));
        }

        @Test
        @DisplayName("SYNCED 상태로 저장한 데이터는 FETCHED 조회에서 나오지 않는다")
        void shouldNotReturnDifferentStatusData() {
            // given
            Instant now = Instant.now();
            SupplierRawData syncedData = SupplierRawData.reconstitute(
                    SupplierRawDataId.forNew(), SupplierId.of(supplierId),
                    SupplierTaskType.PROPERTY_CONTENT, SupplierApiType.MOCK, "{\"synced\":true}",
                    SupplierRawDataStatus.SYNCED, now, now, now, now
            );
            commandAdapter.persist(syncedData);

            // when
            List<SupplierRawData> fetchedList = queryAdapter.findBySupplierIdAndStatus(
                    SupplierId.of(supplierId), SupplierRawDataStatus.FETCHED
            );

            // then
            assertThat(fetchedList).noneMatch(rd -> rd.rawPayload().equals("{\"synced\":true}"));
        }

        @Test
        @DisplayName("해당 Supplier의 해당 상태 데이터가 없으면 빈 리스트를 반환한다")
        void shouldReturnEmptyWhenNoMatchingData() {
            // when
            List<SupplierRawData> result = queryAdapter.findBySupplierIdAndStatus(
                    SupplierId.of(99999L), SupplierRawDataStatus.FETCHED
            );

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("PT-3: 상태별 필터 검증")
    class StatusFilterTest {

        @Test
        @DisplayName("동일 Supplier에 FETCHED, PROCESSING, SYNCED 데이터가 있을 때 상태별로 정확히 필터된다")
        void shouldFilterByStatusCorrectly() {
            // given
            Instant now = Instant.now();

            SupplierRawData fetched = SupplierRawData.forNew(
                    SupplierId.of(supplierId), SupplierTaskType.PROPERTY_CONTENT, SupplierApiType.MOCK, "{\"status\":\"fetched\"}", now
            );
            SupplierRawData processing = SupplierRawData.reconstitute(
                    SupplierRawDataId.forNew(), SupplierId.of(supplierId),
                    SupplierTaskType.PROPERTY_CONTENT, SupplierApiType.MOCK, "{\"status\":\"processing\"}",
                    SupplierRawDataStatus.PROCESSING, now, null, now, now
            );
            SupplierRawData synced = SupplierRawData.reconstitute(
                    SupplierRawDataId.forNew(), SupplierId.of(supplierId),
                    SupplierTaskType.PROPERTY_CONTENT, SupplierApiType.MOCK, "{\"status\":\"synced\"}",
                    SupplierRawDataStatus.SYNCED, now, now, now, now
            );

            commandAdapter.persist(fetched);
            commandAdapter.persist(processing);
            commandAdapter.persist(synced);

            // when
            List<SupplierRawData> fetchedList = queryAdapter.findBySupplierIdAndStatus(
                    SupplierId.of(supplierId), SupplierRawDataStatus.FETCHED
            );
            List<SupplierRawData> processingList = queryAdapter.findBySupplierIdAndStatus(
                    SupplierId.of(supplierId), SupplierRawDataStatus.PROCESSING
            );
            List<SupplierRawData> syncedList = queryAdapter.findBySupplierIdAndStatus(
                    SupplierId.of(supplierId), SupplierRawDataStatus.SYNCED
            );

            // then
            assertThat(fetchedList).anyMatch(rd -> rd.rawPayload().contains("fetched"));
            assertThat(fetchedList).noneMatch(rd -> rd.rawPayload().contains("processing"));

            assertThat(processingList).anyMatch(rd -> rd.rawPayload().contains("processing"));
            assertThat(processingList).noneMatch(rd -> rd.rawPayload().contains("fetched"));

            assertThat(syncedList).anyMatch(rd -> rd.rawPayload().contains("synced"));
            assertThat(syncedList).noneMatch(rd -> rd.rawPayload().contains("fetched"));
        }
    }

    @Nested
    @DisplayName("PT-4: processedAt 필드 처리")
    class ProcessedAtTest {

        @Test
        @DisplayName("processedAt이 null인 FETCHED 데이터가 정상 저장/조회된다")
        void shouldHandleNullProcessedAt() {
            // given
            Instant now = Instant.now();
            SupplierRawData rawData = SupplierRawData.forNew(
                    SupplierId.of(supplierId), SupplierTaskType.PROPERTY_CONTENT, SupplierApiType.MOCK, "{\"test\":true}", now
            );

            // when
            commandAdapter.persist(rawData);
            List<SupplierRawData> found = queryAdapter.findBySupplierIdAndStatus(
                    SupplierId.of(supplierId), SupplierRawDataStatus.FETCHED
            );

            // then
            SupplierRawData result = found.stream()
                    .filter(rd -> rd.rawPayload().equals("{\"test\":true}"))
                    .findFirst()
                    .orElseThrow();
            assertThat(result.processedAt()).isNull();
        }

        @Test
        @DisplayName("processedAt이 설정된 SYNCED 데이터가 정상 저장/조회된다")
        void shouldHandleNonNullProcessedAt() {
            // given
            Instant now = Instant.now();
            Instant processedAt = now.minusSeconds(60);
            SupplierRawData rawData = SupplierRawData.reconstitute(
                    SupplierRawDataId.forNew(), SupplierId.of(supplierId),
                    SupplierTaskType.PROPERTY_CONTENT, SupplierApiType.MOCK, "{\"processed\":true}",
                    SupplierRawDataStatus.SYNCED, now, processedAt, now, now
            );

            // when
            commandAdapter.persist(rawData);
            List<SupplierRawData> found = queryAdapter.findBySupplierIdAndStatus(
                    SupplierId.of(supplierId), SupplierRawDataStatus.SYNCED
            );

            // then
            SupplierRawData result = found.stream()
                    .filter(rd -> rd.rawPayload().equals("{\"processed\":true}"))
                    .findFirst()
                    .orElseThrow();
            assertThat(result.processedAt()).isNotNull();
        }
    }
}
