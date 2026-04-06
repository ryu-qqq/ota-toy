package com.ryuqq.otatoy.domain.property;

import com.ryuqq.otatoy.domain.partner.PartnerId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * ExtranetPropertySliceCriteria 생성 검증 테스트.
 */
class ExtranetPropertySliceCriteriaTest {

    @Nested
    @DisplayName("생성 검증")
    class Creation {

        @Test
        @DisplayName("정상 생성")
        void shouldCreateSuccessfully() {
            ExtranetPropertySliceCriteria criteria = new ExtranetPropertySliceCriteria(
                    PartnerId.of(1L), 20, null
            );
            assertThat(criteria.partnerId()).isEqualTo(PartnerId.of(1L));
            assertThat(criteria.size()).isEqualTo(20);
            assertThat(criteria.cursor()).isNull();
        }

        @Test
        @DisplayName("partnerId가 null이면 생성 실패")
        void shouldFailWhenPartnerIdIsNull() {
            assertThatThrownBy(() -> new ExtranetPropertySliceCriteria(null, 20, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("파트너 ID는 필수");
        }

        @Test
        @DisplayName("size가 0이면 생성 실패")
        void shouldFailWhenSizeIsZero() {
            assertThatThrownBy(() -> new ExtranetPropertySliceCriteria(PartnerId.of(1L), 0, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("페이지 크기는 1~100");
        }

        @Test
        @DisplayName("size가 101이면 생성 실패")
        void shouldFailWhenSizeExceeds100() {
            assertThatThrownBy(() -> new ExtranetPropertySliceCriteria(PartnerId.of(1L), 101, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("페이지 크기는 1~100");
        }

        @Test
        @DisplayName("경계값 -- size=1, size=100은 성공")
        void shouldSucceedAtBoundarySize() {
            ExtranetPropertySliceCriteria c1 = new ExtranetPropertySliceCriteria(PartnerId.of(1L), 1, null);
            assertThat(c1.size()).isEqualTo(1);

            ExtranetPropertySliceCriteria c100 = new ExtranetPropertySliceCriteria(PartnerId.of(1L), 100, null);
            assertThat(c100.size()).isEqualTo(100);
        }

        @Test
        @DisplayName("cursor가 null이면 성공 (첫 페이지)")
        void shouldAllowNullCursor() {
            ExtranetPropertySliceCriteria criteria = new ExtranetPropertySliceCriteria(
                    PartnerId.of(1L), 20, null
            );
            assertThat(criteria.cursor()).isNull();
        }

        @Test
        @DisplayName("cursor가 있으면 성공 (다음 페이지)")
        void shouldAllowCursor() {
            ExtranetPropertySliceCriteria criteria = new ExtranetPropertySliceCriteria(
                    PartnerId.of(1L), 20, 100L
            );
            assertThat(criteria.cursor()).isEqualTo(100L);
        }
    }
}
