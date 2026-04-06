package com.ryuqq.otatoy.persistence.property;

import com.ryuqq.otatoy.domain.property.PropertyAttributeValue;
import com.ryuqq.otatoy.domain.property.PropertyAttributeValues;
import com.ryuqq.otatoy.domain.property.PropertyFixture;
import com.ryuqq.otatoy.domain.property.PropertyId;
import com.ryuqq.otatoy.domain.propertytype.PropertyTypeAttributeId;
import com.ryuqq.otatoy.persistence.MySqlTestContainerConfig;
import com.ryuqq.otatoy.persistence.config.JpaConfig;
import com.ryuqq.otatoy.persistence.config.QueryDslConfig;
import com.ryuqq.otatoy.persistence.property.adapter.PropertyAttributeValueCommandAdapter;
import com.ryuqq.otatoy.persistence.property.adapter.PropertyAttributeValueQueryAdapter;
import com.ryuqq.otatoy.persistence.property.adapter.PropertyCommandAdapter;
import com.ryuqq.otatoy.persistence.property.mapper.PropertyAttributeValueEntityMapper;
import com.ryuqq.otatoy.persistence.property.mapper.PropertyEntityMapper;
import com.ryuqq.otatoy.persistence.property.repository.PropertyAttributeValueQueryDslRepository;
import com.ryuqq.otatoy.persistence.property.repository.PropertyQueryDslRepository;
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
 * PropertyAttributeValue Persistence Adapter 통합 테스트.
 * Testcontainers MySQL + Flyway 기반으로 EAV 속성값 CRUD 동작을 검증한다.
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
        PropertyAttributeValueCommandAdapter.class,
        PropertyAttributeValueQueryAdapter.class,
        PropertyEntityMapper.class,
        PropertyAttributeValueEntityMapper.class,
        PropertyQueryDslRepository.class,
        PropertyAttributeValueQueryDslRepository.class
})
class PropertyAttributeValuePersistenceAdapterTest extends MySqlTestContainerConfig {

    @Autowired
    private PropertyCommandAdapter propertyCommandAdapter;

    @Autowired
    private PropertyAttributeValueCommandAdapter attributeValueCommandAdapter;

    @Autowired
    private PropertyAttributeValueQueryAdapter attributeValueQueryAdapter;

    private Long savedPropertyId;

    @BeforeEach
    void setUp() {
        // FK 선행 데이터: Property 먼저 생성
        savedPropertyId = propertyCommandAdapter.persist(PropertyFixture.aPropertyWithName("속성값 테스트 호텔"));
    }

    // -- PT-1: Domain <-> Entity 매핑 정합성 --

    @Nested
    @DisplayName("PT-1: Domain <-> Entity 매핑 정합성")
    class DomainEntityMappingTest {

        @Test
        @DisplayName("PropertyAttributeValue 저장 후 조회 시 모든 필드가 원본과 동일하다")
        void shouldMapAllFieldsCorrectlyWhenSaveAndFind() {
            // given
            Instant now = Instant.now();
            PropertyAttributeValue attrValue = PropertyAttributeValue.forNew(
                    PropertyId.of(savedPropertyId),
                    PropertyTypeAttributeId.of(100L),
                    "5성급",
                    now
            );

            // when
            attributeValueCommandAdapter.persistAll(List.of(attrValue));
            PropertyAttributeValues found = attributeValueQueryAdapter.findByPropertyId(PropertyId.of(savedPropertyId));

            // then
            assertThat(found.items()).hasSize(1);
            PropertyAttributeValue result = found.items().getFirst();
            assertThat(result.id().value()).isNotNull();
            assertThat(result.propertyId().value()).isEqualTo(savedPropertyId);
            assertThat(result.propertyTypeAttributeId().value()).isEqualTo(100L);
            assertThat(result.value()).isEqualTo("5성급");
            assertThat(result.deletionStatus().deleted()).isFalse();
        }

        @Test
        @DisplayName("긴 문자열 value도 정상적으로 저장/조회된다")
        void shouldHandleLongValueString() {
            // given
            Instant now = Instant.now();
            String longValue = "A".repeat(500); // 최대 길이 500
            PropertyAttributeValue attrValue = PropertyAttributeValue.forNew(
                    PropertyId.of(savedPropertyId),
                    PropertyTypeAttributeId.of(200L),
                    longValue,
                    now
            );

            // when
            attributeValueCommandAdapter.persistAll(List.of(attrValue));
            PropertyAttributeValues found = attributeValueQueryAdapter.findByPropertyId(PropertyId.of(savedPropertyId));

            // then
            assertThat(found.items()).hasSize(1);
            assertThat(found.items().getFirst().value()).isEqualTo(longValue);
        }
    }

    // -- PT-2: CRUD 동작 --

    @Nested
    @DisplayName("PT-2: CRUD 동작")
    class CrudOperationsTest {

