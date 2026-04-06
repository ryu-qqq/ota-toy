package com.ryuqq.otatoy.application.supplier.manager;

import com.ryuqq.otatoy.application.supplier.port.out.SupplierRawDataQueryPort;
import com.ryuqq.otatoy.domain.supplier.SupplierFixture;
import com.ryuqq.otatoy.domain.supplier.SupplierRawData;
import com.ryuqq.otatoy.domain.supplier.SupplierRawDataStatus;
import com.ryuqq.otatoy.domain.supplier.SupplierId;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
 * SupplierRawDataReadManager 단위 테스트.
 * FETCHED 상태 필터링이 올바르게 Port에 전달되는지 검증한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@ExtendWith(MockitoExtension.class)
class SupplierRawDataReadManagerTest {

    @Mock
    SupplierRawDataQueryPort rawDataQueryPort;

    @InjectMocks
    SupplierRawDataReadManager manager;

    @Nested
    @DisplayName("findFetched")
    class FindFetched {

        @Test
        @DisplayName("특정 공급자의 FETCHED 상태 RawData를 조회한다")
        void shouldReturnFetchedRawDataBySupplierId() {
            // given
            SupplierId supplierId = SupplierId.of(1L);
            List<SupplierRawData> rawDataList = List.of(SupplierFixture.fetchedRawData());
            given(rawDataQueryPort.findBySupplierIdAndStatus(supplierId, SupplierRawDataStatus.FETCHED))
                    .willReturn(rawDataList);

            // when
            List<SupplierRawData> result = manager.findFetched(supplierId);

            // then
            assertThat(result).hasSize(1);
            then(rawDataQueryPort).should()
                    .findBySupplierIdAndStatus(supplierId, SupplierRawDataStatus.FETCHED);
        }
    }

    @Nested
    @DisplayName("findAllFetched")
    class FindAllFetched {

        @Test
        @DisplayName("전체 FETCHED 상태 RawData를 조회한다")
        void shouldReturnAllFetchedRawData() {
            // given
            List<SupplierRawData> rawDataList = List.of(SupplierFixture.fetchedRawData());
            given(rawDataQueryPort.findByStatus(SupplierRawDataStatus.FETCHED)).willReturn(rawDataList);

            // when
            List<SupplierRawData> result = manager.findAllFetched();

            // then
            assertThat(result).hasSize(1);
            then(rawDataQueryPort).should().findByStatus(SupplierRawDataStatus.FETCHED);
        }
    }

    @Nested
    @DisplayName("findFetchedBatch")
    class FindFetchedBatch {

        @Test
        @DisplayName("배치 크기 제한으로 FETCHED 상태 RawData를 조회한다")
        void shouldReturnFetchedRawDataWithLimit() {
            // given
            int batchSize = 50;
            List<SupplierRawData> rawDataList = List.of(SupplierFixture.fetchedRawData());
            given(rawDataQueryPort.findByStatusWithLimit(SupplierRawDataStatus.FETCHED, batchSize))
                    .willReturn(rawDataList);

            // when
            List<SupplierRawData> result = manager.findFetchedBatch(batchSize);

            // then
            assertThat(result).hasSize(1);
            then(rawDataQueryPort).should()
                    .findByStatusWithLimit(SupplierRawDataStatus.FETCHED, batchSize);
        }
    }
}
