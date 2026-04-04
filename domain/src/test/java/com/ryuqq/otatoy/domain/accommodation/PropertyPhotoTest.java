package com.ryuqq.otatoy.domain.accommodation;

import com.ryuqq.otatoy.domain.common.vo.CdnUrl;
import com.ryuqq.otatoy.domain.common.vo.OriginUrl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PropertyPhotoTest {

    private static final PropertyId PROPERTY_ID = PropertyId.of(1L);

    @Nested
    @DisplayName("생성 검증")
    class Creation {

        @Test
        @DisplayName("PropertyPhoto 정상 생성")
        void shouldCreatePropertyPhotoSuccessfully() {
            // when
            PropertyPhoto photo = PropertyPhoto.forNew(
                    PROPERTY_ID, PhotoType.EXTERIOR,
                    OriginUrl.of("https://example.com/photo.jpg"),
                    CdnUrl.of("https://cdn.example.com/photo.jpg"), 1
            );

            // then
            assertThat(photo).isNotNull();
            assertThat(photo.id()).isNotNull();
            assertThat(photo.id().isNew()).isTrue();
            assertThat(photo.originUrl()).isEqualTo(OriginUrl.of("https://example.com/photo.jpg"));
            assertThat(photo.photoType()).isEqualTo(PhotoType.EXTERIOR);
        }

        @Test
        @DisplayName("originUrl이 blank이면 생성 실패 (회귀 방지)")
        void shouldFailWhenOriginUrlIsBlank() {
            assertThatThrownBy(() -> OriginUrl.of(""))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("원본 URL은 필수");
        }

        @Test
        @DisplayName("originUrl이 null이면 생성 실패")
        void shouldFailWhenOriginUrlIsNull() {
            assertThatThrownBy(() -> OriginUrl.of(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("원본 URL은 필수");
        }
    }
}
