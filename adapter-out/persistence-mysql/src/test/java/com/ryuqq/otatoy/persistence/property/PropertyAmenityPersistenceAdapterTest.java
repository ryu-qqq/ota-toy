package com.ryuqq.otatoy.persistence.property;

import com.ryuqq.otatoy.domain.accommodation.AmenityName;
import com.ryuqq.otatoy.domain.accommodation.AmenityType;
import com.ryuqq.otatoy.domain.common.vo.Money;
import com.ryuqq.otatoy.domain.property.PropertyAmenities;
import com.ryuqq.otatoy.domain.property.PropertyAmenity;
import com.ryuqq.otatoy.domain.property.PropertyAmenityFixture;
import com.ryuqq.otatoy.domain.property.PropertyFixture;
import com.ryuqq.otatoy.domain.property.PropertyId;
import com.ryuqq.otatoy.persistence.MySqlTestContainerConfig;
import com.ryuqq.otatoy.persistence.config.JpaConfig;
import com.ryuqq.otatoy.persistence.config.QueryDslConfig;
import com.ryuqq.otatoy.persistence.property.adapter.PropertyAmenityCommandAdapter;
import com.ryuqq.otatoy.persistence.property.adapter.PropertyAmenityQueryAdapter;
import com.ryuqq.otatoy.persistence.property.adapter.PropertyCommandAdapter;
import com.ryuqq.otatoy.persistence.property.mapper.PropertyAmenityEntityMapper;
import com.ryuqq.otatoy.persistence.property.mapper.PropertyEntityMapper;
import com.ryuqq.otatoy.persistence.property.repository.PropertyAmenityQueryDslRepository;
import com.ryuqq.otatoy.persistence.property.repository.PropertyQueryDslRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * PropertyAmenity Persistence Adapter 통합 테스트.
 * Testcontainers MySQL + Flyway 기반으로 편의시설 CRUD 동작을 검증한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
        JpaConfig.class,
        QueryDslConfig.class,
        PropertyCommandAdapter.class,
        PropertyAmenityCommandAdapter.class,
        PropertyAmenityQueryAdapter.class,
        PropertyEntityMapper.class,
        PropertyAmenityEntityMapper.class,
        PropertyQueryDslRepository.class,
        PropertyAmenityQueryDslRepository.class
})
class PropertyAmenityPersistenceAdapterTest extends MySqlTestContainerConfig {

    @Autowired
    private PropertyCommandAdapter propertyCommandAdapter;

    @Autowired
    private PropertyAmenityCommandAdapter amenityCommandAdapter;

    @Autowired
    private PropertyAmenityQueryAdapter amenityQueryAdapter;

    private Long savedPropertyId;

    @BeforeEach
    void setUp() {
        // FK 선행 데이터: Property 먼저 생성
        savedPropertyId = propertyCommandAdapter.persist(PropertyFixture.aPropertyWithName("편의시설 테스트 호텔"));
    }

    // -- PT-1: Domain <-> Entity 매핑 정합성 --

    @Nested
    @DisplayName("PT-1: Domain <-> Entity 매핑 정합성")
    class DomainEntityMappingTest {

        @Test
        @DisplayName("PropertyAmenity 저장 후 조회 시 모든 필드가 원본과 동일하다")
        void shouldMapAllFieldsCorrectlyWhenSaveAndFind() {
            // given
            Instant now = Instant.now();
            PropertyAmenity amenity = PropertyAmenity.forNew(
                    PropertyId.of(savedPropertyId), AmenityType.POOL, AmenityName.of("수영장"),
                    Money.of(15000), 1, now
            );

            // when
            amenityCommandAdapter.persistAll(List.of(amenity));
            PropertyAmenities found = amenityQueryAdapter.findByPropertyId(PropertyId.of(savedPropertyId));

            // then
            assertThat(found.items()).hasSize(1);
            PropertyAmenity result = found.items().getFirst();
            assertThat(result.id().value()).isNotNull();
            assertThat(result.propertyId().value()).isEqualTo(savedPropertyId);
            assertThat(result.amenityType()).isEqualTo(AmenityType.POOL);
            assertThat(result.name().value()).isEqualTo("수영장");
            assertThat(result.additionalPrice().amount()).isEqualByComparingTo(BigDecimal.valueOf(15000));
            assertThat(result.sortOrder()).isEqualTo(1);
            assertThat(result.deletionStatus().deleted()).isFalse();
        }

        @Test
        @DisplayName("무료 편의시설(additionalPrice=0)도 정상적으로 저장/조회된다")
        void shouldHandleFreeAmenityCorrectly() {
            // given
            Instant now = Instant.now();
            PropertyAmenity amenity = PropertyAmenity.forNew(
                    PropertyId.of(savedPropertyId), AmenityType.WIFI, AmenityName.of("와이파이"),
                    Money.of(0), 1, now
            );

            // when
            amenityCommandAdapter.persistAll(List.of(amenity));
            PropertyAmenities found = amenityQueryAdapter.findByPropertyId(PropertyId.of(savedPropertyId));

            // then
            assertThat(found.items()).hasSize(1);
            PropertyAmenity result = found.items().getFirst();
            assertThat(result.additionalPrice().amount()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(result.isFree()).isTrue();
        }
    }

