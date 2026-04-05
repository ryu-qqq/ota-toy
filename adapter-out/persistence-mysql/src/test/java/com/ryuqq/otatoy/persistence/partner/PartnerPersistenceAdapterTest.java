package com.ryuqq.otatoy.persistence.partner;

import com.ryuqq.otatoy.domain.partner.Partner;
import com.ryuqq.otatoy.domain.partner.PartnerId;
import com.ryuqq.otatoy.persistence.config.JpaConfig;
import com.ryuqq.otatoy.persistence.config.QueryDslConfig;
import com.ryuqq.otatoy.persistence.partner.adapter.PartnerQueryAdapter;
import com.ryuqq.otatoy.persistence.partner.entity.PartnerJpaEntity;
import com.ryuqq.otatoy.persistence.partner.mapper.PartnerEntityMapper;
import com.ryuqq.otatoy.persistence.partner.repository.PartnerQueryDslRepository;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Partner Persistence Adapter 통합 테스트.
 * Testcontainers MySQL + Flyway 기반으로 Partner QueryAdapter 동작을 검증한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
        JpaConfig.class,
        QueryDslConfig.class,
        PartnerQueryAdapter.class,
        PartnerEntityMapper.class,
        PartnerQueryDslRepository.class
})
class PartnerPersistenceAdapterTest extends MySqlTestContainerConfig {

    @Autowired
    private PartnerQueryAdapter partnerQueryAdapter;

    @Autowired
    private EntityManager entityManager;

    /**
     * Partner는 CommandAdapter가 없으므로 EntityManager로 직접 데이터를 삽입한다.
     */
    private Long insertPartner(String name, String status) {
        Instant now = Instant.now();
        PartnerJpaEntity entity = PartnerJpaEntity.create(null, name, status, now, now, null);
        entityManager.persist(entity);
        entityManager.flush();
        return entity.getId();
    }

    @Nested
    @DisplayName("Partner existsById 동작 검증")
    class ExistsByIdTest {

        @Test
        @DisplayName("저장된 Partner에 대해 existsById는 true를 반환한다")
        void shouldReturnTrueForExistingPartner() {
            // given
            Long partnerId = insertPartner("테스트 파트너", "ACTIVE");

            // when & then
            assertThat(partnerQueryAdapter.existsById(PartnerId.of(partnerId))).isTrue();
        }

        @Test
        @DisplayName("존재하지 않는 ID에 대해 existsById는 false를 반환한다")
        void shouldReturnFalseForNonExistingPartner() {
            // when & then
            assertThat(partnerQueryAdapter.existsById(PartnerId.of(99999L))).isFalse();
        }
    }

    @Nested
    @DisplayName("Partner findById 동작 검증")
    class FindByIdTest {

        @Test
        @DisplayName("저장된 Partner를 findById로 조회하면 Domain 객체로 변환된다")
        void shouldReturnDomainObjectWhenFound() {
            // given
            Long partnerId = insertPartner("그랜드 호텔 파트너", "ACTIVE");

            // when
            Optional<Partner> found = partnerQueryAdapter.findById(PartnerId.of(partnerId));

            // then
            assertThat(found).isPresent();
            Partner partner = found.get();
            assertThat(partner.id().value()).isEqualTo(partnerId);
            assertThat(partner.name().value()).isEqualTo("그랜드 호텔 파트너");
            assertThat(partner.isActive()).isTrue();
        }

        @Test
        @DisplayName("존재하지 않는 ID로 findById 시 Optional.empty()를 반환한다")
        void shouldReturnEmptyForNonExistingId() {
            // when
            Optional<Partner> found = partnerQueryAdapter.findById(PartnerId.of(99999L));

            // then
            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("Partner Soft Delete 필터 검증")
    class SoftDeleteTest {

        @Test
        @DisplayName("soft delete된 Partner는 조회되지 않는다")
        void shouldNotFindSoftDeletedPartner() {
            // given — deleted=true인 Partner를 직접 삽입
            Instant now = Instant.now();
            PartnerJpaEntity deletedEntity = PartnerJpaEntity.create(null, "삭제된 파트너", "ACTIVE", now, now, now);
            entityManager.persist(deletedEntity);
            entityManager.flush();
            Long deletedId = deletedEntity.getId();

            // when
            boolean exists = partnerQueryAdapter.existsById(PartnerId.of(deletedId));
            Optional<Partner> found = partnerQueryAdapter.findById(PartnerId.of(deletedId));

            // then
            assertThat(exists).isFalse();
            assertThat(found).isEmpty();
        }
    }
}
