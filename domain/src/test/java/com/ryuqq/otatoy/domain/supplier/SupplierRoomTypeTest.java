package com.ryuqq.otatoy.domain.supplier;

import com.ryuqq.otatoy.domain.accommodation.RoomTypeId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SupplierRoomTypeTest {

    @Nested
    @DisplayName("T-1: 생성 검증 -- forNew()")
    class Creation {

        @Test
        @DisplayName("정상 생성 시 MAPPED 상태이고 id.value()는 null이다")
        void shouldCreateWithMappedStatusAndNullId() {
            SupplierRoomType srt = SupplierFixture.mappedRoomType();

            assertThat(srt.id().value()).isNull();
            assertThat(srt.status()).isEqualTo(SupplierPropertyStatus.MAPPED);
            assertThat(srt.supplierPropertyId()).isEqualTo(SupplierPropertyId.of(1L));
            assertThat(srt.roomTypeId()).isEqualTo(RoomTypeId.of(200L));
            assertThat(srt.supplierRoomCode()).isEqualTo("EXT-ROOM-001");
            assertThat(srt.lastSyncedAt()).isNull();
        }

        @Test
        @DisplayName("supplierRoomCode가 null이면 생성 실패")
        void shouldFailWhenCodeIsNull() {
            assertThatThrownBy(() -> SupplierRoomType.forNew(
                    SupplierPropertyId.of(1L), RoomTypeId.of(200L), null
            )).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("공급자 객실 코드");
        }

        @Test
        @DisplayName("supplierRoomCode가 blank이면 생성 실패")
        void shouldFailWhenCodeIsBlank() {
            assertThatThrownBy(() -> SupplierRoomType.forNew(
                    SupplierPropertyId.of(1L), RoomTypeId.of(200L), "  "
            )).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("공급자 객실 코드");
        }
    }

    @Nested
    @DisplayName("T-2: reconstitute 검증")
    class Reconstitution {

        @Test
        @DisplayName("reconstitute는 모든 필드를 그대로 복원한다")
        void shouldReconstituteAllFields() {
            SupplierRoomType srt = SupplierFixture.reconstitutedRoomType(SupplierPropertyStatus.MAPPED);

            assertThat(srt.id()).isEqualTo(SupplierRoomTypeId.of(1L));
            assertThat(srt.status()).isEqualTo(SupplierPropertyStatus.MAPPED);
            assertThat(srt.lastSyncedAt()).isEqualTo(SupplierFixture.DEFAULT_NOW);
        }
    }

    @Nested
    @DisplayName("T-3: synced()")
    class Synced {

        @Test
        @DisplayName("synced 호출 시 lastSyncedAt이 갱신된다")
        void shouldUpdateLastSyncedAt() {
            SupplierRoomType srt = SupplierFixture.mappedRoomType();
            Instant syncedAt = Instant.parse("2026-04-04T12:00:00Z");

            srt.synced(syncedAt);

            assertThat(srt.lastSyncedAt()).isEqualTo(syncedAt);
        }
    }

    @Nested
    @DisplayName("T-4: unmap()")
    class Unmap {

        @Test
        @DisplayName("unmap 호출 시 UNMAPPED 상태로 전이된다")
        void shouldChangeToUnmapped() {
            SupplierRoomType srt = SupplierFixture.mappedRoomType();

            srt.unmap();

            assertThat(srt.status()).isEqualTo(SupplierPropertyStatus.UNMAPPED);
        }
    }

    @Nested
    @DisplayName("T-5: equals/hashCode")
    class Equality {

        @Test
        @DisplayName("같은 id의 SupplierRoomType은 동등하다")
        void shouldBeEqualWithSameId() {
            SupplierRoomType srt1 = SupplierFixture.reconstitutedRoomType(SupplierPropertyStatus.MAPPED);
            SupplierRoomType srt2 = SupplierFixture.reconstitutedRoomType(SupplierPropertyStatus.UNMAPPED);

            assertThat(srt1).isEqualTo(srt2);
            assertThat(srt1.hashCode()).isEqualTo(srt2.hashCode());
        }

        @Test
        @DisplayName("id가 null인 두 SupplierRoomType은 동등하지 않다")
        void shouldNotBeEqualWhenIdIsNull() {
            SupplierRoomType srt1 = SupplierFixture.mappedRoomType();
            SupplierRoomType srt2 = SupplierFixture.mappedRoomType();

            assertThat(srt1).isNotEqualTo(srt2);
        }
    }
}
