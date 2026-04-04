package com.ryuqq.otatoy.domain.supplier;

import com.ryuqq.otatoy.domain.property.PropertyId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SupplierPropertyTest {

    private static final Instant NOW = Instant.parse("2026-04-04T00:00:00Z");

    @Nested
    @DisplayName("T-1: 생성 검증 -- forNew()")
    class Creation {

        @Test
        @DisplayName("정상 생성 시 MAPPED 상태이고 id.value()는 null이다")
        void shouldCreateWithMappedStatusAndNullId() {
            SupplierProperty sp = SupplierFixture.mappedProperty();

            assertThat(sp.id().value()).isNull();
            assertThat(sp.status()).isEqualTo(SupplierMappingStatus.MAPPED);
            assertThat(sp.supplierId()).isEqualTo(SupplierId.of(1L));
            assertThat(sp.propertyId()).isEqualTo(PropertyId.of(100L));
            assertThat(sp.supplierPropertyCode()).isEqualTo("EXT-PROP-001");
            assertThat(sp.lastSyncedAt()).isNull();
        }

        @Test
        @DisplayName("supplierPropertyCode가 null이면 생성 실패")
        void shouldFailWhenCodeIsNull() {
            assertThatThrownBy(() -> SupplierProperty.forNew(
                    SupplierId.of(1L), PropertyId.of(100L), null
            )).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("공급자 숙소 코드");
        }

        @Test
        @DisplayName("supplierPropertyCode가 blank이면 생성 실패")
        void shouldFailWhenCodeIsBlank() {
            assertThatThrownBy(() -> SupplierProperty.forNew(
                    SupplierId.of(1L), PropertyId.of(100L), "  "
            )).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("공급자 숙소 코드");
        }
    }

    @Nested
    @DisplayName("T-2: reconstitute 검증")
    class Reconstitution {

        @Test
        @DisplayName("reconstitute는 모든 필드를 그대로 복원한다")
        void shouldReconstituteAllFields() {
            SupplierProperty sp = SupplierFixture.reconstitutedProperty(SupplierMappingStatus.MAPPED);

            assertThat(sp.id()).isEqualTo(SupplierPropertyId.of(1L));
            assertThat(sp.status()).isEqualTo(SupplierMappingStatus.MAPPED);
            assertThat(sp.lastSyncedAt()).isEqualTo(NOW);
        }
    }

    @Nested
    @DisplayName("T-3: synced()")
    class Synced {

        @Test
        @DisplayName("synced 호출 시 lastSyncedAt이 갱신된다")
        void shouldUpdateLastSyncedAt() {
            SupplierProperty sp = SupplierFixture.mappedProperty();
            Instant syncedAt = Instant.parse("2026-04-04T12:00:00Z");

            sp.synced(syncedAt);

            assertThat(sp.lastSyncedAt()).isEqualTo(syncedAt);
        }

        @Test
        @DisplayName("UNMAPPED 상태에서 synced 호출 시 예외가 발생한다")
        void shouldFailWhenSyncedOnUnmapped() {
            SupplierProperty sp = SupplierFixture.unmappedProperty();

            assertThatThrownBy(() -> sp.synced(Instant.now()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("매핑 해제된 상태에서는 동기화할 수 없습니다");
        }
    }

    @Nested
    @DisplayName("T-4: unmap()")
    class Unmap {

        @Test
        @DisplayName("unmap 호출 시 UNMAPPED 상태로 전이된다")
        void shouldChangeToUnmapped() {
            SupplierProperty sp = SupplierFixture.mappedProperty();

            sp.unmap();

            assertThat(sp.status()).isEqualTo(SupplierMappingStatus.UNMAPPED);
        }

        @Test
        @DisplayName("이미 UNMAPPED 상태에서 unmap 재호출은 멱등하게 통과한다")
        void shouldBeIdempotentWhenAlreadyUnmapped() {
            SupplierProperty sp = SupplierFixture.unmappedProperty();

            sp.unmap();

            assertThat(sp.status()).isEqualTo(SupplierMappingStatus.UNMAPPED);
        }
    }

    @Nested
    @DisplayName("T-5: equals/hashCode")
    class Equality {

        @Test
        @DisplayName("같은 id의 SupplierProperty는 동등하다")
        void shouldBeEqualWithSameId() {
            SupplierProperty sp1 = SupplierFixture.reconstitutedProperty(SupplierMappingStatus.MAPPED);
            SupplierProperty sp2 = SupplierFixture.reconstitutedProperty(SupplierMappingStatus.UNMAPPED);

            assertThat(sp1).isEqualTo(sp2);
            assertThat(sp1.hashCode()).isEqualTo(sp2.hashCode());
        }

        @Test
        @DisplayName("id가 null인 두 SupplierProperty는 동등하지 않다")
        void shouldNotBeEqualWhenIdIsNull() {
            SupplierProperty sp1 = SupplierFixture.mappedProperty();
            SupplierProperty sp2 = SupplierFixture.mappedProperty();

            assertThat(sp1).isNotEqualTo(sp2);
        }
    }
}
