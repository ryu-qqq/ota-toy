package com.ryuqq.otatoy.domain.supplier;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SupplierTest {

    private static final Instant NOW = Instant.parse("2026-04-04T00:00:00Z");
    private static final Instant LATER = Instant.parse("2026-04-05T00:00:00Z");

    @Nested
    @DisplayName("T-1: 생성 검증 -- forNew()")
    class Creation {

        @Test
        @DisplayName("정상 생성 시 ACTIVE 상태이고 id.value()는 null이다")
        void shouldCreateWithActiveStatusAndNullId() {
            Supplier supplier = SupplierFixture.activeSupplier();

            assertThat(supplier.id().value()).isNull();
            assertThat(supplier.status()).isEqualTo(SupplierStatus.ACTIVE);
            assertThat(supplier.name()).isEqualTo(SupplierFixture.DEFAULT_NAME);
            assertThat(supplier.nameKr()).isEqualTo(SupplierFixture.DEFAULT_NAME_KR);
            assertThat(supplier.companyTitle()).isEqualTo(SupplierFixture.DEFAULT_COMPANY_TITLE);
            assertThat(supplier.ownerName()).isEqualTo(SupplierFixture.DEFAULT_OWNER_NAME);
            assertThat(supplier.businessNo()).isEqualTo(SupplierFixture.DEFAULT_BUSINESS_NO);
            assertThat(supplier.phone()).isEqualTo(SupplierFixture.DEFAULT_PHONE);
            assertThat(supplier.email()).isEqualTo(SupplierFixture.DEFAULT_EMAIL);
            assertThat(supplier.createdAt()).isEqualTo(NOW);
            assertThat(supplier.updatedAt()).isEqualTo(NOW);
        }

        @Test
        @DisplayName("companyTitle이 null이면 생성 실패")
        void shouldFailWhenCompanyTitleIsNull() {
            assertThatThrownBy(() -> CompanyTitle.of(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("회사명");
        }

        @Test
        @DisplayName("companyTitle이 blank이면 생성 실패")
        void shouldFailWhenCompanyTitleIsBlank() {
            assertThatThrownBy(() -> CompanyTitle.of("  "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("회사명");
        }

        @Test
        @DisplayName("ownerName이 null이면 생성 실패")
        void shouldFailWhenOwnerNameIsNull() {
            assertThatThrownBy(() -> OwnerName.of(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("대표자명");
        }

        @Test
        @DisplayName("businessNo가 null이면 생성 실패")
        void shouldFailWhenBusinessNoIsNull() {
            assertThatThrownBy(() -> BusinessNo.of(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("사업자번호");
        }
    }

    @Nested
    @DisplayName("T-2: reconstitute 검증")
    class Reconstitution {

        @Test
        @DisplayName("reconstitute는 모든 필드를 그대로 복원한다")
        void shouldReconstituteAllFields() {
            Supplier supplier = SupplierFixture.reconstitutedSupplier();

            assertThat(supplier.id()).isEqualTo(SupplierId.of(1L));
            assertThat(supplier.status()).isEqualTo(SupplierStatus.ACTIVE);
            assertThat(supplier.name()).isEqualTo(SupplierFixture.DEFAULT_NAME);
        }
    }

    @Nested
    @DisplayName("T-3: 상태 전이 -- suspend()")
    class Suspend {

        @Test
        @DisplayName("ACTIVE -> SUSPENDED 정상 전이")
        void shouldSuspendFromActive() {
            Supplier supplier = SupplierFixture.supplierWithStatus(SupplierStatus.ACTIVE);
            supplier.suspend(LATER);

            assertThat(supplier.status()).isEqualTo(SupplierStatus.SUSPENDED);
            assertThat(supplier.updatedAt()).isEqualTo(LATER);
        }

        @Test
        @DisplayName("이미 SUSPENDED이면 예외")
        void shouldFailWhenAlreadySuspended() {
            Supplier supplier = SupplierFixture.suspendedSupplier();

            assertThatThrownBy(() -> supplier.suspend(LATER))
                    .isInstanceOf(SupplierAlreadySuspendedException.class);
        }

        @Test
        @DisplayName("TERMINATED에서 suspend 불가")
        void shouldFailWhenTerminated() {
            Supplier supplier = SupplierFixture.terminatedSupplier();

            assertThatThrownBy(() -> supplier.suspend(LATER))
                    .isInstanceOf(SupplierAlreadyTerminatedException.class);
        }
    }

    @Nested
    @DisplayName("T-4: 상태 전이 -- activate()")
    class Activate {

        @Test
        @DisplayName("SUSPENDED -> ACTIVE 정상 전이")
        void shouldActivateFromSuspended() {
            Supplier supplier = SupplierFixture.suspendedSupplier();
            supplier.activate(LATER);

            assertThat(supplier.status()).isEqualTo(SupplierStatus.ACTIVE);
            assertThat(supplier.updatedAt()).isEqualTo(LATER);
        }

        @Test
        @DisplayName("이미 ACTIVE이면 예외")
        void shouldFailWhenAlreadyActive() {
            Supplier supplier = SupplierFixture.supplierWithStatus(SupplierStatus.ACTIVE);

            assertThatThrownBy(() -> supplier.activate(LATER))
                    .isInstanceOf(InvalidSupplierStateTransitionException.class);
        }

        @Test
        @DisplayName("TERMINATED에서 activate 불가")
        void shouldFailWhenTerminated() {
            Supplier supplier = SupplierFixture.terminatedSupplier();

            assertThatThrownBy(() -> supplier.activate(LATER))
                    .isInstanceOf(SupplierAlreadyTerminatedException.class);
        }
    }

    @Nested
    @DisplayName("T-5: 상태 전이 -- terminate()")
    class Terminate {

        @Test
        @DisplayName("ACTIVE -> TERMINATED 정상 전이")
        void shouldTerminateFromActive() {
            Supplier supplier = SupplierFixture.supplierWithStatus(SupplierStatus.ACTIVE);
            supplier.terminate(LATER);

            assertThat(supplier.status()).isEqualTo(SupplierStatus.TERMINATED);
            assertThat(supplier.updatedAt()).isEqualTo(LATER);
        }

        @Test
        @DisplayName("SUSPENDED -> TERMINATED 정상 전이")
        void shouldTerminateFromSuspended() {
            Supplier supplier = SupplierFixture.suspendedSupplier();
            supplier.terminate(LATER);

            assertThat(supplier.status()).isEqualTo(SupplierStatus.TERMINATED);
        }

        @Test
        @DisplayName("이미 TERMINATED이면 예외")
        void shouldFailWhenAlreadyTerminated() {
            Supplier supplier = SupplierFixture.terminatedSupplier();

            assertThatThrownBy(() -> supplier.terminate(LATER))
                    .isInstanceOf(SupplierAlreadyTerminatedException.class);
        }
    }

    @Nested
    @DisplayName("T-6: isActive()")
    class IsActive {

        @Test
        @DisplayName("ACTIVE이면 true")
        void shouldReturnTrueWhenActive() {
            Supplier supplier = SupplierFixture.supplierWithStatus(SupplierStatus.ACTIVE);
            assertThat(supplier.isActive()).isTrue();
        }

        @Test
        @DisplayName("SUSPENDED이면 false")
        void shouldReturnFalseWhenSuspended() {
            Supplier supplier = SupplierFixture.suspendedSupplier();
            assertThat(supplier.isActive()).isFalse();
        }

        @Test
        @DisplayName("TERMINATED이면 false")
        void shouldReturnFalseWhenTerminated() {
            Supplier supplier = SupplierFixture.terminatedSupplier();
            assertThat(supplier.isActive()).isFalse();
        }
    }

    @Nested
    @DisplayName("T-7: equals/hashCode")
    class Equality {

        @Test
        @DisplayName("같은 id의 Supplier는 동등하다")
        void shouldBeEqualWithSameId() {
            Supplier s1 = SupplierFixture.supplierWithStatus(SupplierStatus.ACTIVE);
            Supplier s2 = SupplierFixture.supplierWithStatus(SupplierStatus.SUSPENDED);

            assertThat(s1).isEqualTo(s2);
            assertThat(s1.hashCode()).isEqualTo(s2.hashCode());
        }

        @Test
        @DisplayName("id가 null인 두 Supplier는 동등하지 않다")
        void shouldNotBeEqualWhenIdIsNull() {
            Supplier s1 = SupplierFixture.activeSupplier();
            Supplier s2 = SupplierFixture.activeSupplier();

            assertThat(s1).isNotEqualTo(s2);
        }

        @Test
        @DisplayName("자기 자신과는 동등하다")
        void shouldBeEqualToSelf() {
            Supplier s = SupplierFixture.activeSupplier();
            assertThat(s).isEqualTo(s);
        }
    }
}
