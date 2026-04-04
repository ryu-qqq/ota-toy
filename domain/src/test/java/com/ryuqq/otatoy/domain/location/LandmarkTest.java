package com.ryuqq.otatoy.domain.location;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LandmarkTest {

    @Nested
    @DisplayName("forNew() 생성 검증")
    class ForNew {

        @Test
        @DisplayName("Landmark 정상 생성")
        void shouldCreateLandmarkSuccessfully() {
            // when
            Landmark landmark = Landmark.forNew(
                    LandmarkName.of("서울역"),
                    LandmarkType.STATION,
                    37.5547, 126.9707
            );

            // then
            assertThat(landmark).isNotNull();
            assertThat(landmark.id().isNew()).isTrue();
            assertThat(landmark.name()).isEqualTo(LandmarkName.of("서울역"));
            assertThat(landmark.landmarkType()).isEqualTo(LandmarkType.STATION);
            assertThat(landmark.latitude()).isEqualTo(37.5547);
            assertThat(landmark.longitude()).isEqualTo(126.9707);
        }

        @Test
        @DisplayName("name이 null이면 생성 실패")
        void shouldFailWhenNameIsNull() {
            assertThatThrownBy(() -> Landmark.forNew(
                    null, LandmarkType.STATION, 37.5, 127.0
            ))
                    .isInstanceOf(LocationException.class)
                    .hasMessageContaining("랜드마크명");
        }

        @Test
        @DisplayName("landmarkType이 null이면 생성 실패")
        void shouldFailWhenTypeIsNull() {
            assertThatThrownBy(() -> Landmark.forNew(
                    LandmarkName.of("서울역"), null, 37.5, 127.0
            ))
                    .isInstanceOf(LocationException.class)
                    .hasMessageContaining("랜드마크 유형");
        }

        @Test
        @DisplayName("위도가 -91이면 생성 실패")
        void shouldFailWhenLatitudeIsBelowMinus90() {
            assertThatThrownBy(() -> Landmark.forNew(
                    LandmarkName.of("테스트"), LandmarkType.TOURIST, -91, 127.0
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("위도");
        }

        @Test
        @DisplayName("위도가 91이면 생성 실패")
        void shouldFailWhenLatitudeIsAbove90() {
            assertThatThrownBy(() -> Landmark.forNew(
                    LandmarkName.of("테스트"), LandmarkType.TOURIST, 91, 127.0
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("위도");
        }

        @Test
        @DisplayName("경도가 -181이면 생성 실패")
        void shouldFailWhenLongitudeIsBelowMinus180() {
            assertThatThrownBy(() -> Landmark.forNew(
                    LandmarkName.of("테스트"), LandmarkType.TOURIST, 37.5, -181
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("경도");
        }

        @Test
        @DisplayName("경도가 181이면 생성 실패")
        void shouldFailWhenLongitudeIsAbove180() {
            assertThatThrownBy(() -> Landmark.forNew(
                    LandmarkName.of("테스트"), LandmarkType.TOURIST, 37.5, 181
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("경도");
        }

        @Test
        @DisplayName("경계값 -- 위도 90, 경도 180은 성공")
        void shouldSucceedAtBoundaryValues() {
            Landmark landmark = Landmark.forNew(
                    LandmarkName.of("북극"), LandmarkType.TOURIST, 90, 180
            );

            assertThat(landmark.latitude()).isEqualTo(90);
            assertThat(landmark.longitude()).isEqualTo(180);
        }

        @Test
        @DisplayName("경계값 -- 위도 -90, 경도 -180은 성공")
        void shouldSucceedAtNegativeBoundaryValues() {
            Landmark landmark = Landmark.forNew(
                    LandmarkName.of("남극"), LandmarkType.TOURIST, -90, -180
            );

            assertThat(landmark.latitude()).isEqualTo(-90);
            assertThat(landmark.longitude()).isEqualTo(-180);
        }
    }

    @Nested
    @DisplayName("reconstitute() 복원 검증")
    class Reconstitute {

        @Test
        @DisplayName("모든 필드가 올바르게 복원된다")
        void shouldReconstituteWithAllFields() {
            // given
            LandmarkId id = LandmarkId.of(42L);
            LandmarkName name = LandmarkName.of("경복궁");
            LandmarkType type = LandmarkType.TOURIST;
            double lat = 37.5796;
            double lng = 126.9770;

            // when
            Landmark landmark = Landmark.reconstitute(id, name, type, lat, lng);

            // then
            assertThat(landmark.id()).isEqualTo(id);
            assertThat(landmark.id().isNew()).isFalse();
            assertThat(landmark.name()).isEqualTo(name);
            assertThat(landmark.landmarkType()).isEqualTo(type);
            assertThat(landmark.latitude()).isEqualTo(lat);
            assertThat(landmark.longitude()).isEqualTo(lng);
        }
    }

    @Nested
    @DisplayName("equals/hashCode 검증")
    class Equality {

        @Test
        @DisplayName("동일 ID의 Landmark는 equals true")
        void shouldBeEqualWithSameId() {
            Landmark a = Landmark.reconstitute(
                    LandmarkId.of(1L), LandmarkName.of("A"), LandmarkType.STATION, 37.0, 127.0
            );
            Landmark b = Landmark.reconstitute(
                    LandmarkId.of(1L), LandmarkName.of("B"), LandmarkType.TOURIST, 38.0, 128.0
            );

            assertThat(a).isEqualTo(b);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }

        @Test
        @DisplayName("다른 ID의 Landmark는 equals false")
        void shouldNotBeEqualWithDifferentId() {
            Landmark a = Landmark.reconstitute(
                    LandmarkId.of(1L), LandmarkName.of("A"), LandmarkType.STATION, 37.0, 127.0
            );
            Landmark b = Landmark.reconstitute(
                    LandmarkId.of(2L), LandmarkName.of("A"), LandmarkType.STATION, 37.0, 127.0
            );

            assertThat(a).isNotEqualTo(b);
        }

        @Test
        @DisplayName("forNew()로 만든 두 객체는 같은 LandmarkId(null)을 가지므로 equals true -- DB 채번 전까지 구분 불가")
        void forNewObjectsShouldBeEqualBeforePersistence() {
            Landmark a = Landmark.forNew(LandmarkName.of("A"), LandmarkType.STATION, 37.0, 127.0);
            Landmark b = Landmark.forNew(LandmarkName.of("B"), LandmarkType.TOURIST, 38.0, 128.0);

            // LandmarkId(null) == LandmarkId(null) 이므로 equals true
            // 이것은 ID 기반 equals의 알려진 제약. DB 채번 후 구분된다.
            assertThat(a).isEqualTo(b);
        }
    }
}
