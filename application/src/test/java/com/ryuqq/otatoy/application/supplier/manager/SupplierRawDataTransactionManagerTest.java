package com.ryuqq.otatoy.application.supplier.manager;

import com.ryuqq.otatoy.application.supplier.port.out.SupplierRawDataCommandPort;
import com.ryuqq.otatoy.domain.supplier.SupplierFixture;
import com.ryuqq.otatoy.domain.supplier.SupplierRawData;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.BDDMockito.then;

/**
 * SupplierRawDataTransactionManager 단위 테스트.
 * RawData 저장이 올바르게 Port에 위임되는지 검증한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@ExtendWith(MockitoExtension.class)
class SupplierRawDataTransactionManagerTest {

    @Mock
    SupplierRawDataCommandPort rawDataCommandPort;

    @InjectMocks
    SupplierRawDataTransactionManager manager;

    @Test
    @DisplayName("RawData를 Port에 위임하여 저장한다")
    void shouldDelegatePersistToPort() {
        // given
        SupplierRawData rawData = SupplierFixture.fetchedRawData();

        // when
        manager.persist(rawData);

        // then
        then(rawDataCommandPort).should().persist(rawData);
    }
}
