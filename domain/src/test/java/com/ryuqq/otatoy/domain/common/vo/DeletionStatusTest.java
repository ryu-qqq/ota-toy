package com.ryuqq.otatoy.domain.common.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class DeletionStatusTest {

    @Nested
    @DisplayName("생성 검증")
    class Creation {

        @Test
        @DisplayName("active()는 삭제되지 않은 상태를 반환한다")
        void activeShouldNotBeDeleted() {
            DeletionStatus status = DeletionStatus.active();

            assertThat(status.deleted()).isFalse();
            assertThat(status.deletedAt()).isNull();
        }

        @Test
        @DisplayName("deleted()는 삭제된 상태를 반환한다")
        void deletedShouldBeDeleted() {
            Instant now = Instant.now();
            DeletionStatus status = DeletionStatus.deleted(now);

            assertThat(status.deleted()).isTrue();
            assertThat(status.deletedAt()).isEqualTo(now);
        }
    }

    @Nested
    @DisplayName("동등성 검증")
    class Equality {

        @Test
        @DisplayName("같은 active 상태는 동등하다")
        void activeStatusesShouldBeEqual() {
            assertThat(DeletionStatus.active()).isEqualTo(DeletionStatus.active());
        }

        @Test
        @DisplayName("같은 시점의 deleted 상태는 동등하다")
        void sameDeletedAtShouldBeEqual() {
            Instant now = Instant.now();
            assertThat(DeletionStatus.deleted(now)).isEqualTo(DeletionStatus.deleted(now));
        }
    }
}
