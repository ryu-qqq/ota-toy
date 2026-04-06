package com.ryuqq.otatoy.persistence.roomtype;

import com.ryuqq.otatoy.domain.property.Property;
import com.ryuqq.otatoy.domain.property.PropertyFixture;
import com.ryuqq.otatoy.domain.property.PropertyId;
import com.ryuqq.otatoy.domain.roomattribute.ViewTypeId;
import com.ryuqq.otatoy.domain.roomtype.RoomType;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeId;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeName;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeView;
import com.ryuqq.otatoy.persistence.MySqlTestContainerConfig;
import com.ryuqq.otatoy.persistence.config.JpaConfig;
import com.ryuqq.otatoy.persistence.config.QueryDslConfig;
import com.ryuqq.otatoy.persistence.property.adapter.PropertyCommandAdapter;
import com.ryuqq.otatoy.persistence.property.mapper.PropertyEntityMapper;
import com.ryuqq.otatoy.persistence.roomtype.adapter.RoomTypeCommandAdapter;
import com.ryuqq.otatoy.persistence.roomtype.adapter.RoomTypeViewCommandAdapter;
import com.ryuqq.otatoy.persistence.roomtype.entity.RoomTypeViewJpaEntity;
import com.ryuqq.otatoy.persistence.roomtype.mapper.RoomTypeEntityMapper;
import com.ryuqq.otatoy.persistence.roomtype.mapper.RoomTypeViewEntityMapper;
import com.ryuqq.otatoy.persistence.roomtype.repository.RoomTypeViewJpaRepository;
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
 * RoomTypeView Persistence Adapter 통합 테스트.
 * Testcontainers MySQL + Flyway 기반으로 실제 DB에서 CRUD 동작을 검증한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
        JpaConfig.class,
        QueryDslConfig.class,
        RoomTypeViewCommandAdapter.class,
        RoomTypeViewEntityMapper.class,
        RoomTypeCommandAdapter.class,
        RoomTypeEntityMapper.class,
        PropertyCommandAdapter.class,
        PropertyEntityMapper.class
})
class RoomTypeViewPersistenceAdapterTest extends MySqlTestContainerConfig {

    @Autowired
    private RoomTypeViewCommandAdapter roomTypeViewCommandAdapter;

    @Autowired
    private RoomTypeViewEntityMapper roomTypeViewEntityMapper;

    @Autowired
    private RoomTypeViewJpaRepository roomTypeViewJpaRepository;

    @Autowired
    private RoomTypeCommandAdapter roomTypeCommandAdapter;

    @Autowired
    private PropertyCommandAdapter propertyCommandAdapter;

    private Long propertyId;
    private Long roomTypeId;

    @BeforeEach
    void setUp() {
        // FK 선행 데이터: Property -> RoomType
        Property property = PropertyFixture.aPropertyWithName("전망 테스트 호텔");
        propertyId = propertyCommandAdapter.persist(property);

        RoomType roomType = RoomType.forNew(
                PropertyId.of(propertyId), RoomTypeName.of("디럭스 트윈"),
                null, null, null, 2, 4, 5, null, null, Instant.now()
        );
        roomTypeId = roomTypeCommandAdapter.persist(roomType);
    }

    @Nested
    @DisplayName("PT-1: Domain <-> Entity 매핑 정합성")
    class DomainEntityMappingTest {

