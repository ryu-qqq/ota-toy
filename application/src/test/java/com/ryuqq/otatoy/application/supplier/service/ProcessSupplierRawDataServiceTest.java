package com.ryuqq.otatoy.application.supplier.service;

import com.ryuqq.otatoy.application.supplier.dto.command.ProcessSupplierRawDataCommand;
import com.ryuqq.otatoy.application.supplier.manager.SupplierRawDataReadManager;
import com.ryuqq.otatoy.application.supplier.manager.SupplierRawDataTransactionManager;
import com.ryuqq.otatoy.application.supplier.processor.SupplierRawDataProcessor;
import com.ryuqq.otatoy.application.supplier.processor.SupplierRawDataProcessorProvider;
import com.ryuqq.otatoy.domain.supplier.SupplierFixture;
import com.ryuqq.otatoy.domain.supplier.SupplierRawData;
import com.ryuqq.otatoy.domain.supplier.SupplierRawDataStatus;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class ProcessSupplierRawDataServiceTest {

    @Mock
    SupplierRawDataReadManager rawDataReadManager;

    @Mock
    SupplierRawDataTransactionManager rawDataTransactionManager;

    @Mock
    SupplierRawDataProcessorProvider processorProvider;

    @Mock
    SupplierRawDataProcessor processor;

    @InjectMocks
    ProcessSupplierRawDataService service;

    private static final Instant NOW = Instant.parse("2026-04-06T12:00:00Z");
    private static final ProcessSupplierRawDataCommand COMMAND = ProcessSupplierRawDataCommand.of(50, NOW);

    @Nested
    @DisplayName("정상 흐름")
    class Success {

        @Test
        @DisplayName("FETCHED RawData를 TaskType별 프로세서로 가공하고 SYNCED로 전이한다")
        void shouldProcessAndMarkSynced() {
            // given
            SupplierRawData rawData = SupplierFixture.fetchedRawData();
            given(rawDataReadManager.findFetchedBatch(50)).willReturn(List.of(rawData));
            given(processorProvider.getProcessor(rawData.taskType())).willReturn(processor);

            // when
            service.execute(COMMAND);

            // then
            assertThat(rawData.status()).isEqualTo(SupplierRawDataStatus.SYNCED);
            then(processor).should().process(rawData);
        }
    }

    @Nested
    @DisplayName("실패 흐름")
    class Failure {

        @Test
        @DisplayName("가공 실패 시 FAILED로 전이하고 나머지는 계속 처리한다")
        void shouldMarkFailedAndContinue() {
            // given
            SupplierRawData rawData = SupplierFixture.fetchedRawData();
            given(rawDataReadManager.findFetchedBatch(50)).willReturn(List.of(rawData));
            given(processorProvider.getProcessor(rawData.taskType())).willReturn(processor);
            willThrow(new RuntimeException("파싱 실패")).given(processor).process(rawData);

            // when
            service.execute(COMMAND);

            // then
            assertThat(rawData.status()).isEqualTo(SupplierRawDataStatus.FAILED);
        }
    }

    @Nested
    @DisplayName("빈 데이터")
    class EmptyData {

        @Test
        @DisplayName("FETCHED 데이터가 없으면 아무 것도 하지 않는다")
        void shouldDoNothingWhenNoFetchedData() {
            // given
            given(rawDataReadManager.findFetchedBatch(50)).willReturn(Collections.emptyList());

            // when
            service.execute(COMMAND);

            // then
            then(processorProvider).should(never()).getProcessor(any());
        }
    }
}
