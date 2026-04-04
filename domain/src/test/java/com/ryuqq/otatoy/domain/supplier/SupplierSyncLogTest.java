package com.ryuqq.otatoy.domain.supplier;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class SupplierSyncLogTest {

    private static final Instant NOW = Instant.parse("2026-04-04T00:00:00Z");

    @Nested
    @DisplayName("T-1: 생성 검증 -- forSuccess()")
    class SuccessCreation {

        @Test
        @DisplayName("성공 로그 생성 시 SUCCESS 상태이고 카운트가 정확하다")
        void shouldCreateSuccessLog() {
            SupplierSyncLog log = SupplierFixture.successSyncLog();

            assertThat(log.id().value()).isNull();
            assertThat(log.status()).isEqualTo(SupplierSyncStatus.SUCCESS);
            assertThat(log.supplierId()).isEqualTo(SupplierId.of(1L));
            assertThat(log.syncType()).isEqualTo(SupplierSyncType.PROPERTY);
            assertThat(log.syncedAt()).isEqualTo(NOW);
            assertThat(log.totalCount()).isEqualTo(10);
            assertThat(log.createdCount()).isEqualTo(5);
            assertThat(log.updatedCount()).isEqualTo(3);
            assertThat(log.deletedCount()).isEqualTo(2);
            assertThat(log.errorMessage()).isNull();
        }
    }

    @Nested
    @DisplayName("T-2: 생성 검증 -- forFailed()")
    class FailedCreation {

        @Test
        @DisplayName("실패 로그 생성 시 FAILED 상태이고 카운트가 모두 0이다")
        void shouldCreateFailedLog() {
            SupplierSyncLog log = SupplierFixture.failedSyncLog();

            assertThat(log.id().value()).isNull();
            assertThat(log.status()).isEqualTo(SupplierSyncStatus.FAILED);
            assertThat(log.totalCount()).isEqualTo(0);
            assertThat(log.createdCount()).isEqualTo(0);
            assertThat(log.updatedCount()).isEqualTo(0);
            assertThat(log.deletedCount()).isEqualTo(0);
            assertThat(log.errorMessage()).isEqualTo("Connection timeout");
        }
    }

    @Nested
    @DisplayName("T-3: reconstitute 검증")
    class Reconstitution {

        @Test
        @DisplayName("reconstitute는 모든 필드를 그대로 복원한다")
        void shouldReconstituteAllFields() {
            SupplierSyncLog log = SupplierFixture.reconstitutedSyncLog(SupplierSyncStatus.SUCCESS);

            assertThat(log.id()).isEqualTo(SupplierSyncLogId.of(1L));
            assertThat(log.status()).isEqualTo(SupplierSyncStatus.SUCCESS);
            assertThat(log.syncType()).isEqualTo(SupplierSyncType.FULL);
            assertThat(log.errorMessage()).isNull();
        }

        @Test
        @DisplayName("실패 상태 복원 시 에러 메시지가 포함된다")
        void shouldReconstituteWithErrorMessage() {
            SupplierSyncLog log = SupplierFixture.reconstitutedSyncLog(SupplierSyncStatus.FAILED);

            assertThat(log.status()).isEqualTo(SupplierSyncStatus.FAILED);
            assertThat(log.errorMessage()).isEqualTo("에러 발생");
        }
    }

    @Nested
    @DisplayName("T-4: markFailed()")
    class MarkFailed {

        @Test
        @DisplayName("markFailed 호출 시 FAILED 상태로 전이되고 에러 메시지가 설정된다")
        void shouldMarkAsFailed() {
            SupplierSyncLog log = SupplierFixture.successSyncLog();

            log.markFailed("후속 처리 실패");

            assertThat(log.status()).isEqualTo(SupplierSyncStatus.FAILED);
            assertThat(log.errorMessage()).isEqualTo("후속 처리 실패");
        }
    }

    @Nested
    @DisplayName("T-5: equals/hashCode")
    class Equality {

        @Test
        @DisplayName("같은 id의 SupplierSyncLog는 동등하다")
        void shouldBeEqualWithSameId() {
            SupplierSyncLog log1 = SupplierFixture.reconstitutedSyncLog(SupplierSyncStatus.SUCCESS);
            SupplierSyncLog log2 = SupplierFixture.reconstitutedSyncLog(SupplierSyncStatus.FAILED);

            assertThat(log1).isEqualTo(log2);
            assertThat(log1.hashCode()).isEqualTo(log2.hashCode());
        }

        @Test
        @DisplayName("id가 null인 두 SupplierSyncLog는 동등하지 않다")
        void shouldNotBeEqualWhenIdIsNull() {
            SupplierSyncLog log1 = SupplierFixture.successSyncLog();
            SupplierSyncLog log2 = SupplierFixture.successSyncLog();

            assertThat(log1).isNotEqualTo(log2);
        }
    }
}
