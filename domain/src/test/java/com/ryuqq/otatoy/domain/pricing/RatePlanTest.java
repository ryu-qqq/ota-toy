package com.ryuqq.otatoy.domain.pricing;

import com.ryuqq.otatoy.domain.roomtype.RoomTypeId;
import com.ryuqq.otatoy.domain.supplier.SupplierId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class RatePlanTest {

    private static final Instant NOW = Instant.parse("2026-04-04T00:00:00Z");
    private static final RoomTypeId ROOM_TYPE_ID = RoomTypeId.of(1L);
    private static final RatePlanName NAME = RatePlanName.of("기본 요금제");
    private static final SupplierId SUPPLIER_ID = SupplierId.of(10L);

    @Nested
    @DisplayName("forNew() 팩토리 메서드")
    class ForNewTest {

        @Test
        @DisplayName("DIRECT 소스 타입으로 정상 생성한다")
        void createDirectRatePlan() {
            CancellationPolicy cancellationPolicy = CancellationPolicy.of(true, false, 3, "체크인 3일 전까지 무료 취소");
            RatePlan ratePlan = RatePlan.forNew(
                    ROOM_TYPE_ID, NAME, SourceType.DIRECT, null,
                    cancellationPolicy, PaymentPolicy.PREPAY, NOW
            );

            assertNull(ratePlan.id());
            assertEquals(ROOM_TYPE_ID, ratePlan.roomTypeId());
            assertEquals(NAME, ratePlan.name());
            assertEquals(SourceType.DIRECT, ratePlan.sourceType());
            assertTrue(ratePlan.isDirect());
            assertFalse(ratePlan.isSupplier());
            assertTrue(ratePlan.cancellationPolicy().freeCancellation());
            assertEquals(PaymentPolicy.PREPAY, ratePlan.paymentPolicy());
        }

        @Test
        @DisplayName("SUPPLIER 소스 타입으로 정상 생성한다")
        void createSupplierRatePlan() {
            CancellationPolicy cancellationPolicy = CancellationPolicy.of(false, true, 0, "환불 불가");
            RatePlan ratePlan = RatePlan.forNew(
                    ROOM_TYPE_ID, NAME, SourceType.SUPPLIER, SUPPLIER_ID,
                    cancellationPolicy, PaymentPolicy.PREPAY, NOW
            );

            assertNotNull(ratePlan);
            assertEquals(SourceType.SUPPLIER, ratePlan.sourceType());
            assertEquals(SUPPLIER_ID, ratePlan.supplierId());
            assertTrue(ratePlan.isSupplier());
        }

        @Test
        @DisplayName("roomTypeId가 null이면 예외를 던진다")
        void failWhenRoomTypeIdNull() {
            CancellationPolicy cancellationPolicy = CancellationPolicy.of(false, false, 0, null);
            assertThrows(IllegalArgumentException.class, () ->
                    RatePlan.forNew(null, NAME, SourceType.DIRECT, null,
                            cancellationPolicy, PaymentPolicy.PREPAY, NOW)
            );
        }

        @Test
        @DisplayName("paymentPolicy가 null이면 예외를 던진다")
        void failWhenPaymentPolicyNull() {
            CancellationPolicy cancellationPolicy = CancellationPolicy.of(false, false, 0, null);
            assertThrows(IllegalArgumentException.class, () ->
                    RatePlan.forNew(ROOM_TYPE_ID, NAME, SourceType.DIRECT, null,
                            cancellationPolicy, null, NOW)
            );
        }

        @Test
        @DisplayName("SUPPLIER인데 supplierId가 null이면 예외를 던진다")
        void failWhenSupplierWithoutSupplierId() {
            CancellationPolicy cancellationPolicy = CancellationPolicy.of(false, false, 0, null);
            assertThrows(IllegalArgumentException.class, () ->
                    RatePlan.forNew(ROOM_TYPE_ID, NAME, SourceType.SUPPLIER, null,
                            cancellationPolicy, PaymentPolicy.PREPAY, NOW)
            );
        }

        @Test
        @DisplayName("무료 취소와 환불 불가를 동시에 설정하면 예외를 던진다")
        void failWhenFreeCancellationAndNonRefundable() {
            assertThrows(IllegalArgumentException.class, () ->
                    CancellationPolicy.of(true, true, 0, null)
            );
        }

        @Test
        @DisplayName("무료 취소 기한이 음수이면 예외를 던진다")
        void failWhenNegativeDeadlineDays() {
            assertThrows(IllegalArgumentException.class, () ->
                    CancellationPolicy.of(true, false, -1, null)
            );
        }
    }

    @Nested
    @DisplayName("updatePolicy() 비즈니스 메서드")
    class UpdatePolicyTest {

        @Test
        @DisplayName("정상적으로 정책을 변경한다")
        void updatePolicySuccess() {
            RatePlan ratePlan = createDefaultRatePlan();
            Instant updateTime = Instant.parse("2026-04-05T00:00:00Z");

            CancellationPolicy newCancellation = CancellationPolicy.of(false, true, 0, "환불 불가");
            ratePlan.updatePolicy(newCancellation, PaymentPolicy.PAY_AT_PROPERTY, updateTime);

            assertFalse(ratePlan.cancellationPolicy().freeCancellation());
            assertTrue(ratePlan.cancellationPolicy().nonRefundable());
            assertEquals(PaymentPolicy.PAY_AT_PROPERTY, ratePlan.paymentPolicy());
            assertEquals(updateTime, ratePlan.updatedAt());
        }

        @Test
        @DisplayName("무료 취소 + 환불 불가 동시 설정 시 예외를 던진다")
        void failWhenConflictingPolicy() {
            RatePlan ratePlan = createDefaultRatePlan();

            assertThrows(IllegalArgumentException.class, () ->
                    CancellationPolicy.of(true, true, 0, null)
            );
        }

        @Test
        @DisplayName("음수 취소 기한 시 예외를 던진다")
        void failWhenNegativeDeadline() {
            RatePlan ratePlan = createDefaultRatePlan();

            assertThrows(IllegalArgumentException.class, () ->
                    CancellationPolicy.of(true, false, -1, null)
            );
        }
    }

    @Nested
    @DisplayName("reconstitute() 복원")
    class ReconstituteTest {

        @Test
        @DisplayName("검증 없이 모든 필드를 복원한다")
        void reconstituteSuccess() {
            RatePlanId id = RatePlanId.of(100L);
            CancellationPolicy cancellationPolicy = CancellationPolicy.of(true, false, 3, "취소 가능");
            RatePlan ratePlan = RatePlan.reconstitute(
                    id, ROOM_TYPE_ID, NAME, SourceType.DIRECT, null,
                    cancellationPolicy, PaymentPolicy.PREPAY, NOW, NOW
            );

            assertEquals(id, ratePlan.id());
            assertEquals(ROOM_TYPE_ID, ratePlan.roomTypeId());
        }
    }

    @Nested
    @DisplayName("equals/hashCode")
    class EqualsHashCodeTest {

        @Test
        @DisplayName("같은 ID의 RatePlan은 동등하다")
        void equalWhenSameId() {
            RatePlanId id = RatePlanId.of(1L);
            RatePlan a = RatePlan.reconstitute(id, ROOM_TYPE_ID, NAME,
                    SourceType.DIRECT, null,
                    CancellationPolicy.of(true, false, 3, null),
                    PaymentPolicy.PREPAY, NOW, NOW);
            RatePlan b = RatePlan.reconstitute(id, ROOM_TYPE_ID, RatePlanName.of("다른 이름"),
                    SourceType.SUPPLIER, SUPPLIER_ID,
                    CancellationPolicy.of(false, true, 0, null),
                    PaymentPolicy.PAY_AT_PROPERTY, NOW, NOW);

            assertEquals(a, b);
            assertEquals(a.hashCode(), b.hashCode());
        }

        @Test
        @DisplayName("다른 ID의 RatePlan은 동등하지 않다")
        void notEqualWhenDifferentId() {
            CancellationPolicy cancellationPolicy = CancellationPolicy.of(false, false, 0, null);
            RatePlan a = RatePlan.reconstitute(RatePlanId.of(1L), ROOM_TYPE_ID, NAME,
                    SourceType.DIRECT, null, cancellationPolicy,
                    PaymentPolicy.PREPAY, NOW, NOW);
            RatePlan b = RatePlan.reconstitute(RatePlanId.of(2L), ROOM_TYPE_ID, NAME,
                    SourceType.DIRECT, null, cancellationPolicy,
                    PaymentPolicy.PREPAY, NOW, NOW);

            assertNotEquals(a, b);
        }
    }

    private RatePlan createDefaultRatePlan() {
        CancellationPolicy cancellationPolicy = CancellationPolicy.of(true, false, 3, "체크인 3일 전까지 무료 취소");
        return RatePlan.forNew(ROOM_TYPE_ID, NAME, SourceType.DIRECT, null,
                cancellationPolicy, PaymentPolicy.PREPAY, NOW);
    }
}
