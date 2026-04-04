package com.ryuqq.otatoy.domain.location;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Location BC의 VO (LandmarkId, PropertyLandmarkId, LandmarkName, LandmarkType) 테스트.
 */
class LocationVoTest {

    @Nested
    @DisplayName("LandmarkId")
    class LandmarkIdTest {

        @Test
        @DisplayName("of()로 생성하면 value가 올바르다")
        void shouldCreateWithValue() {
            LandmarkId id = LandmarkId.of(42L);
            assertThat(id.value()).isEqualTo(42L);
        }

        @Test
        @DisplayName("null value이면 isNew() true")
        void shouldBeNewWhenNull() {
            LandmarkId id = LandmarkId.of(null);
            assertThat(id.isNew()).isTrue();
        }

        @Test
        @DisplayName("non-null value이면 isNew() false")
        void shouldNotBeNewWhenNonNull() {
            LandmarkId id = LandmarkId.of(1L);
            assertThat(id.isNew()).isFalse();
        }

        @Test
        @DisplayName("동일 value이면 equals true (Record)")
        void shouldBeEqualWithSameValue() {
            assertThat(LandmarkId.of(1L)).isEqualTo(LandmarkId.of(1L));
        }
    }

    @Nested
    @DisplayName("PropertyLandmarkId")
    class PropertyLandmarkIdTest {

        @Test
        @DisplayName("of()로 생성하면 value가 올바르다")
        void shouldCreateWithValue() {
            PropertyLandmarkId id = PropertyLandmarkId.of(99L);
            assertThat(id.value()).isEqualTo(99L);
        }

        @Test
        @DisplayName("null value이면 isNew() true")
        void shouldBeNewWhenNull() {
            PropertyLandmarkId id = PropertyLandmarkId.of(null);
            assertThat(id.isNew()).isTrue();
        }

        @Test
        @DisplayName("non-null value이면 isNew() false")
        void shouldNotBeNewWhenNonNull() {
            PropertyLandmarkId id = PropertyLandmarkId.of(1L);
            assertThat(id.isNew()).isFalse();
        }
    }

    @Nested
    @DisplayName("LandmarkName")
    class LandmarkNameTest {

        @Test
        @DisplayName("정상 생성")
        void shouldCreateSuccessfully() {
            LandmarkName name = LandmarkName.of("서울역");
            assertThat(name.value()).isEqualTo("서울역");
        }

        @Test
        @DisplayName("null이면 생성 실패")
        void shouldFailWhenNull() {
            assertThatThrownBy(() -> LandmarkName.of(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("랜드마크명");
        }

        @Test
        @DisplayName("빈 문자열이면 생성 실패")
        void shouldFailWhenEmpty() {
            assertThatThrownBy(() -> LandmarkName.of(""))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("랜드마크명");
        }

        @Test
        @DisplayName("공백만 있으면 생성 실패")
        void shouldFailWhenBlank() {
            assertThatThrownBy(() -> LandmarkName.of("   "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("랜드마크명");
        }

        @Test
        @DisplayName("동일 value이면 equals true (Record)")
        void shouldBeEqualWithSameValue() {
            assertThat(LandmarkName.of("서울역")).isEqualTo(LandmarkName.of("서울역"));
        }
    }

    @Nested
    @DisplayName("LandmarkType")
    class LandmarkTypeTest {

        @Test
        @DisplayName("모든 타입이 displayName()을 반환한다")
        void shouldHaveDisplayName() {
            for (LandmarkType type : LandmarkType.values()) {
                assertThat(type.displayName()).isNotNull();
                assertThat(type.displayName()).isNotBlank();
            }
        }

        @Test
        @DisplayName("STATION의 displayName은 '역'이다")
        void stationDisplayName() {
            assertThat(LandmarkType.STATION.displayName()).isEqualTo("역");
        }

        @Test
        @DisplayName("TOURIST의 displayName은 '관광지'이다")
        void touristDisplayName() {
            assertThat(LandmarkType.TOURIST.displayName()).isEqualTo("관광지");
        }

        @Test
        @DisplayName("AIRPORT의 displayName은 '공항'이다")
        void airportDisplayName() {
            assertThat(LandmarkType.AIRPORT.displayName()).isEqualTo("공항");
        }
    }
}