        @Test
        @DisplayName("persistAll로 복수 속성값을 저장할 수 있다")
        void shouldPersistMultipleAttributeValues() {
            // given
            Instant now = Instant.now();
            List<PropertyAttributeValue> values = List.of(
                    PropertyAttributeValue.forNew(PropertyId.of(savedPropertyId), PropertyTypeAttributeId.of(1L), "5성급", now),
                    PropertyAttributeValue.forNew(PropertyId.of(savedPropertyId), PropertyTypeAttributeId.of(2L), "체크인 15:00", now),
                    PropertyAttributeValue.forNew(PropertyId.of(savedPropertyId), PropertyTypeAttributeId.of(3L), "체크아웃 11:00", now)
            );

            // when
            attributeValueCommandAdapter.persistAll(values);

            // then
            PropertyAttributeValues found = attributeValueQueryAdapter.findByPropertyId(PropertyId.of(savedPropertyId));
            assertThat(found.items()).hasSize(3);
        }

        @Test
        @DisplayName("존재하지 않는 propertyId로 조회 시 빈 컬렉션을 반환한다")
        void shouldReturnEmptyForNonExistingPropertyId() {
            // when
            PropertyAttributeValues found = attributeValueQueryAdapter.findByPropertyId(PropertyId.of(99999L));

            // then
            assertThat(found.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("null 또는 빈 리스트로 persistAll 호출 시 예외 없이 무시한다")
        void shouldHandleNullOrEmptyListGracefully() {
            // when & then - 예외 없이 실행 완료
            attributeValueCommandAdapter.persistAll(null);
            attributeValueCommandAdapter.persistAll(List.of());

            PropertyAttributeValues found = attributeValueQueryAdapter.findByPropertyId(PropertyId.of(savedPropertyId));
            assertThat(found.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("attributeId와 value 매핑이 정확하게 유지된다")
        void shouldMaintainCorrectAttributeIdAndValueMapping() {
            // given
            Instant now = Instant.now();
            List<PropertyAttributeValue> values = List.of(
                    PropertyAttributeValue.forNew(PropertyId.of(savedPropertyId), PropertyTypeAttributeId.of(10L), "럭셔리", now),
                    PropertyAttributeValue.forNew(PropertyId.of(savedPropertyId), PropertyTypeAttributeId.of(20L), "오션뷰", now),
                    PropertyAttributeValue.forNew(PropertyId.of(savedPropertyId), PropertyTypeAttributeId.of(30L), "반려동물 가능", now)
            );

            // when
            attributeValueCommandAdapter.persistAll(values);
            PropertyAttributeValues found = attributeValueQueryAdapter.findByPropertyId(PropertyId.of(savedPropertyId));

            // then - 각 attributeId에 대해 올바른 value가 매핑되어 있는지 검증
            assertThat(found.items()).hasSize(3);
            for (PropertyAttributeValue item : found.items()) {
                if (item.propertyTypeAttributeId().value() == 10L) {
                    assertThat(item.value()).isEqualTo("럭셔리");
                } else if (item.propertyTypeAttributeId().value() == 20L) {
                    assertThat(item.value()).isEqualTo("오션뷰");
                } else if (item.propertyTypeAttributeId().value() == 30L) {
                    assertThat(item.value()).isEqualTo("반려동물 가능");
                }
            }
        }
    }

    // -- PT-3: Soft Delete 검증 --

    @Nested
    @DisplayName("PT-3: Soft Delete 검증")
    class SoftDeleteTest {

        @Test
        @DisplayName("soft delete 처리된 속성값은 findByPropertyId에서 조회되지 않는다")
        void shouldNotReturnSoftDeletedAttributeValues() {
            // given
            Instant now = Instant.now();
            PropertyAttributeValue attrValue = PropertyAttributeValue.forNew(
                    PropertyId.of(savedPropertyId), PropertyTypeAttributeId.of(100L), "테스트 값", now
            );
            attributeValueCommandAdapter.persistAll(List.of(attrValue));

            PropertyAttributeValues saved = attributeValueQueryAdapter.findByPropertyId(PropertyId.of(savedPropertyId));
            assertThat(saved.items()).hasSize(1);

            // when - soft delete 처리 후 다시 저장
            PropertyAttributeValue savedValue = saved.items().getFirst();
            savedValue.delete(Instant.now());
            attributeValueCommandAdapter.persistAll(List.of(savedValue));

            // then - 조회 시 제외됨
            PropertyAttributeValues afterDelete = attributeValueQueryAdapter.findByPropertyId(PropertyId.of(savedPropertyId));
            assertThat(afterDelete.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("일부만 soft delete 시 나머지는 정상 조회된다")
        void shouldReturnOnlyActiveAttributeValues() {
            // given
            Instant now = Instant.now();
            List<PropertyAttributeValue> values = List.of(
                    PropertyAttributeValue.forNew(PropertyId.of(savedPropertyId), PropertyTypeAttributeId.of(1L), "값1", now),
                    PropertyAttributeValue.forNew(PropertyId.of(savedPropertyId), PropertyTypeAttributeId.of(2L), "값2", now)
            );
            attributeValueCommandAdapter.persistAll(values);

            PropertyAttributeValues saved = attributeValueQueryAdapter.findByPropertyId(PropertyId.of(savedPropertyId));
            assertThat(saved.items()).hasSize(2);

            // when - 첫 번째만 soft delete
            PropertyAttributeValue first = saved.items().getFirst();
            first.delete(Instant.now());
            attributeValueCommandAdapter.persistAll(List.of(first));

            // then - 두 번째만 조회
            PropertyAttributeValues afterDelete = attributeValueQueryAdapter.findByPropertyId(PropertyId.of(savedPropertyId));
            assertThat(afterDelete.items()).hasSize(1);
            assertThat(afterDelete.items().getFirst().value()).isEqualTo("값2");
        }
    }

    // -- PT-4: Diff 패턴 시나리오 --

    @Nested
    @DisplayName("PT-4: Diff 패턴 시나리오")
    class DiffPatternTest {

        @Test
        @DisplayName("기존 속성값 조회 -> 새 속성값 추가 -> 삭제 대상 제거 시나리오")
        void shouldSupportDiffPatternWorkflow() {
            // given - 초기 속성값 저장
            Instant now = Instant.now();
            List<PropertyAttributeValue> initial = List.of(
                    PropertyAttributeValue.forNew(PropertyId.of(savedPropertyId), PropertyTypeAttributeId.of(1L), "5성급", now),
                    PropertyAttributeValue.forNew(PropertyId.of(savedPropertyId), PropertyTypeAttributeId.of(2L), "체크인 15:00", now)
            );
            attributeValueCommandAdapter.persistAll(initial);

            // 기존 데이터 조회
            PropertyAttributeValues existing = attributeValueQueryAdapter.findByPropertyId(PropertyId.of(savedPropertyId));
            assertThat(existing.items()).hasSize(2);

            // when - 새 속성값 목록 (attributeId=1 유지, attributeId=2 제거, attributeId=3 추가)
            Instant updateTime = Instant.now();
            PropertyAttributeValues newValues = PropertyAttributeValues.forNew(List.of(
                    PropertyAttributeValue.forNew(PropertyId.of(savedPropertyId), PropertyTypeAttributeId.of(1L), "5성급", updateTime),
                    PropertyAttributeValue.forNew(PropertyId.of(savedPropertyId), PropertyTypeAttributeId.of(3L), "반려동물 가능", updateTime)
            ));

            // diff 계산
            var diff = existing.update(newValues);

            // added 저장
            attributeValueCommandAdapter.persistAll(diff.added());

            // removed soft delete 후 저장
            attributeValueCommandAdapter.persistAll(diff.removed());

            // then
            PropertyAttributeValues afterDiff = attributeValueQueryAdapter.findByPropertyId(PropertyId.of(savedPropertyId));
            assertThat(afterDiff.items()).hasSize(2);

            List<Long> attrIds = afterDiff.stream()
                    .map(v -> v.propertyTypeAttributeId().value())
                    .toList();
            assertThat(attrIds).containsExactlyInAnyOrder(1L, 3L);
        }
    }

    // -- PT-5: 서로 다른 Property의 속성값 격리 --

    @Nested
    @DisplayName("PT-5: Property 간 속성값 격리")
    class PropertyIsolationTest {

        @Test
        @DisplayName("서로 다른 Property의 속성값은 격리되어 조회된다")
        void shouldIsolateAttributeValuesBetweenProperties() {
            // given
            Long anotherPropertyId = propertyCommandAdapter.persist(PropertyFixture.aPropertyWithName("다른 호텔"));
            Instant now = Instant.now();

            attributeValueCommandAdapter.persistAll(List.of(
                    PropertyAttributeValue.forNew(PropertyId.of(savedPropertyId), PropertyTypeAttributeId.of(1L), "값A", now)
            ));
            attributeValueCommandAdapter.persistAll(List.of(
                    PropertyAttributeValue.forNew(PropertyId.of(anotherPropertyId), PropertyTypeAttributeId.of(1L), "값B", now),
                    PropertyAttributeValue.forNew(PropertyId.of(anotherPropertyId), PropertyTypeAttributeId.of(2L), "값C", now)
            ));

            // when
            PropertyAttributeValues firstPropertyValues = attributeValueQueryAdapter.findByPropertyId(PropertyId.of(savedPropertyId));
            PropertyAttributeValues secondPropertyValues = attributeValueQueryAdapter.findByPropertyId(PropertyId.of(anotherPropertyId));

            // then
            assertThat(firstPropertyValues.items()).hasSize(1);
            assertThat(firstPropertyValues.items().getFirst().value()).isEqualTo("값A");

            assertThat(secondPropertyValues.items()).hasSize(2);
        }
    }
}
