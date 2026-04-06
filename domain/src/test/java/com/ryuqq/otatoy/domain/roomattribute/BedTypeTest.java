package com.ryuqq.otatoy.domain.roomattribute;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BedTypeTest {

    private static final Instant NOW = Instant.parse("2026-04-04T00:00:00Z");

    @Nested
    @DisplayName("생성 검증")
    class Creation {

        @Test
        @DisplayName("BedType 정상 생성")
        void shouldCreateBedTypeSuccessfully() {
            // when
            BedType bedType = BedType.forNew(BedTypeCode.of("SINGLE"), BedTypeName.of("싱글 침대"), NOW);

            // then
            assertThat(bedType).isNotNull();
            assertThat(bedType.id()).isNotNull();
            assertThat(bedType.id().isNew()).isTrue();
            assertThat(bedType.code().value()).isEqualTo("SINGLE");
            assertThat(bedType.name().value()).isEqualTo("싱글 침대");
            assertThat(bedType.createdAt()).isEqualTo(NOW);
            assertThat(bedType.updatedAt()).isEqualTo(NOW);
        }

        @Test
        @DisplayName("reconstitute로 DB 복원 성공")
        void shouldReconstituteBedType() {
            // when
            BedType bedType = BedType.reconstitute(
                    BedTypeId.of(1L), BedTypeCode.of("DOUBLE"), BedTypeName.of("더블 침대"), NOW, NOW
            );

            // then
            assertThat(bedType.id().value()).isEqualTo(1L);
            assertThat(bedType.id().isNew()).isFalse();
            assertThat(bedType.code().value()).isEqualTo("DOUBLE");
        }
    }

    @Nested
    @DisplayName("VO 검증 - BedTypeCode")
    class BedTypeCodeValidation {

        @Test
        @DisplayName("BedTypeCode null이면 생성 실패")
        void shouldFailWhenCodeIsNull() {
            assertThatThrownBy(() -> BedTypeCode.of(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("침대 유형 코드는 필수입니다");
        }

        @Test
        @DisplayName("BedTypeCode 빈 값이면 생성 실패")
        void shouldFailWhenCodeIsBlank() {
            assertThatThrownBy(() -> BedTypeCode.of(""))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("침대 유형 코드는 필수입니다");
        }

        @Test
        @DisplayName("BedTypeCode 공백만 있으면 생성 실패")
        void shouldFailWhenCodeIsWhitespace() {
            assertThatThrownBy(() -> BedTypeCode.of("   "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("침대 유형 코드는 필수입니다");
        }

        @Test
        @DisplayName("BedTypeCode 50자 초과이면 생성 실패")
        void shouldFailWhenCodeExceedsMaxLength() {
            String longCode = "A".repeat(51);
            assertThatThrownBy(() -> BedTypeCode.of(longCode))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("50자 이하여야 합니다");
        }

        @Test
        @DisplayName("BedTypeCode 50자 경계값 생성 성공")
        void shouldSucceedWhenCodeIsExactlyMaxLength() {
            String code = "A".repeat(50);
            BedTypeCode bedTypeCode = BedTypeCode.of(code);
            assertThat(bedTypeCode.value()).hasSize(50);
        }
    }

    @Nested
    @DisplayName("VO 검증 - BedTypeName")
    class BedTypeNameValidation {

        @Test
        @DisplayName("BedTypeName null이면 생성 실패")
        void shouldFailWhenNameIsNull() {
            assertThatThrownBy(() -> BedTypeName.of(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("침대 유형명은 필수입니다");
        }

        @Test
        @DisplayName("BedTypeName 빈 값이면 생성 실패")
        void shouldFailWhenNameIsBlank() {
            assertThatThrownBy(() -> BedTypeName.of(""))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("침대 유형명은 필수입니다");
        }

        @Test
        @DisplayName("BedTypeName 200자 초과이면 생성 실패")
        void shouldFailWhenNameExceedsMaxLength() {
            String longName = "가".repeat(201);
            assertThatThrownBy(() -> BedTypeName.of(longName))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("200자 이하여야 합니다");
        }

        @Test
        @DisplayName("BedTypeName 200자 경계값 생성 성공")
        void shouldSucceedWhenNameIsExactlyMaxLength() {
            String name = "가".repeat(200);
            BedTypeName bedTypeName = BedTypeName.of(name);
            assertThat(bedTypeName.value()).hasSize(200);
        }
    }

    @Nested
    @DisplayName("VO 검증 - BedTypeId")
    class BedTypeIdValidation {

        @Test
        @DisplayName("BedTypeId null 허용 (신규 엔티티)")
        void shouldAllowNullForNewEntity() {
            BedTypeId id = BedTypeId.of(null);
            assertThat(id.isNew()).isTrue();
            assertThat(id.value()).isNull();
        }

        @Test
        @DisplayName("BedTypeId forNew()는 null ID 생성")
        void shouldCreateNewIdWithNull() {
            BedTypeId id = BedTypeId.forNew();
            assertThat(id.isNew()).isTrue();
        }

        @Test
        @DisplayName("BedTypeId 값이 있으면 isNew() false")
        void shouldNotBeNewWhenValueExists() {
            BedTypeId id = BedTypeId.of(1L);
            assertThat(id.isNew()).isFalse();
            assertThat(id.value()).isEqualTo(1L);
        }
    }

    @Nested
    @DisplayName("동등성 검증")
    class Equality {

        @Test
        @DisplayName("같은 ID의 BedType은 동등하다")
        void shouldBeEqualWithSameId() {
            BedType a = BedType.reconstitute(BedTypeId.of(1L), BedTypeCode.of("SINGLE"), BedTypeName.of("싱글"), NOW, NOW);
            BedType b = BedType.reconstitute(BedTypeId.of(1L), BedTypeCode.of("DOUBLE"), BedTypeName.of("더블"), NOW, NOW);
            assertThat(a).isEqualTo(b);
        }

        @Test
        @DisplayName("다른 ID의 BedType은 동등하지 않다")
        void shouldNotBeEqualWithDifferentId() {
            BedType a = BedType.reconstitute(BedTypeId.of(1L), BedTypeCode.of("SINGLE"), BedTypeName.of("싱글"), NOW, NOW);
            BedType b = BedType.reconstitute(BedTypeId.of(2L), BedTypeCode.of("SINGLE"), BedTypeName.of("싱글"), NOW, NOW);
            assertThat(a).isNotEqualTo(b);
        }

        @Test
        @DisplayName("ID VO가 같은 값이면 동등하다 (BedTypeId record 동등성)")
        void shouldBeEqualWhenBothIdsHaveSameValue() {
            // BedTypeId는 record이므로 BedTypeId(null) == BedTypeId(null)
            BedType a = BedType.forNew(BedTypeCode.of("SINGLE"), BedTypeName.of("싱글"), NOW);
            BedType b = BedType.forNew(BedTypeCode.of("SINGLE"), BedTypeName.of("싱글"), NOW);
            // BedType.equals: id != null (BedTypeId(null)는 null이 아님) && id.equals(r.id) (record 동등성)
            assertThat(a).isEqualTo(b);
        }
    }
}
