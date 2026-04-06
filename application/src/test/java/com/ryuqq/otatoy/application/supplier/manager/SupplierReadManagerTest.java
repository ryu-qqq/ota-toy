package com.ryuqq.otatoy.application.supplier.manager;

import com.ryuqq.otatoy.application.supplier.port.out.SupplierQueryPort;
import com.ryuqq.otatoy.domain.supplier.Supplier;
import com.ryuqq.otatoy.domain.supplier.SupplierFixture;
import com.ryuqq.otatoy.domain.supplier.SupplierStatus;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

/**
 * SupplierReadManager 단위 테스트.
 * ACTIVE 상태 필터링이 Port에 올바르게 전달되는지 검증한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@ExtendWith(MockitoExtension.class)
class SupplierReadManagerTest {

    @Mock
    SupplierQueryPort supplierQueryPort;

    @InjectMocks
    SupplierReadManager manager;

    @Test
    @DisplayName("ACTIVE 상태의 공급자 목록을 반환한다")
    void shouldReturnActiveSuppliers() {
        // given
        Supplier activeSupplier = SupplierFixture.reconstitutedSupplier();
        given(supplierQueryPort.findByStatus(SupplierStatus.ACTIVE))
                .willReturn(List.of(activeSupplier));

        // when
        List<Supplier> result = manager.findActiveSuppliers();

        // then
        assertThat(result).hasSize(1);
        then(supplierQueryPort).should().findByStatus(SupplierStatus.ACTIVE);
    }

    @Test
    @DisplayName("ACTIVE 공급자가 없으면 빈 목록을 반환한다")
    void shouldReturnEmptyWhenNoActiveSuppliers() {
        // given
        given(supplierQueryPort.findByStatus(SupplierStatus.ACTIVE)).willReturn(List.of());

        // when
        List<Supplier> result = manager.findActiveSuppliers();

        // then
        assertThat(result).isEmpty();
    }
}
