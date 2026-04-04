package com.ryuqq.otatoy.domain.location;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PropertyLandmarkTest {

    @Nested
    @DisplayName("forNew() 생성 검증")
    class ForNew {

        @Test
        @DisplayName("PropertyLandmark 정상 생성")
        void shouldCreateSuccessfully() {
            // when
            PropertyLandmark pl = PropertyLandmark.forNew(
                    1L, LandmarkId.of(10L), 1.5, 18
            );

            // then
            assertThat(pl).isNotNull();
            assertThat(pl.id().isNew()).isTrue();
            assertThat(pl.propertyId()).isEqualTo(1L);
            assertThat(pl.landmarkId()).isEqualTo(LandmarkId.of(10L));
            assertThat(pl.distanceKm()).isEqualTo(1.5);
            assertThat(pl.walkingMinutes()).isEqualTo(18);
        }

        @Test
        @DisplayName("propertyId가 0이면 생성 실패")
        void shouldFailWhenPropertyIdIsZero() {
            assertThatThrownBy(() -> PropertyLandmark.forNew(
                    0, LandmarkId.of(1L), 1.0, 10
            ))
                    .isInstanceOf(LocationException.class)
                    .hasMessageContaining("숙소 ID");
        }

        @Test
        @DisplayName("propertyId가 음수이면 생성 실패")
        void shouldFailWhenPropertyIdIsNegative() {
            assertThatThrownBy(() -> PropertyLandmark.forNew(
                    -1, LandmarkId.of(1L), 1.0, 10
            ))
                    .isInstanceOf(LocationException.class)
                    .hasMessageContaining("숙소 ID");
        }

        @Test
        @DisplayName("landmarkId가 null이면 생성 실패")
        void shouldFailWhenLandmarkIdIsNull() {
            assertThatThrownBy(() -> PropertyLandmark.forNew(
                    1L, null, 1.0, 10
            ))
                    .isInstanceOf(LocationException.class)
                    .hasMessageContaining("랜드마크 ID");
        }

        @Test
        @DisplayName("landmarkId value가 null이면 생성 실패")
        void shouldFailWhenLandmarkIdValueIsNull() {
            assertThatThrownBy(() -> PropertyLandmark.forNew(
                    1L, LandmarkId.of(null), 1.0, 10
            ))
                    .isInstanceOf(LocationException.class)
                    .hasMessageContaining("랜드마크 ID");
        }

        @Test
        @DisplayName("distanceKm가 음수이면 생성 실패")
        void shouldFailWhenDistanceIsNegative() {
            assertThatThrownBy(() -> PropertyLandmark.forNew(
                    1L, LandmarkId.of(1L), -0.1, 10
            ))
                    .isInstanceOf(LocationException.class)
                    .hasMessageContaining("거리");
        }

        @Test
        @DisplayName("walkingMinutes가 음수이면 생성 실패")
        void shouldFailWhenWalkingMinutesIsNegative() {
            assertThatThrownBy(() -> PropertyLandmark.forNew(
                    1L, LandmarkId.of(1L), 1.0, -1
            ))
                    .isInstanceOf(LocationException.class)
                    .hasMessageContaining("도보 시간");
        }

        @Test
        @DisplayName("경계값 -- distanceKm=0, walkingMinutes=0은 성공")
        void shouldSucceedWithZeroValues() {
            PropertyLandmark pl = PropertyLandmark.forNew(
                    1L, LandmarkId.of(1L), 0, 0
            );

            assertThat(pl.distanceKm()).isEqualTo(0);
            assertThat(pl.walkingMinutes()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("reconstitute() 복원 검증")
    class Reconstitute {

        @Test
        @DisplayName("모든 필드가 올바르게 복원된다")
        void shouldReconstituteWithAllFields() {
            // when
            PropertyLandmark pl = PropertyLandmark.reconstitute(
                    PropertyLandmarkId.of(99L), 5L, LandmarkId.of(10L), 2.3, 28
            );

            // then
            assertThat(pl.id()).isEqualTo(PropertyLandmarkId.of(99L));
            assertThat(pl.id().isNew()).isFalse();
            assertThat(pl.propertyId()).isEqualTo(5L);
            assertThat(pl.landmarkId()).isEqualTo(LandmarkId.of(10L));
            assertThat(pl.distanceKm()).isEqualTo(2.3);
            assertThat(pl.walkingMinutes()).isEqualTo(28);
        }
    }

    @Nested
    @DisplayName("equals/hashCode 검증")
    class Equality {

        @Test
        @DisplayName("동일 ID의 PropertyLandmark는 equals true")
        void shouldBeEqualWithSameId() {
            PropertyLandmark a = PropertyLandmark.reconstitute(
                    PropertyLandmarkId.of(1L), 1L, LandmarkId.of(1L), 1.0, 10
            );
            PropertyLandmark b = PropertyLandmark.reconstitute(
                    PropertyLandmarkId.of(1L), 2L, LandmarkId.of(2L), 2.0, 20
            );

            assertThat(a).isEqualTo(b);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }

        @Test
        @DisplayName("다른 ID의 PropertyLandmark는 equals false")
        void shouldNotBeEqualWithDifferentId() {
            PropertyLandmark a = PropertyLandmark.reconstitute(
                    PropertyLandmarkId.of(1L), 1L, LandmarkId.of(1L), 1.0, 10
            );
            PropertyLandmark b = PropertyLandmark.reconstitute(
                    PropertyLandmarkId.of(2L), 1L, LandmarkId.of(1L), 1.0, 10
            );

            assertThat(a).isNotEqualTo(b);
        }
    }
}