    // -- PT-2: CRUD 동작 --

    @Nested
    @DisplayName("PT-2: CRUD 동작")
    class CrudOperationsTest {

        @Test
        @DisplayName("persistAll로 복수 편의시설을 저장할 수 있다")
        void shouldPersistMultipleAmenities() {
            // given
            Instant now = Instant.now();
            List<PropertyAmenity> amenities = List.of(
                    PropertyAmenity.forNew(PropertyId.of(savedPropertyId), AmenityType.PARKING, AmenityName.of("주차장"), Money.of(0), 1, now),
                    PropertyAmenity.forNew(PropertyId.of(savedPropertyId), AmenityType.POOL, AmenityName.of("수영장"), Money.of(10000), 2, now),
                    PropertyAmenity.forNew(PropertyId.of(savedPropertyId), AmenityType.FITNESS, AmenityName.of("피트니스"), Money.of(5000), 3, now)
            );

            // when
            amenityCommandAdapter.persistAll(amenities);

            // then
            PropertyAmenities found = amenityQueryAdapter.findByPropertyId(PropertyId.of(savedPropertyId));
            assertThat(found.items()).hasSize(3);
        }

        @Test
        @DisplayName("findByPropertyId는 sortOrder 오름차순으로 정렬된 결과를 반환한다")
        void shouldReturnAmenitiesSortedBySortOrder() {
            // given
            Instant now = Instant.now();
            List<PropertyAmenity> amenities = List.of(
                    PropertyAmenity.forNew(PropertyId.of(savedPropertyId), AmenityType.FITNESS, AmenityName.of("피트니스"), Money.of(0), 3, now),
                    PropertyAmenity.forNew(PropertyId.of(savedPropertyId), AmenityType.PARKING, AmenityName.of("주차장"), Money.of(0), 1, now),
                    PropertyAmenity.forNew(PropertyId.of(savedPropertyId), AmenityType.POOL, AmenityName.of("수영장"), Money.of(0), 2, now)
            );

            // when
            amenityCommandAdapter.persistAll(amenities);
            PropertyAmenities found = amenityQueryAdapter.findByPropertyId(PropertyId.of(savedPropertyId));

            // then
            assertThat(found.items()).hasSize(3);
            assertThat(found.items().get(0).sortOrder()).isEqualTo(1);
            assertThat(found.items().get(1).sortOrder()).isEqualTo(2);
            assertThat(found.items().get(2).sortOrder()).isEqualTo(3);
        }

