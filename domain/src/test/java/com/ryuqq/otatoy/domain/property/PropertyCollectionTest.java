package com.ryuqq.otatoy.domain.property;

import com.ryuqq.otatoy.domain.accommodation.AmenityName;
import com.ryuqq.otatoy.domain.accommodation.AmenityType;
import com.ryuqq.otatoy.domain.accommodation.PhotoType;
import com.ryuqq.otatoy.domain.common.vo.CdnUrl;
import com.ryuqq.otatoy.domain.common.vo.DeletionStatus;
import com.ryuqq.otatoy.domain.common.vo.Money;
import com.ryuqq.otatoy.domain.common.vo.OriginUrl;
import com.ryuqq.otatoy.domain.propertytype.PropertyTypeAttributeId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * PropertyAmenities, PropertyPhotos, PropertyAttributeValues 일급 컬렉션
 * 및 Diff 객체 테스트.
 */
class PropertyCollectionTest {

    private static final PropertyId PROPERTY_ID = PropertyId.of(1L);
    private static final Instant NOW = Instant.parse("2026-04-04T00:00:00Z");
    private static final Instant LATER = NOW.plusSeconds(60);

    // ========== PropertyAmenities ==========

    @Nested
    @DisplayName("PropertyAmenities")
    class PropertyAmenitiesTest {

        @Test
        @DisplayName("빈 목록으로 생성 가능")
        void shouldCreateEmpty() {
            PropertyAmenities amenities = PropertyAmenities.forNew(List.of());
            assertThat(amenities.isEmpty()).isTrue();
            assertThat(amenities.size()).isEqualTo(0);
        }

