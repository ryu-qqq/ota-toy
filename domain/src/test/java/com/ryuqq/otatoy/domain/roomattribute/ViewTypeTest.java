package com.ryuqq.otatoy.domain.roomattribute;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ViewTypeTest {

    private static final Instant NOW = Instant.parse("2026-04-04T00:00:00Z");

    @Nested
    @DisplayName("생성 검증")
    class Creation {

        @Test
        @DisplayName("ViewType 정상 생성")
        void shouldCreateViewTypeSuccessfully() {
            // when
            ViewType viewType = ViewType.forNew(ViewTypeCode.of("OCEAN"), ViewTypeName.of("바다 전망"), NOW);

            // then
            assertThat(viewType).isNotNull();
            assertThat(viewType.id().isNew()).isTrue();
            assertThat(viewType.code().value()).isEqualTo("OCEAN");
            assertThat(viewType.name().value()).isEqualTo("바다 전망");
            assertThat(viewType.createdAt()).isEqualTo(NOW);
        }

        @Test
        @DisplayName("reconstitute로 DB 복원 성공")
        void shouldReconstituteViewType() {
            // when
            ViewType viewType = ViewType.reconstitute(
                    ViewTypeId.of(1L), ViewTypeCode.of("MOUNTAIN"), ViewTypeName.of("산 전망"), NOW, NOW
            );

            // then
            assertThat(viewType.id().value()).isEqualTo(1L);
            assertThat(viewType.id().isNew()).isFalse();
        }
    }

    @Nested
    @DisplayName("VO 검증 - ViewTypeCode")
    class ViewTypeCodeValidation {

        @Test
        @DisplayName("ViewTypeCode null이면 생성 실패")
        void shouldFailWhenCodeIsNull() {
            assertThatThrownBy(() -> ViewTypeCode.of(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("전망 유형 코드는 필수입니다");
        }

        @Test
        @DisplayName("ViewTypeCode 빈 값이면 생성 실패")
        void shouldFailWhenCodeIsBlank() {
            assertThatThrownBy(() -> ViewTypeCode.of(""))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("전망 유형 코드는 필수입니다");
        }

        @Test
        @DisplayName("ViewTypeCode 50자 초과이면 생성 실패")
        void shouldFailWhenCodeExceedsMaxLength() {
            String longCode = "A".repeat(51);
            assertThatThrownBy(() -> ViewTypeCode.of(longCode))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("50자 이하여야 합니다");
        }

        @Test
        @DisplayName("ViewTypeCode 50자 경계값 생성 성공")
        void shouldSucceedWhenCodeIsExactlyMaxLength() {
            String code = "A".repeat(50);
            ViewTypeCode viewTypeCode = ViewTypeCode.of(code);
            assertThat(viewTypeCode.value()).hasSize(50);
        }
    }

    @Nested
    @DisplayName("VO 검증 - ViewTypeName")
    class ViewTypeNameValidation {

        @Test
        @DisplayName("ViewTypeName null이면 생성 실패")
        void shouldFailWhenNameIsNull() {
            assertThatThrownBy(() -> ViewTypeName.of(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("전망 유형명은 필수입니다");
        }

        @Test
        @DisplayName("ViewTypeName 빈 값이면 생성 실패")
        void shouldFailWhenNameIsBlank() {
            assertThatThrownBy(() -> ViewTypeName.of(""))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("전망 유형명은 필수입니다");
        }

        @Test
        @DisplayName("ViewTypeName 200자 초과이면 생성 실패")
        void shouldFailWhenNameExceedsMaxLength() {
            String longName = "가".repeat(201);
            assertThatThrownBy(() -> ViewTypeName.of(longName))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("200자 이하여야 합니다");
        }

        @Test
        @DisplayName("ViewTypeName 200자 경계값 생성 성공")
        void shouldSucceedWhenNameIsExactlyMaxLength() {
            String name = "가".repeat(200);
            ViewTypeName viewTypeName = ViewTypeName.of(name);
            assertThat(viewTypeName.value()).hasSize(200);
        }
    }

    @Nested
    @DisplayName("VO 검증 - ViewTypeId")
    class ViewTypeIdValidation {

        @Test
        @DisplayName("ViewTypeId null 허용 (신규 엔티티)")
        void shouldAllowNullForNewEntity() {
            ViewTypeId id = ViewTypeId.of(null);
            assertThat(id.isNew()).isTrue();
        }

        @Test
        @DisplayName("ViewTypeId forNew()는 null ID 생성")
        void shouldCreateNewIdWithNull() {
            ViewTypeId id = ViewTypeId.forNew();
            assertThat(id.isNew()).isTrue();
        }

        @Test
        @DisplayName("ViewTypeId 값이 있으면 isNew() false")
        void shouldNotBeNewWhenValueExists() {
            ViewTypeId id = ViewTypeId.of(1L);
            assertThat(id.isNew()).isFalse();
            assertThat(id.value()).isEqualTo(1L);
        }
    }

    @Nested
    @DisplayName("동등성 검증")
    class Equality {

        @Test
        @DisplayName("같은 ID의 ViewType은 동등하다")
        void shouldBeEqualWithSameId() {
            ViewType a = ViewType.reconstitute(ViewTypeId.of(1L), ViewTypeCode.of("OCEAN"), ViewTypeName.of("바다"), NOW, NOW);
            ViewType b = ViewType.reconstitute(ViewTypeId.of(1L), ViewTypeCode.of("MOUNTAIN"), ViewTypeName.of("산"), NOW, NOW);
            assertThat(a).isEqualTo(b);
        }

        @Test
        @DisplayName("다른 ID의 ViewType은 동등하지 않다")
        void shouldNotBeEqualWithDifferentId() {
            ViewType a = ViewType.reconstitute(ViewTypeId.of(1L), ViewTypeCode.of("OCEAN"), ViewTypeName.of("바다"), NOW, NOW);
            ViewType b = ViewType.reconstitute(ViewTypeId.of(2L), ViewTypeCode.of("OCEAN"), ViewTypeName.of("바다"), NOW, NOW);
            assertThat(a).isNotEqualTo(b);
        }
    }
}
