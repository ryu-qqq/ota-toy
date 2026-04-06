package com.ryuqq.otatoy.domain.brand;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Brand 엔티티 검증")
class BrandTest {

    private static final Instant NOW = BrandFixture.DEFAULT_NOW;
    private static final Instant LATER = NOW.plusSeconds(3600);

    @Nested
    @DisplayName("T-1: forNew() 팩토리 메서드")
    class ForNewTest {

        @Test
        @DisplayName("신규 Brand는 id가 null이다")
        void shouldHaveNullId() {
            Brand brand = BrandFixture.newBrand();

            assertThat(brand.id()).isNull();
            assertThat(brand.name()).isEqualTo(BrandFixture.DEFAULT_NAME);
            assertThat(brand.nameKr()).isEqualTo(BrandFixture.DEFAULT_NAME_KR);
            assertThat(brand.logoUrl()).isEqualTo(BrandFixture.DEFAULT_LOGO_URL);
            assertThat(brand.createdAt()).isEqualTo(NOW);
            assertThat(brand.updatedAt()).isEqualTo(NOW);
        }

        @Test
        @DisplayName("nullable 필드가 null인 Brand도 생성 가능하다")
        void shouldCreateMinimalBrand() {
            Brand brand = BrandFixture.minimalBrand();

            assertThat(brand.nameKr().value()).isNull();
            assertThat(brand.logoUrl().value()).isNull();
        }
    }

    @Nested
    @DisplayName("T-2: reconstitute() 팩토리 메서드")
    class ReconstituteTest {

        @Test
        @DisplayName("DB 복원 시 모든 필드가 그대로 복원된다")
        void shouldReconstituteAllFields() {
            Brand brand = BrandFixture.reconstitutedBrand();

            assertThat(brand.id()).isEqualTo(BrandId.of(1L));
            assertThat(brand.name()).isEqualTo(BrandFixture.DEFAULT_NAME);
            assertThat(brand.nameKr()).isEqualTo(BrandFixture.DEFAULT_NAME_KR);
            assertThat(brand.logoUrl()).isEqualTo(BrandFixture.DEFAULT_LOGO_URL);
            assertThat(brand.createdAt()).isEqualTo(NOW);
            assertThat(brand.updatedAt()).isEqualTo(NOW);
        }
    }

    @Nested
    @DisplayName("T-3: rename() 상태 변경")
    class RenameTest {

        @Test
        @DisplayName("이름 변경 시 name과 updatedAt이 갱신된다")
        void shouldUpdateNameAndTimestamp() {
            Brand brand = BrandFixture.reconstitutedBrand();
            BrandName newName = BrandName.of("NewBrand");

            brand.rename(newName, LATER);

            assertThat(brand.name()).isEqualTo(newName);
            assertThat(brand.updatedAt()).isEqualTo(LATER);
        }

        @Test
        @DisplayName("이름 변경 후에도 다른 필드는 유지된다")
        void shouldPreserveOtherFields() {
            Brand brand = BrandFixture.reconstitutedBrand();

            brand.rename(BrandName.of("Changed"), LATER);

            assertThat(brand.id()).isEqualTo(BrandId.of(1L));
            assertThat(brand.nameKr()).isEqualTo(BrandFixture.DEFAULT_NAME_KR);
            assertThat(brand.logoUrl()).isEqualTo(BrandFixture.DEFAULT_LOGO_URL);
            assertThat(brand.createdAt()).isEqualTo(NOW);
        }
    }

    @Nested
    @DisplayName("T-4: updateLogoUrl() 상태 변경")
    class UpdateLogoUrlTest {

        @Test
        @DisplayName("로고 URL 변경 시 logoUrl과 updatedAt이 갱신된다")
        void shouldUpdateLogoUrlAndTimestamp() {
            Brand brand = BrandFixture.reconstitutedBrand();
            LogoUrl newUrl = LogoUrl.of("https://example.com/new-logo.png");

            brand.updateLogoUrl(newUrl, LATER);

            assertThat(brand.logoUrl()).isEqualTo(newUrl);
            assertThat(brand.updatedAt()).isEqualTo(LATER);
        }

        @Test
        @DisplayName("로고 URL을 null로 변경할 수 있다")
        void shouldAllowNullLogoUrl() {
            Brand brand = BrandFixture.reconstitutedBrand();

            brand.updateLogoUrl(LogoUrl.of(null), LATER);

            assertThat(brand.logoUrl().value()).isNull();
        }
    }

    @Nested
    @DisplayName("T-5: equals/hashCode")
    class EqualsHashCodeTest {

        @Test
        @DisplayName("같은 ID를 가진 Brand는 동등하다")
        void sameIdShouldBeEqual() {
            Brand b1 = BrandFixture.brandWithId(1L);
            Brand b2 = Brand.reconstitute(
                    BrandId.of(1L), BrandName.of("다른이름"), BrandNameKr.of("다른한글명"),
                    LogoUrl.of(null), NOW, LATER
            );

            assertThat(b1).isEqualTo(b2);
            assertThat(b1.hashCode()).isEqualTo(b2.hashCode());
        }

        @Test
        @DisplayName("다른 ID를 가진 Brand는 동등하지 않다")
        void differentIdShouldNotBeEqual() {
            Brand b1 = BrandFixture.brandWithId(1L);
            Brand b2 = BrandFixture.brandWithId(2L);

            assertThat(b1).isNotEqualTo(b2);
        }

        @Test
        @DisplayName("forNew()로 만든 두 객체는 id가 null이므로 equals false")
        void forNewBrandsShouldNotBeEqual() {
            Brand b1 = Brand.forNew(BrandName.of("A"), BrandNameKr.of(null), LogoUrl.of(null), NOW);
            Brand b2 = Brand.forNew(BrandName.of("B"), BrandNameKr.of(null), LogoUrl.of(null), NOW);

            assertThat(b1).isNotEqualTo(b2);
        }
    }
}