        @Test
        @DisplayName("RoomTypeView 저장 후 조회 시 모든 필드가 원본과 동일하다")
        void shouldMapAllFieldsCorrectlyWhenSaveAndFind() {
            // given
            RoomTypeView view = RoomTypeView.forNew(
                    RoomTypeId.of(roomTypeId), ViewTypeId.of(1L), Instant.now()
            );

            // when
            roomTypeViewCommandAdapter.persistAll(List.of(view));

            // then - JpaRepository로 직접 조회하여 Entity 필드 검증
            List<RoomTypeViewJpaEntity> entities = roomTypeViewJpaRepository.findAll();
            assertThat(entities).isNotEmpty();

            RoomTypeViewJpaEntity savedEntity = entities.stream()
                    .filter(e -> e.getRoomTypeId().equals(roomTypeId) && e.getViewTypeId().equals(1L))
                    .findFirst()
                    .orElseThrow();

            assertThat(savedEntity.getRoomTypeId()).isEqualTo(roomTypeId);
            assertThat(savedEntity.getViewTypeId()).isEqualTo(1L);
            assertThat(savedEntity.getCreatedAt()).isNotNull();
            assertThat(savedEntity.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("Entity -> Domain 변환 시 모든 필드가 올바르게 매핑된다")
        void shouldConvertEntityToDomainCorrectly() {
            // given
            RoomTypeView view = RoomTypeView.forNew(
                    RoomTypeId.of(roomTypeId), ViewTypeId.of(5L), Instant.now()
            );
            roomTypeViewCommandAdapter.persistAll(List.of(view));

            // when
            RoomTypeViewJpaEntity entity = roomTypeViewJpaRepository.findAll().stream()
                    .filter(e -> e.getViewTypeId().equals(5L))
                    .findFirst()
                    .orElseThrow();
            RoomTypeView domain = roomTypeViewEntityMapper.toDomain(entity);

            // then
            assertThat(domain.id().value()).isEqualTo(entity.getId());
            assertThat(domain.roomTypeId().value()).isEqualTo(roomTypeId);
            assertThat(domain.viewTypeId().value()).isEqualTo(5L);
            assertThat(domain.createdAt()).isNotNull();
            assertThat(domain.updatedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("PT-2: persistAll 동작 검증")
    class PersistAllTest {

        @Test
        @DisplayName("단일 전망을 저장할 수 있다")
        void shouldPersistSingleView() {
            // given
            RoomTypeView view = RoomTypeView.forNew(
                    RoomTypeId.of(roomTypeId), ViewTypeId.of(1L), Instant.now()
            );

            // when
            roomTypeViewCommandAdapter.persistAll(List.of(view));

            // then
            List<RoomTypeViewJpaEntity> result = roomTypeViewJpaRepository.findAll();
            assertThat(result).anyMatch(e ->
                    e.getRoomTypeId().equals(roomTypeId)
                            && e.getViewTypeId().equals(1L)
            );
        }

        @Test
        @DisplayName("복수 전망을 한번에 저장할 수 있다")
        void shouldPersistMultipleViews() {
            // given
            List<RoomTypeView> views = List.of(
                    RoomTypeView.forNew(RoomTypeId.of(roomTypeId), ViewTypeId.of(1L), Instant.now()),
                    RoomTypeView.forNew(RoomTypeId.of(roomTypeId), ViewTypeId.of(2L), Instant.now()),
                    RoomTypeView.forNew(RoomTypeId.of(roomTypeId), ViewTypeId.of(3L), Instant.now())
            );

            // when
            roomTypeViewCommandAdapter.persistAll(views);

            // then
            List<RoomTypeViewJpaEntity> result = roomTypeViewJpaRepository.findAll().stream()
                    .filter(e -> e.getRoomTypeId().equals(roomTypeId))
                    .toList();
            assertThat(result).hasSize(3);
            assertThat(result).extracting(RoomTypeViewJpaEntity::getViewTypeId)
                    .containsExactlyInAnyOrder(1L, 2L, 3L);
        }

        @Test
        @DisplayName("빈 리스트를 persistAll 해도 예외가 발생하지 않는다")
        void shouldHandleEmptyListGracefully() {
            // when & then - 예외 없이 정상 수행
            roomTypeViewCommandAdapter.persistAll(List.of());

            // 기존 데이터에 영향 없음 확인
            assertThat(roomTypeViewJpaRepository.findAll().stream()
                    .filter(e -> e.getRoomTypeId().equals(roomTypeId))
                    .toList()).isEmpty();
        }
    }

    @Nested
    @DisplayName("PT-3: 번들 패턴 — RoomType 생성 시 View 함께 저장")
    class BundlePatternTest {

        @Test
        @DisplayName("RoomType 저장 후 해당 ID로 View를 함께 저장할 수 있다")
        void shouldPersistViewsWithRoomType() {
            // given - 새 RoomType 생성
            RoomType newRoomType = RoomType.forNew(
                    PropertyId.of(propertyId), RoomTypeName.of("오션뷰 스위트"),
                    null, null, null, 2, 4, 3, null, null, Instant.now()
            );
            Long newRoomTypeId = roomTypeCommandAdapter.persist(newRoomType);

            // Pending 상태의 View에 RoomTypeId 할당
            RoomTypeView pendingView = RoomTypeView.forPending(ViewTypeId.of(1L), Instant.now());
            RoomTypeView assignedView = pendingView.withRoomTypeId(RoomTypeId.of(newRoomTypeId));

            // when
            roomTypeViewCommandAdapter.persistAll(List.of(assignedView));

            // then
            List<RoomTypeViewJpaEntity> result = roomTypeViewJpaRepository.findAll();
            assertThat(result).anyMatch(e ->
                    e.getRoomTypeId().equals(newRoomTypeId)
                            && e.getViewTypeId().equals(1L)
            );
        }

        @Test
        @DisplayName("Pending View 리스트에 RoomTypeId를 할당 후 일괄 저장할 수 있다")
        void shouldPersistPendingViewsWithAssignedRoomTypeId() {
            // given
            Long newRoomTypeId = roomTypeCommandAdapter.persist(
                    RoomType.forNew(PropertyId.of(propertyId), RoomTypeName.of("마운틴뷰 디럭스"),
                            null, null, null, 2, 4, 2, null, null, Instant.now())
            );

            List<RoomTypeView> pendingViews = List.of(
                    RoomTypeView.forPending(ViewTypeId.of(1L), Instant.now()),
                    RoomTypeView.forPending(ViewTypeId.of(2L), Instant.now()),
                    RoomTypeView.forPending(ViewTypeId.of(3L), Instant.now())
            );
            List<RoomTypeView> assignedViews = pendingViews.stream()
                    .map(v -> v.withRoomTypeId(RoomTypeId.of(newRoomTypeId)))
                    .toList();

            // when
            roomTypeViewCommandAdapter.persistAll(assignedViews);

            // then
            List<RoomTypeViewJpaEntity> result = roomTypeViewJpaRepository.findAll().stream()
                    .filter(e -> e.getRoomTypeId().equals(newRoomTypeId))
                    .toList();
            assertThat(result).hasSize(3);
            assertThat(result).extracting(RoomTypeViewJpaEntity::getViewTypeId)
                    .containsExactlyInAnyOrder(1L, 2L, 3L);
        }
    }

    @Nested
    @DisplayName("PT-4: Flyway 마이그레이션 검증")
    class FlywayMigrationTest {

        @Test
        @DisplayName("room_type_view 테이블이 Flyway 마이그레이션으로 정상 생성된다")
        void shouldCreateRoomTypeViewTable() {
            // Flyway 마이그레이션이 실패하면 테스트 자체가 기동되지 않으므로
            // 여기까지 도달했다면 마이그레이션 성공을 의미한다.
            RoomTypeView view = RoomTypeView.forNew(
                    RoomTypeId.of(roomTypeId), ViewTypeId.of(1L), Instant.now()
            );
            roomTypeViewCommandAdapter.persistAll(List.of(view));

            List<RoomTypeViewJpaEntity> result = roomTypeViewJpaRepository.findAll();
            assertThat(result).isNotEmpty();
        }
    }
}