        @Test
        @DisplayName("존재하지 않는 propertyId로 조회 시 빈 컬렉션을 반환한다")
        void shouldReturnEmptyForNonExistingPropertyId() {
            // when
            PropertyAmenities found = amenityQueryAdapter.findByPropertyId(PropertyId.of(99999L));

            // then
            assertThat(found.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("null 또는 빈 리스트로 persistAll 호출 시 예외 없이 무시한다")
        void shouldHandleNullOrEmptyListGracefully() {
            // when & then - 예외 없이 실행 완료
            amenityCommandAdapter.persistAll(null);
            amenityCommandAdapter.persistAll(List.of());

            PropertyAmenities found = amenityQueryAdapter.findByPropertyId(PropertyId.of(savedPropertyId));
            assertThat(found.isEmpty()).isTrue();
        }
    }

    // -- PT-3: Soft Delete 검증 --

    @Nested
    @DisplayName("PT-3: Soft Delete 검증")
    class SoftDeleteTest {

        @Test
        @DisplayName("soft delete 처리된 편의시설은 findByPropertyId에서 조회되지 않는다")
        void shouldNotReturnSoftDeletedAmenities() {
            // given
            Instant now = Instant.now();
            PropertyAmenity amenity = PropertyAmenity.forNew(
                    PropertyId.of(savedPropertyId), AmenityType.POOL, AmenityName.of("수영장"),
                    Money.of(10000), 1, now
            );
            amenityCommandAdapter.persistAll(List.of(amenity));

            // 저장된 편의시설 조회
            PropertyAmenities saved = amenityQueryAdapter.findByPropertyId(PropertyId.of(savedPropertyId));
            assertThat(saved.items()).hasSize(1);

            // when - soft delete 처리 후 다시 저장
            PropertyAmenity savedAmenity = saved.items().getFirst();
            savedAmenity.delete(Instant.now());
            amenityCommandAdapter.persistAll(List.of(savedAmenity));

            // then - 조회 시 제외됨
            PropertyAmenities afterDelete = amenityQueryAdapter.findByPropertyId(PropertyId.of(savedPropertyId));
            assertThat(afterDelete.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("일부만 soft delete 시 나머지는 정상 조회된다")
        void shouldReturnOnlyActiveAmenities() {
            // given
            Instant now = Instant.now();
            List<PropertyAmenity> amenities = List.of(
                    PropertyAmenity.forNew(PropertyId.of(savedPropertyId), AmenityType.PARKING, AmenityName.of("주차장"), Money.of(0), 1, now),
                    PropertyAmenity.forNew(PropertyId.of(savedPropertyId), AmenityType.POOL, AmenityName.of("수영장"), Money.of(10000), 2, now)
            );
            amenityCommandAdapter.persistAll(amenities);

            PropertyAmenities saved = amenityQueryAdapter.findByPropertyId(PropertyId.of(savedPropertyId));
            assertThat(saved.items()).hasSize(2);

            // when - 첫 번째만 soft delete
            PropertyAmenity first = saved.items().getFirst();
            first.delete(Instant.now());
            amenityCommandAdapter.persistAll(List.of(first));

            // then - 두 번째만 조회
            PropertyAmenities afterDelete = amenityQueryAdapter.findByPropertyId(PropertyId.of(savedPropertyId));
            assertThat(afterDelete.items()).hasSize(1);
            assertThat(afterDelete.items().getFirst().amenityType()).isEqualTo(AmenityType.POOL);
        }
    }

    // -- PT-4: Diff 패턴 시나리오 --

    @Nested
    @DisplayName("PT-4: Diff 패턴 시나리오")
    class DiffPatternTest {

        @Test
        @DisplayName("기존 편의시설 조회 -> 새 편의시설 추가 -> 삭제 대상 제거 시나리오")
        void shouldSupportDiffPatternWorkflow() {
            // given - 초기 편의시설 저장
            Instant now = Instant.now();
            List<PropertyAmenity> initial = List.of(
                    PropertyAmenity.forNew(PropertyId.of(savedPropertyId), AmenityType.PARKING, AmenityName.of("주차장"), Money.of(0), 1, now),
                    PropertyAmenity.forNew(PropertyId.of(savedPropertyId), AmenityType.POOL, AmenityName.of("수영장"), Money.of(10000), 2, now)
            );
            amenityCommandAdapter.persistAll(initial);

            // 기존 데이터 조회
            PropertyAmenities existing = amenityQueryAdapter.findByPropertyId(PropertyId.of(savedPropertyId));
            assertThat(existing.items()).hasSize(2);

            // when - 새 편의시설 목록 (PARKING 유지, POOL 제거, FITNESS 추가)
            Instant updateTime = Instant.now();
            PropertyAmenities newAmenities = PropertyAmenities.forNew(List.of(
                    PropertyAmenity.forNew(PropertyId.of(savedPropertyId), AmenityType.PARKING, AmenityName.of("주차장"), Money.of(0), 1, updateTime),
                    PropertyAmenity.forNew(PropertyId.of(savedPropertyId), AmenityType.FITNESS, AmenityName.of("피트니스"), Money.of(5000), 2, updateTime)
            ));

            // diff 계산
            var diff = existing.update(newAmenities);

            // added 저장
            amenityCommandAdapter.persistAll(diff.added());

            // removed soft delete 후 저장
            amenityCommandAdapter.persistAll(diff.removed());

            // then
            PropertyAmenities afterDiff = amenityQueryAdapter.findByPropertyId(PropertyId.of(savedPropertyId));
            assertThat(afterDiff.items()).hasSize(2);

            List<AmenityType> types = afterDiff.stream()
                    .map(PropertyAmenity::amenityType)
                    .toList();
            assertThat(types).containsExactlyInAnyOrder(AmenityType.PARKING, AmenityType.FITNESS);
        }
    }

    // -- PT-5: AmenityType별 조회 검증 --

    @Nested
    @DisplayName("PT-5: 다양한 AmenityType 저장/조회")
    class AmenityTypeTest {

        @Test
        @DisplayName("다양한 AmenityType이 정상적으로 저장/조회된다")
        void shouldPersistAndRetrieveVariousAmenityTypes() {
            // given
            Instant now = Instant.now();
            List<PropertyAmenity> amenities = List.of(
                    PropertyAmenity.forNew(PropertyId.of(savedPropertyId), AmenityType.PARKING, AmenityName.of("주차장"), Money.of(0), 1, now),
                    PropertyAmenity.forNew(PropertyId.of(savedPropertyId), AmenityType.WIFI, AmenityName.of("와이파이"), Money.of(0), 2, now),
                    PropertyAmenity.forNew(PropertyId.of(savedPropertyId), AmenityType.ROOM_SERVICE, AmenityName.of("룸서비스"), Money.of(30000), 3, now),
                    PropertyAmenity.forNew(PropertyId.of(savedPropertyId), AmenityType.FRONT_DESK_24H, AmenityName.of("24시간 프런트"), Money.of(0), 4, now)
            );

            // when
            amenityCommandAdapter.persistAll(amenities);
            PropertyAmenities found = amenityQueryAdapter.findByPropertyId(PropertyId.of(savedPropertyId));

            // then
            assertThat(found.items()).hasSize(4);
            List<AmenityType> types = found.stream()
                    .map(PropertyAmenity::amenityType)
                    .toList();
            assertThat(types).containsExactly(
                    AmenityType.PARKING, AmenityType.WIFI,
                    AmenityType.ROOM_SERVICE, AmenityType.FRONT_DESK_24H
            );
        }
    }
}
