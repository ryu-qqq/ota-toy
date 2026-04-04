package com.ryuqq.otatoy.domain.pricing;

import com.ryuqq.otatoy.domain.accommodation.RoomTypeId;
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
            RatePlan ratePlan = RatePlan.forNew(
                    ROOM_TYPE_ID, NAME, SourceType.DIRECT, null,
                    true, false, 3, "체크인 3일 전까지 무료 취소",
                    PaymentPolicy.PREPAY, NOW
            );

            assertNull(ratePlan.id());
            assertEquals(ROOM_TYPE_ID, ratePlan.roomTypeId());
            assertEquals(NAME, ratePlan.name());
            assertEquals(SourceType.DIRECT, ratePlan.sourceType());
            assertTrue(ratePlan.isDirect());
            assertFalse(ratePlan.isSupplier());
            assertTrue(ratePlan.freeCancellation());
            assertEquals(PaymentPolicy.PREPAY, ratePlan.paymentPolicy());
        }

        @Test
        @DisplayName("SUPPLIER 소스 타입으로 정상 생성한다")
        void createSupplierRatePlan() {
            RatePlan ratePlan = RatePlan.forNew(
                    ROOM_TYPE_ID, NAME, SourceType.SUPPLIER, SUPPLIER_ID,
                    false, true, 0, "환불 불가",
                    PaymentPolicy.PREPAY, NOW
            );

            assertNotNull(ratePlan);
            assertEquals(SourceType.SUPPLIER, ratePlan.sourceType());
            assertEquals(SUPPLIER_ID, ratePlan.supplierId());
            assertTrue(ratePlan.isSupplier());
        }

        @Test
        @DisplayName("roomTypeId가 null이면 예외를 던진다")
        void failWhenRoomTypeIdNull() {
            assertThrows(IllegalArgumentException.class, () ->
                    RatePlan.forNew(null, NAME, SourceType.DIRECT, null,
                            false, false, 0, null, PaymentPolicy.PREPAY, NOW)
            );
        }

        @Test
        @DisplayName("paymentPolicy가 null이면 예외를 던진다")
        void failWhenPaymentPolicyNull() {
            assertThrows(IllegalArgumentException.class, () ->
                    RatePlan.forNew(ROOM_TYPE_ID, NAME, SourceType.DIRECT, null,
                            false, false, 0, null, null, NOW)
            );
        }

        @Test
        @DisplayName("SUPPLIER인데 supplierId가 null이면 예외를 던진다")
        void failWhenSupplierWithoutSupplierId() {
            assertThrows(IllegalArgumentException.class, () ->
                    RatePlan.forNew(ROOM_TYPE_ID, NAME, SourceType.SUPPLIER, null,
                            false, false, 0, null, PaymentPolicy.PREPAY, NOW)
            );
        }

        @Test
        @DisplayName("무료 취소와 환불 불가를 동시에 설정하면 예외를 던진다")
        void failWhenFreeCancellationAndNonRefundable() {
            assertThrows(IllegalArgumentException.class, () ->
                    RatePlan.forNew(ROOM_TYPE_ID, NAME, SourceType.DIRECT, null,
                            true, true, 0, null, PaymentPolicy.PREPAY, NOW)
            );
        }

        @Test
        @DisplayName("무료 취소 기한이 음수이면 예외를 던진다")
        void failWhenNegativeDeadlineDays() {
            assertThrows(IllegalArgumentException.class, () ->
                    RatePlan.forNew(ROOM_TYPE_ID, NAME, SourceType.DIRECT, null,
                            true, false, -1, null, PaymentPolicy.PREPAY, NOW)
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

            ratePlan.updatePolicy(false, true, 0, "환불 불가",
                    PaymentPolicy.PAY_AT_PROPERTY, updateTime);

            assertFalse(ratePlan.freeCancellation());
            assertTrue(ratePlan.nonRefundable());
            assertEquals(PaymentPolicy.PAY_AT_PROPERTY, ratePlan.paymentPolicy());
            assertEquals(updateTime, ratePlan.updatedAt());
        }

        @Test
        @DisplayName("무료 취소 + 환불 불가 동시 설정 시 예외를 던진다")
        void failWhenConflictingPolicy() {
            RatePlan ratePlan = createDefaultRatePlan();

            assertThrows(IllegalArgumentException.class, () ->
                    ratePlan.updatePolicy(true, true, 0, null,
                            PaymentPolicy.PREPAY, NOW)
            );
        }

        @Test
        @DisplayName("음수 취소 기한 시 예외를 던진다")
        void failWhenNegativeDeadline() {
            RatePlan ratePlan = createDefaultRatePlan();

            assertThrows(IllegalArgumentException.class, () ->
                    ratePlan.updatePolicy(true, false, -1, null,
                            PaymentPolicy.PREPAY, NOW)
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
            RatePlan ratePlan = RatePlan.reconstitute(
                    id, ROOM_TYPE_ID, NAME, SourceType.DIRECT, null,
                    true, false, 3, "취소 가능", PaymentPolicy.PREPAY, NOW, NOW
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
                    SourceType.DIRECT, null, true, false, 3, null,
                    PaymentPolicy.PREPAY, NOW, NOW);
            RatePlan b = RatePlan.reconstitute(id, ROOM_TYPE_ID, RatePlanName.of("다른 이름"),
                    SourceType.SUPPLIER, SUPPLIER_ID, false, true, 0, null,
                    PaymentPolicy.PAY_AT_PROPERTY, NOW, NOW);

            assertEquals(a, b);
            assertEquals(a.hashCode(), b.hashCode());
        }

        @Test
        @DisplayName("다른 ID의 RatePlan은 동등하지 않다")
        void notEqualWhenDifferentId() {
            RatePlan a = RatePlan.reconstitute(RatePlanId.of(1L), ROOM_TYPE_ID, NAME,
                    SourceType.DIRECT, null, false, false, 0, null,
                    PaymentPolicy.PREPAY, NOW, NOW);
            RatePlan b = RatePlan.reconstitute(RatePlanId.of(2L), ROOM_TYPE_ID, NAME,
                    SourceType.DIRECT, null, false, false, 0, null,
                    PaymentPolicy.PREPAY, NOW, NOW);

            assertNotEquals(a, b);
        }
    }

    private RatePlan createDefaultRatePlan() {
        return RatePlan.forNew(ROOM_TYPE_ID, NAME, SourceType.DIRECT, null,
                true, false, 3, "체크인 3일 전까지 무료 취소",
                PaymentPolicy.PREPAY, NOW);
    }
}
