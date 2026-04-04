package com.ryuqq.otatoy.domain.supplier;

import com.ryuqq.otatoy.domain.roomtype.RoomTypeId;
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
            assertThat(srt.status()).isEqualTo(SupplierMappingStatus.MAPPED);
            assertThat(srt.supplierPropertyId()).isEqualTo(SupplierPropertyId.of(1L));
            assertThat(srt.roomTypeId()).isEqualTo(RoomTypeId.of(200L));
            assertThat(srt.supplierRoomCode()).isEqualTo("EXT-ROOM-001");
            assertThat(srt.lastSyncedAt()).isNull();
        }

        @Test
        @DisplayName("supplierRoomCode가 null이면 생성 실패")
        void shouldFailWhenCodeIsNull() {
            assertThatThrownBy(() -> SupplierRoomType.forNew(
                    SupplierPropertyId.of(1L), RoomTypeId.of(200L), null, Instant.parse("2026-04-04T00:00:00Z")
            )).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("공급자 객실 코드");
        }

        @Test
        @DisplayName("supplierRoomCode가 blank이면 생성 실패")
        void shouldFailWhenCodeIsBlank() {
            assertThatThrownBy(() -> SupplierRoomType.forNew(
                    SupplierPropertyId.of(1L), RoomTypeId.of(200L), "  ", Instant.parse("2026-04-04T00:00:00Z")
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
            SupplierRoomType srt = SupplierFixture.reconstitutedRoomType(SupplierMappingStatus.MAPPED);

            assertThat(srt.id()).isEqualTo(SupplierRoomTypeId.of(1L));
            assertThat(srt.status()).isEqualTo(SupplierMappingStatus.MAPPED);
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

        @Test
        @DisplayName("UNMAPPED 상태에서 synced 호출 시 예외가 발생한다")
        void shouldFailWhenSyncedOnUnmapped() {
            SupplierRoomType srt = SupplierFixture.unmappedRoomType();

            assertThatThrownBy(() -> srt.synced(Instant.now()))
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
            SupplierRoomType srt = SupplierFixture.mappedRoomType();

            srt.unmap(Instant.parse("2026-04-04T00:00:00Z"));

            assertThat(srt.status()).isEqualTo(SupplierMappingStatus.UNMAPPED);
        }

        @Test
        @DisplayName("이미 UNMAPPED 상태에서 unmap 재호출은 멱등하게 통과한다")
        void shouldBeIdempotentWhenAlreadyUnmapped() {
            SupplierRoomType srt = SupplierFixture.unmappedRoomType();

            srt.unmap(Instant.parse("2026-04-04T00:00:00Z"));

            assertThat(srt.status()).isEqualTo(SupplierMappingStatus.UNMAPPED);
        }
    }

    @Nested
    @DisplayName("T-5: equals/hashCode")
    class Equality {

        @Test
        @DisplayName("같은 id의 SupplierRoomType은 동등하다")
        void shouldBeEqualWithSameId() {
            SupplierRoomType srt1 = SupplierFixture.reconstitutedRoomType(SupplierMappingStatus.MAPPED);
            SupplierRoomType srt2 = SupplierFixture.reconstitutedRoomType(SupplierMappingStatus.UNMAPPED);

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
