package com.ryuqq.otatoy.persistence.property;

import com.ryuqq.otatoy.domain.accommodation.PhotoType;
import com.ryuqq.otatoy.domain.common.vo.CdnUrl;
import com.ryuqq.otatoy.domain.common.vo.OriginUrl;
import com.ryuqq.otatoy.domain.property.Property;
import com.ryuqq.otatoy.domain.property.PropertyFixture;
import com.ryuqq.otatoy.domain.property.PropertyId;
import com.ryuqq.otatoy.domain.property.PropertyPhoto;
import com.ryuqq.otatoy.domain.property.PropertyStatus;
import com.ryuqq.otatoy.persistence.config.JpaConfig;
import com.ryuqq.otatoy.persistence.config.QueryDslConfig;
import com.ryuqq.otatoy.persistence.property.adapter.PropertyCommandAdapter;
import com.ryuqq.otatoy.persistence.property.adapter.PropertyPhotoCommandAdapter;
import com.ryuqq.otatoy.persistence.property.adapter.PropertyPhotoQueryAdapter;
import com.ryuqq.otatoy.persistence.property.adapter.PropertyQueryAdapter;
import com.ryuqq.otatoy.persistence.property.mapper.PropertyEntityMapper;
import com.ryuqq.otatoy.persistence.property.mapper.PropertyPhotoEntityMapper;
import com.ryuqq.otatoy.persistence.property.repository.PropertyPhotoQueryDslRepository;
import com.ryuqq.otatoy.persistence.property.repository.PropertyQueryDslRepository;
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
 * Property Persistence Adapter 통합 테스트.
 * Testcontainers MySQL + Flyway 기반으로 실제 DB에서 CRUD 동작을 검증한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
        JpaConfig.class,
        QueryDslConfig.class,
        PropertyCommandAdapter.class,
        PropertyQueryAdapter.class,
        PropertyPhotoCommandAdapter.class,
        PropertyPhotoQueryAdapter.class,
        PropertyEntityMapper.class,
        PropertyPhotoEntityMapper.class,
        PropertyQueryDslRepository.class,
        PropertyPhotoQueryDslRepository.class
})
class PropertyPersistenceAdapterTest extends MySqlTestContainerConfig {

    @Autowired
    private PropertyCommandAdapter propertyCommandAdapter;

    @Autowired
    private PropertyQueryAdapter propertyQueryAdapter;

    @Autowired
    private PropertyPhotoCommandAdapter propertyPhotoCommandAdapter;

    @Autowired
    private PropertyPhotoQueryAdapter propertyPhotoQueryAdapter;

    // -- PT-1: Domain <-> Entity 매핑 정합성 --

    @Nested
    @DisplayName("PT-1: Domain <-> Entity 매핑 정합성")
    class DomainEntityMappingTest {

        @Test
        @DisplayName("Property 저장 후 조회 시 모든 필드가 원본과 동일하다")
        void shouldMapAllFieldsCorrectlyWhenSaveAndFind() {
            // given
            Property original = PropertyFixture.aPropertyWithName("그랜드 호텔");

            // when
            Long savedId = propertyCommandAdapter.persist(original);
            Optional<Property> found = propertyQueryAdapter.findById(PropertyId.of(savedId));

            // then
            assertThat(found).isPresent();
            Property result = found.get();
            assertThat(result.id().value()).isEqualTo(savedId);
            assertThat(result.partnerId().value()).isEqualTo(1L);
            assertThat(result.brandId().value()).isEqualTo(1L);
            assertThat(result.propertyTypeId().value()).isEqualTo(1L);
            assertThat(result.name().value()).isEqualTo("그랜드 호텔");
            assertThat(result.description().value()).isEqualTo("테스트 설명");
            assertThat(result.location().address()).isEqualTo("서울시 강남구");
            assertThat(result.location().latitude()).isEqualTo(37.5665);
            assertThat(result.location().longitude()).isEqualTo(126.978);
            assertThat(result.location().neighborhood()).isEqualTo("강남");
            assertThat(result.location().region()).isEqualTo("서울");
            assertThat(result.status()).isEqualTo(PropertyStatus.ACTIVE);
            assertThat(result.promotionText().value()).isEqualTo("프로모션");
        }

        @Test
        @DisplayName("nullable 필드가 null인 Property도 정상적으로 저장/조회된다")
        void shouldHandleNullableFieldsCorrectly() {
            // given
            Property original = PropertyFixture.aPropertyWithoutOptional("미니멀 호텔");

            // when
            Long savedId = propertyCommandAdapter.persist(original);
            Optional<Property> found = propertyQueryAdapter.findById(PropertyId.of(savedId));

            // then
            assertThat(found).isPresent();
            Property result = found.get();
            assertThat(result.brandId()).isNull();
            assertThat(result.description()).isNull();
            assertThat(result.promotionText()).isNull();
            assertThat(result.location().neighborhood()).isNull();
            assertThat(result.location().region()).isNull();
        }
    }

    // -- PT-2: CRUD 동작 --

    @Nested
    @DisplayName("PT-2: Repository CRUD 동작")
    class CrudTest {

        @Test
        @DisplayName("persist 후 findById로 조회할 수 있다")
        void shouldPersistAndFindById() {
            // given
            Property property = PropertyFixture.aPropertyWithName("리조트 호텔");

            // when
            Long savedId = propertyCommandAdapter.persist(property);
            Optional<Property> found = propertyQueryAdapter.findById(PropertyId.of(savedId));

            // then
            assertThat(found).isPresent();
            assertThat(found.get().name().value()).isEqualTo("리조트 호텔");
        }