        @Test
        @DisplayName("null 목록으로 생성하면 빈 컬렉션")
        void shouldCreateEmptyFromNull() {
            PropertyAmenities amenities = PropertyAmenities.forNew(null);
            assertThat(amenities.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("정렬 순서가 중복이면 생성 실패")
        void shouldFailWhenDuplicateSortOrder() {
            PropertyAmenity a1 = PropertyAmenity.forNew(PROPERTY_ID, AmenityType.WIFI,
                    AmenityName.of("와이파이"), Money.of(0), 1, NOW);
            PropertyAmenity a2 = PropertyAmenity.forNew(PROPERTY_ID, AmenityType.POOL,
                    AmenityName.of("수영장"), Money.of(5000), 1, NOW);

            assertThatThrownBy(() -> PropertyAmenities.forNew(List.of(a1, a2)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("정렬 순서가 중복");
        }

        @Test
        @DisplayName("정렬 순서가 다르면 생성 성공")
        void shouldSucceedWithUniqueSortOrder() {
            PropertyAmenity a1 = PropertyAmenity.forNew(PROPERTY_ID, AmenityType.WIFI,
                    AmenityName.of("와이파이"), Money.of(0), 1, NOW);
            PropertyAmenity a2 = PropertyAmenity.forNew(PROPERTY_ID, AmenityType.POOL,
                    AmenityName.of("수영장"), Money.of(5000), 2, NOW);

            PropertyAmenities amenities = PropertyAmenities.forNew(List.of(a1, a2));
            assertThat(amenities.size()).isEqualTo(2);
        }

        @Test
        @DisplayName("update() -- 빈 컬렉션끼리 비교하면 변경 없음")
        void shouldHaveNoChangesWhenBothEmpty() {
            PropertyAmenities existing = PropertyAmenities.forNew(List.of());
            PropertyAmenities newOnes = PropertyAmenities.forNew(List.of());

            PropertyAmenityDiff diff = existing.update(newOnes);
            assertThat(diff.hasNoChanges()).isTrue();
        }

        @Test
        @DisplayName("update() -- 새 항목 추가")
        void shouldDetectAdded() {
            PropertyAmenities existing = PropertyAmenities.forNew(List.of());
            PropertyAmenity newAmenity = PropertyAmenity.forNew(PROPERTY_ID, AmenityType.WIFI,
                    AmenityName.of("와이파이"), Money.of(0), 1, LATER);
            PropertyAmenities newOnes = PropertyAmenities.forNew(List.of(newAmenity));

            PropertyAmenityDiff diff = existing.update(newOnes);
            assertThat(diff.added()).hasSize(1);
            assertThat(diff.removed()).isEmpty();
            assertThat(diff.retained()).isEmpty();
            assertThat(diff.hasNoChanges()).isFalse();
        }

        @Test
        @DisplayName("update() -- 기존 항목 삭제")
        void shouldDetectRemoved() {
            PropertyAmenity existingAmenity = PropertyAmenity.forNew(PROPERTY_ID, AmenityType.WIFI,
                    AmenityName.of("와이파이"), Money.of(0), 1, NOW);
            PropertyAmenities existing = PropertyAmenities.forNew(List.of(existingAmenity));
            PropertyAmenities newOnes = PropertyAmenities.forNew(List.of());

            PropertyAmenityDiff diff = existing.update(newOnes);
            assertThat(diff.added()).isEmpty();
            assertThat(diff.removed()).hasSize(1);
            assertThat(diff.removed().getFirst().isDeleted()).isTrue();
            assertThat(diff.retained()).isEmpty();
        }

        @Test
        @DisplayName("update() -- 동일 항목 유지 (amenityKey 기준)")
        void shouldDetectRetained() {
            PropertyAmenity existingAmenity = PropertyAmenity.forNew(PROPERTY_ID, AmenityType.WIFI,
                    AmenityName.of("와이파이"), Money.of(0), 1, NOW);
            PropertyAmenities existing = PropertyAmenities.forNew(List.of(existingAmenity));

            PropertyAmenity newAmenity = PropertyAmenity.forNew(PROPERTY_ID, AmenityType.WIFI,
                    AmenityName.of("와이파이"), Money.of(0), 1, LATER);
            PropertyAmenities newOnes = PropertyAmenities.forNew(List.of(newAmenity));

            PropertyAmenityDiff diff = existing.update(newOnes);
            assertThat(diff.added()).isEmpty();
            assertThat(diff.removed()).isEmpty();
            assertThat(diff.retained()).hasSize(1);
            assertThat(diff.hasNoChanges()).isTrue();
        }

        @Test
        @DisplayName("update() -- 추가+삭제+유지 혼합")
        void shouldDetectMixedChanges() {
            PropertyAmenity wifi = PropertyAmenity.forNew(PROPERTY_ID, AmenityType.WIFI,
                    AmenityName.of("와이파이"), Money.of(0), 1, NOW);
            PropertyAmenity pool = PropertyAmenity.forNew(PROPERTY_ID, AmenityType.POOL,
                    AmenityName.of("수영장"), Money.of(5000), 2, NOW);
            PropertyAmenities existing = PropertyAmenities.forNew(List.of(wifi, pool));

            // 와이파이 유지, 수영장 삭제, 주차장 추가
            PropertyAmenity newWifi = PropertyAmenity.forNew(PROPERTY_ID, AmenityType.WIFI,
                    AmenityName.of("와이파이"), Money.of(0), 1, LATER);
            PropertyAmenity parking = PropertyAmenity.forNew(PROPERTY_ID, AmenityType.PARKING,
                    AmenityName.of("주차장"), Money.of(0), 2, LATER);
            PropertyAmenities newOnes = PropertyAmenities.forNew(List.of(newWifi, parking));

            PropertyAmenityDiff diff = existing.update(newOnes);
            assertThat(diff.added()).hasSize(1);     // 주차장
            assertThat(diff.removed()).hasSize(1);   // 수영장
            assertThat(diff.retained()).hasSize(1);  // 와이파이
            assertThat(diff.hasNoChanges()).isFalse();
        }

        @Test
        @DisplayName("allPersistTargets()는 added + removed + retained를 모두 포함한다")
        void shouldReturnAllPersistTargets() {
            PropertyAmenity wifi = PropertyAmenity.forNew(PROPERTY_ID, AmenityType.WIFI,
                    AmenityName.of("와이파이"), Money.of(0), 1, NOW);
            PropertyAmenities existing = PropertyAmenities.forNew(List.of(wifi));

            PropertyAmenity parking = PropertyAmenity.forNew(PROPERTY_ID, AmenityType.PARKING,
                    AmenityName.of("주차장"), Money.of(0), 1, LATER);
            PropertyAmenities newOnes = PropertyAmenities.forNew(List.of(parking));

            PropertyAmenityDiff diff = existing.update(newOnes);
            // added=주차장, removed=와이파이, retained=없음
            assertThat(diff.allPersistTargets()).hasSize(2);
        }
    }

    // ========== PropertyPhotos ==========

    @Nested
    @DisplayName("PropertyPhotos")
    class PropertyPhotosTest {

        @Test
        @DisplayName("빈 목록으로 생성 가능")
        void shouldCreateEmpty() {
            PropertyPhotos photos = PropertyPhotos.forNew(List.of());
            assertThat(photos.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("null 목록으로 생성하면 빈 컬렉션")
        void shouldCreateEmptyFromNull() {
            PropertyPhotos photos = PropertyPhotos.forNew(null);
            assertThat(photos.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("정렬 순서가 중복이면 생성 실패")
        void shouldFailWhenDuplicateSortOrder() {
            PropertyPhoto p1 = PropertyPhoto.forNew(PROPERTY_ID, PhotoType.EXTERIOR,
                    OriginUrl.of("https://a.com/1.jpg"), CdnUrl.of("https://cdn.a.com/1.jpg"), 1, NOW);
            PropertyPhoto p2 = PropertyPhoto.forNew(PROPERTY_ID, PhotoType.ROOM,
                    OriginUrl.of("https://a.com/2.jpg"), CdnUrl.of("https://cdn.a.com/2.jpg"), 1, NOW);

            assertThatThrownBy(() -> PropertyPhotos.forNew(List.of(p1, p2)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("정렬 순서가 중복");
        }

        @Test
        @DisplayName("update() -- 새 사진 추가")
        void shouldDetectAdded() {
            PropertyPhotos existing = PropertyPhotos.forNew(List.of());
            PropertyPhoto newPhoto = PropertyPhoto.forNew(PROPERTY_ID, PhotoType.EXTERIOR,
                    OriginUrl.of("https://a.com/1.jpg"), null, 1, LATER);
            PropertyPhotos newOnes = PropertyPhotos.forNew(List.of(newPhoto));

            PropertyPhotoDiff diff = existing.update(newOnes);
            assertThat(diff.added()).hasSize(1);
            assertThat(diff.removed()).isEmpty();
            assertThat(diff.hasNoChanges()).isFalse();
        }

        @Test
        @DisplayName("update() -- 동일 사진 유지 (photoKey 기준: originUrl + photoType)")
        void shouldDetectRetained() {
            PropertyPhoto existingPhoto = PropertyPhoto.forNew(PROPERTY_ID, PhotoType.EXTERIOR,
                    OriginUrl.of("https://a.com/1.jpg"), CdnUrl.of("https://cdn.a.com/1.jpg"), 1, NOW);
            PropertyPhotos existing = PropertyPhotos.forNew(List.of(existingPhoto));

            PropertyPhoto newPhoto = PropertyPhoto.forNew(PROPERTY_ID, PhotoType.EXTERIOR,
                    OriginUrl.of("https://a.com/1.jpg"), CdnUrl.of("https://cdn.a.com/1.jpg"), 1, LATER);
            PropertyPhotos newOnes = PropertyPhotos.forNew(List.of(newPhoto));

            PropertyPhotoDiff diff = existing.update(newOnes);
            assertThat(diff.retained()).hasSize(1);
            assertThat(diff.hasNoChanges()).isTrue();
        }
    }

    // ========== PropertyAttributeValues ==========

    @Nested
    @DisplayName("PropertyAttributeValues")
    class PropertyAttributeValuesTest {

        @Test
        @DisplayName("빈 목록으로 생성 가능")
        void shouldCreateEmpty() {
            PropertyAttributeValues values = PropertyAttributeValues.forNew(List.of());
            assertThat(values.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("null 목록으로 생성하면 빈 컬렉션")
        void shouldCreateEmptyFromNull() {
            PropertyAttributeValues values = PropertyAttributeValues.forNew(null);
            assertThat(values.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("동일 속성 ID가 중복이면 생성 실패")
        void shouldFailWhenDuplicateAttributeId() {
            PropertyAttributeValue av1 = PropertyAttributeValue.forNew(
                    PROPERTY_ID, PropertyTypeAttributeId.of(10L), "값1", NOW);
            PropertyAttributeValue av2 = PropertyAttributeValue.forNew(
                    PROPERTY_ID, PropertyTypeAttributeId.of(10L), "값2", NOW);

            assertThatThrownBy(() -> PropertyAttributeValues.forNew(List.of(av1, av2)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("속성 ID가 중복");
        }

        @Test
        @DisplayName("update() -- 새 속성값 추가")
        void shouldDetectAdded() {
            PropertyAttributeValues existing = PropertyAttributeValues.forNew(List.of());
            PropertyAttributeValue newVal = PropertyAttributeValue.forNew(
                    PROPERTY_ID, PropertyTypeAttributeId.of(10L), "5성급", LATER);
            PropertyAttributeValues newOnes = PropertyAttributeValues.forNew(List.of(newVal));

            PropertyAttributeValueDiff diff = existing.update(newOnes);
            assertThat(diff.added()).hasSize(1);
            assertThat(diff.hasNoChanges()).isFalse();
        }

        @Test
        @DisplayName("update() -- 기존 속성값 삭제")
        void shouldDetectRemoved() {
            PropertyAttributeValue existingVal = PropertyAttributeValue.forNew(
                    PROPERTY_ID, PropertyTypeAttributeId.of(10L), "5성급", NOW);
            PropertyAttributeValues existing = PropertyAttributeValues.forNew(List.of(existingVal));
            PropertyAttributeValues newOnes = PropertyAttributeValues.forNew(List.of());

            PropertyAttributeValueDiff diff = existing.update(newOnes);
            assertThat(diff.removed()).hasSize(1);
            assertThat(diff.removed().getFirst().isDeleted()).isTrue();
        }

        @Test
        @DisplayName("update() -- 동일 속성 유지 (attributeKey 기준)")
        void shouldDetectRetained() {
            PropertyAttributeValue existingVal = PropertyAttributeValue.forNew(
                    PROPERTY_ID, PropertyTypeAttributeId.of(10L), "5성급", NOW);
            PropertyAttributeValues existing = PropertyAttributeValues.forNew(List.of(existingVal));

            PropertyAttributeValue newVal = PropertyAttributeValue.forNew(
                    PROPERTY_ID, PropertyTypeAttributeId.of(10L), "5성급 업데이트", LATER);
            PropertyAttributeValues newOnes = PropertyAttributeValues.forNew(List.of(newVal));

            PropertyAttributeValueDiff diff = existing.update(newOnes);
            assertThat(diff.retained()).hasSize(1);
            assertThat(diff.hasNoChanges()).isTrue();
        }

        @Test
        @DisplayName("allPersistTargets()는 모든 항목을 포함한다")
        void shouldReturnAllPersistTargets() {
            PropertyAttributeValue existingVal = PropertyAttributeValue.forNew(
                    PROPERTY_ID, PropertyTypeAttributeId.of(10L), "기존값", NOW);
            PropertyAttributeValues existing = PropertyAttributeValues.forNew(List.of(existingVal));

            PropertyAttributeValue newVal = PropertyAttributeValue.forNew(
                    PROPERTY_ID, PropertyTypeAttributeId.of(20L), "새값", LATER);
            PropertyAttributeValues newOnes = PropertyAttributeValues.forNew(List.of(newVal));

            PropertyAttributeValueDiff diff = existing.update(newOnes);
            // added=새값, removed=기존값, retained=없음
            assertThat(diff.allPersistTargets()).hasSize(2);
        }
    }
}
