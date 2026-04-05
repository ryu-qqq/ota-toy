package com.ryuqq.otatoy.persistence.propertytype;

import com.ryuqq.otatoy.domain.propertytype.PropertyType;
import com.ryuqq.otatoy.domain.propertytype.PropertyTypeAttribute;
import com.ryuqq.otatoy.domain.propertytype.PropertyTypeId;
import com.ryuqq.otatoy.persistence.config.JpaConfig;
import com.ryuqq.otatoy.persistence.config.QueryDslConfig;
import com.ryuqq.otatoy.persistence.propertytype.adapter.PropertyTypeQueryAdapter;
import com.ryuqq.otatoy.persistence.propertytype.entity.PropertyTypeAttributeJpaEntity;
import com.ryuqq.otatoy.persistence.propertytype.entity.PropertyTypeJpaEntity;
import com.ryuqq.otatoy.persistence.propertytype.mapper.PropertyTypeEntityMapper;
import com.ryuqq.otatoy.persistence.propertytype.repository.PropertyTypeQueryDslRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import com.ryuqq.otatoy.persistence.MySqlTestContainerConfig;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * PropertyType Persistence Adapter 통합 테스트.
 * Testcontainers MySQL + Flyway 기반으로 PropertyType QueryAdapter 동작을 검증한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
        JpaConfig.class,
        QueryDslConfig.class,
        PropertyTypeQueryAdapter.class,
        PropertyTypeEntityMapper.class,
        PropertyTypeQueryDslRepository.class
})
class PropertyTypePersistenceAdapterTest extends MySqlTestContainerConfig {

    @Autowired
    private PropertyTypeQueryAdapter propertyTypeQueryAdapter;

    @Autowired
    private EntityManager entityManager;

    private Long insertPropertyType(String code, String name, String description) {
        Instant now = Instant.now();
        PropertyTypeJpaEntity entity = PropertyTypeJpaEntity.create(null, code, name, description, now, now, null);
        entityManager.persist(entity);
        entityManager.flush();
        return entity.getId();
    }

    private Long insertPropertyTypeAttribute(Long propertyTypeId, String key, String name,
                                              String valueType, boolean required, int sortOrder) {
        Instant now = Instant.now();
        PropertyTypeAttributeJpaEntity entity = PropertyTypeAttributeJpaEntity.create(
                null, propertyTypeId, key, name, valueType, required, sortOrder, now, now, null);
        entityManager.persist(entity);
        entityManager.flush();
        return entity.getId();
    }

    @Nested
    @DisplayName("PropertyType existsById 동작 검증")
    class ExistsByIdTest {

        @Test
        @DisplayName("저장된 PropertyType에 대해 existsById는 true를 반환한다")
        void shouldReturnTrueForExistingPropertyType() {
            // given
            Long typeId = insertPropertyType("HOTEL", "호텔", "일반 호텔");

            // when & then
            assertThat(propertyTypeQueryAdapter.existsById(PropertyTypeId.of(typeId))).isTrue();
        }

        @Test
        @DisplayName("존재하지 않는 ID에 대해 existsById는 false를 반환한다")
        void shouldReturnFalseForNonExistingPropertyType() {
            // when & then
            assertThat(propertyTypeQueryAdapter.existsById(PropertyTypeId.of(99999L))).isFalse();
        }
    }

    @Nested
    @DisplayName("PropertyType findById 동작 검증")
    class FindByIdTest {

        @Test
        @DisplayName("저장된 PropertyType을 findById로 조회하면 Domain 객체로 변환된다")
        void shouldReturnDomainObjectWhenFound() {
            // given
            Long typeId = insertPropertyType("RESORT", "리조트", "리조트 유형");

            // when
            Optional<PropertyType> found = propertyTypeQueryAdapter.findById(PropertyTypeId.of(typeId));

            // then
            assertThat(found).isPresent();
            PropertyType propertyType = found.get();
            assertThat(propertyType.id().value()).isEqualTo(typeId);
            assertThat(propertyType.code().value()).isEqualTo("RESORT");
            assertThat(propertyType.name().value()).isEqualTo("리조트");
        }

        @Test
        @DisplayName("존재하지 않는 ID로 findById 시 Optional.empty()를 반환한다")
        void shouldReturnEmptyForNonExistingId() {
            // when
            Optional<PropertyType> found = propertyTypeQueryAdapter.findById(PropertyTypeId.of(99999L));

            // then
            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("PropertyTypeAttribute 조회 검증")
    class FindAttributesTest {

        @Test
        @DisplayName("PropertyType에 속한 속성 목록을 정렬 순서대로 조회한다")
        void shouldFindAttributesByPropertyTypeId() {
            // given
            Long typeId = insertPropertyType("PENSION", "펜션", "펜션 유형");
            insertPropertyTypeAttribute(typeId, "bbq_available", "바베큐 가능 여부", "BOOLEAN", true, 2);
            insertPropertyTypeAttribute(typeId, "max_rooms", "최대 객실 수", "NUMBER", false, 1);

            // when
            List<PropertyTypeAttribute> attributes =
                    propertyTypeQueryAdapter.findAttributesByPropertyTypeId(PropertyTypeId.of(typeId));

            // then
            assertThat(attributes).hasSize(2);
            // sortOrder 오름차순이므로 max_rooms(1)가 먼저
            assertThat(attributes.getFirst().attributeKey()).isEqualTo("max_rooms");
            assertThat(attributes.getFirst().sortOrder()).isEqualTo(1);
            assertThat(attributes.get(1).attributeKey()).isEqualTo("bbq_available");
            assertThat(attributes.get(1).sortOrder()).isEqualTo(2);
        }

        @Test
        @DisplayName("속성이 없는 PropertyType의 속성 목록은 빈 리스트이다")
        void shouldReturnEmptyListWhenNoAttributes() {
            // given
            Long typeId = insertPropertyType("MOTEL", "모텔", "모텔 유형");

            // when
            List<PropertyTypeAttribute> attributes =
                    propertyTypeQueryAdapter.findAttributesByPropertyTypeId(PropertyTypeId.of(typeId));

            // then
            assertThat(attributes).isEmpty();
        }
    }

    @Nested
    @DisplayName("PropertyType Soft Delete 필터 검증")
    class SoftDeleteTest {

        @Test
        @DisplayName("soft delete된 PropertyType은 조회되지 않는다")
        void shouldNotFindSoftDeletedPropertyType() {
            // given
            Instant now = Instant.now();
            PropertyTypeJpaEntity deletedEntity = PropertyTypeJpaEntity.create(
                    null, "DELETED_TYPE", "삭제 유형", "삭제된 유형", now, now, now);
            entityManager.persist(deletedEntity);
            entityManager.flush();
            Long deletedId = deletedEntity.getId();

            // when
            boolean exists = propertyTypeQueryAdapter.existsById(PropertyTypeId.of(deletedId));
            Optional<PropertyType> found = propertyTypeQueryAdapter.findById(PropertyTypeId.of(deletedId));

            // then
            assertThat(exists).isFalse();
            assertThat(found).isEmpty();
        }
    }
}