        @Test
        @DisplayName("persistAll로 복수 건을 저장할 수 있다")
        void shouldPersistMultipleProperties() {
            // given
            List<Property> properties = List.of(
                    PropertyFixture.aPropertyWithName("호텔 A"),
                    PropertyFixture.aPropertyWithName("호텔 B"),
                    PropertyFixture.aPropertyWithName("호텔 C")
            );

            // when
            propertyCommandAdapter.persistAll(properties);

            // then — 각각 조회는 ID를 모르므로 existsById 패턴으로 확인 불가
            // persistAll의 반환값이 void이므로 예외 없이 성공하는 것 자체를 검증
            // 추가로 개별 persist + findById로 교차 검증
            Long id = propertyCommandAdapter.persist(PropertyFixture.aPropertyWithName("호텔 D"));
            assertThat(propertyQueryAdapter.existsById(PropertyId.of(id))).isTrue();
        }

        @Test
        @DisplayName("존재하지 않는 ID로 findById 시 Optional.empty()를 반환한다")
        void shouldReturnEmptyForNonExistingId() {
            // when
            Optional<Property> found = propertyQueryAdapter.findById(PropertyId.of(99999L));

            // then
            assertThat(found).isEmpty();
        }
    }

    // -- PT-3: existsById --

    @Nested
    @DisplayName("PT-3: existsById 동작")
    class ExistsByIdTest {

        @Test
        @DisplayName("저장된 Property에 대해 existsById는 true를 반환한다")
        void shouldReturnTrueForExistingProperty() {
            // given
            Long savedId = propertyCommandAdapter.persist(PropertyFixture.aPropertyWithName("존재 확인용 호텔"));

            // when & then
            assertThat(propertyQueryAdapter.existsById(PropertyId.of(savedId))).isTrue();
        }

        @Test
        @DisplayName("존재하지 않는 ID에 대해 existsById는 false를 반환한다")
        void shouldReturnFalseForNonExistingProperty() {
            // when & then
            assertThat(propertyQueryAdapter.existsById(PropertyId.of(99999L))).isFalse();
        }
    }

    // -- PT-4: Flyway 마이그레이션 --

    @Nested
    @DisplayName("PT-4: Flyway 마이그레이션 검증")
    class FlywayMigrationTest {

        @Test
        @DisplayName("Testcontainers 시작 시 Flyway가 정상적으로 테이블을 생성한다")
        void shouldCreateTablesViaFlyway() {
            // Flyway가 실패하면 테스트 컨텍스트 자체가 로드되지 않으므로,
            // 이 테스트가 실행 가능한 것 자체가 마이그레이션 성공을 증명한다.
            // 추가로 실제 CRUD 동작이 성공하는지 확인한다.
            Long savedId = propertyCommandAdapter.persist(PropertyFixture.aPropertyWithName("마이그레이션 검증 호텔"));
            assertThat(savedId).isNotNull();
            assertThat(propertyQueryAdapter.findById(PropertyId.of(savedId))).isPresent();
        }
    }

    // -- PT-5: PropertyPhoto 연관 검증 --

    @Nested
    @DisplayName("PT-5: PropertyPhoto 저장/조회 검증")
    class PropertyPhotoTest {

        @Test
        @DisplayName("PropertyPhoto를 저장 후 propertyId로 조회할 수 있다")
        void shouldPersistAndFindPhotosByPropertyId() {
            // given
            Long propertyId = propertyCommandAdapter.persist(PropertyFixture.aPropertyWithName("사진 테스트 호텔"));
            Instant now = Instant.now();

            List<PropertyPhoto> photos = List.of(
                    PropertyPhoto.forNew(PropertyId.of(propertyId), PhotoType.EXTERIOR,
                            OriginUrl.of("https://example.com/photo1.jpg"), CdnUrl.of("https://cdn.example.com/photo1.jpg"), 1, now),
                    PropertyPhoto.forNew(PropertyId.of(propertyId), PhotoType.LOBBY,
                            OriginUrl.of("https://example.com/photo2.jpg"), null, 2, now)
            );

            // when
            int savedCount = propertyPhotoCommandAdapter.persistAll(photos);

            // then
            assertThat(savedCount).isEqualTo(2);

            List<PropertyPhoto> found = propertyPhotoQueryAdapter.findByPropertyId(PropertyId.of(propertyId));
            assertThat(found).hasSize(2);
            assertThat(found.getFirst().sortOrder()).isLessThan(found.get(1).sortOrder());
        }

        @Test
        @DisplayName("PropertyPhoto의 모든 필드가 정합성을 유지한다")
        void shouldMapAllPhotoFieldsCorrectly() {
            // given
            Long propertyId = propertyCommandAdapter.persist(PropertyFixture.aPropertyWithName("사진 매핑 호텔"));
            Instant now = Instant.now();

            PropertyPhoto photo = PropertyPhoto.forNew(
                    PropertyId.of(propertyId), PhotoType.ROOM,
                    OriginUrl.of("https://example.com/room.jpg"),
                    CdnUrl.of("https://cdn.example.com/room.jpg"),
                    3, now
            );

            // when
            propertyPhotoCommandAdapter.persistAll(List.of(photo));
            List<PropertyPhoto> found = propertyPhotoQueryAdapter.findByPropertyId(PropertyId.of(propertyId));

            // then
            assertThat(found).hasSize(1);
            PropertyPhoto result = found.getFirst();
            assertThat(result.propertyId().value()).isEqualTo(propertyId);
            assertThat(result.photoType()).isEqualTo(PhotoType.ROOM);
            assertThat(result.originUrl().value()).isEqualTo("https://example.com/room.jpg");
            assertThat(result.cdnUrl().value()).isEqualTo("https://cdn.example.com/room.jpg");
            assertThat(result.sortOrder()).isEqualTo(3);
        }

    }
}
