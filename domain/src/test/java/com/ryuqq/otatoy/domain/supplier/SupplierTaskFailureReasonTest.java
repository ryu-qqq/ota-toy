package com.ryuqq.otatoy.domain.supplier;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SupplierTaskFailureReasonTest {

    private static final Instant NOW = Instant.parse("2026-04-06T00:00:00Z");

    @Nested
    @DisplayName("생성 검증")
    class Creation {

        @Test
        @DisplayName("모든 필드가 있으면 정상 생성")
        void shouldCreateWithAllFields() {
            SupplierTaskFailureReason reason = SupplierTaskFailureReason.of(500, "ERR-001", "서버 에러", NOW);

            assertThat(reason.httpStatus()).isEqualTo(500);
            assertThat(reason.errorCode()).isEqualTo("ERR-001");
            assertThat(reason.errorMessage()).isEqualTo("서버 에러");
            assertThat(reason.occurredAt()).isEqualTo(NOW);
        }

        @Test
        @DisplayName("httpStatus와 errorCode는 null 허용")
        void shouldAllowNullableFields() {
            SupplierTaskFailureReason reason = SupplierTaskFailureReason.of(null, null, "타임아웃", NOW);

            assertThat(reason.httpStatus()).isNull();
            assertThat(reason.errorCode()).isNull();
            assertThat(reason.errorMessage()).isEqualTo("타임아웃");
        }

        @Test
        @DisplayName("errorMessage가 null이면 생성 실패")
        void shouldFailWhenMessageIsNull() {
            assertThatThrownBy(() -> SupplierTaskFailureReason.of(500, "ERR", null, NOW))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("에러 메시지");
        }

        @Test
        @DisplayName("errorMessage가 blank이면 생성 실패")
        void shouldFailWhenMessageIsBlank() {
            assertThatThrownBy(() -> SupplierTaskFailureReason.of(500, "ERR", "  ", NOW))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("에러 메시지");
        }

        @Test
        @DisplayName("occurredAt이 null이면 생성 실패")
        void shouldFailWhenOccurredAtIsNull() {
            assertThatThrownBy(() -> SupplierTaskFailureReason.of(500, "ERR", "에러", null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("실패 시각");
        }
    }

    @Nested
    @DisplayName("JSON 직렬화/역직렬화")
    class JsonSerialization {

        @Test
        @DisplayName("전체 필드가 있는 경우 직렬화 후 역직렬화하면 동일하다")
        void shouldRoundTripWithAllFields() {
            SupplierTaskFailureReason original = SupplierTaskFailureReason.of(500, "ERR-001", "서버 에러", NOW);
            String json = original.toJson();
            SupplierTaskFailureReason restored = SupplierTaskFailureReason.fromJson(json);

            assertThat(restored).isEqualTo(original);
        }

        @Test
        @DisplayName("nullable 필드가 null인 경우 직렬화 후 역직렬화하면 동일하다")
        void shouldRoundTripWithNullFields() {
            SupplierTaskFailureReason original = SupplierTaskFailureReason.of(null, null, "타임아웃", NOW);
            String json = original.toJson();
            SupplierTaskFailureReason restored = SupplierTaskFailureReason.fromJson(json);

            assertThat(restored).isEqualTo(original);
        }

        @Test
        @DisplayName("특수 문자가 포함된 errorMessage도 정상 처리한다")
        void shouldHandleSpecialCharacters() {
            SupplierTaskFailureReason original = SupplierTaskFailureReason.of(
                    400, "PARSE_ERR", "JSON 파싱 실패: \"unexpected token\"", NOW);
            String json = original.toJson();
            SupplierTaskFailureReason restored = SupplierTaskFailureReason.fromJson(json);

            assertThat(restored).isEqualTo(original);
        }

        @Test
        @DisplayName("null JSON이면 예외 발생")
        void shouldFailOnNullJson() {
            assertThatThrownBy(() -> SupplierTaskFailureReason.fromJson(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("빈 JSON이면 예외 발생")
        void shouldFailOnBlankJson() {
            assertThatThrownBy(() -> SupplierTaskFailureReason.fromJson("  "))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("SupplierTaskId")
    class TaskIdTest {

        @Test
        @DisplayName("forNew()는 null ID를 반환한다")
        void forNewReturnsNullId() {
            SupplierTaskId id = SupplierTaskId.forNew();
            assertThat(id.value()).isNull();
            assertThat(id.isNew()).isTrue();
        }

        @Test
        @DisplayName("of(100L)은 값이 있는 ID를 반환한다")
        void ofReturnsValueId() {
            SupplierTaskId id = SupplierTaskId.of(100L);
            assertThat(id.value()).isEqualTo(100L);
            assertThat(id.isNew()).isFalse();
        }
    }
}
