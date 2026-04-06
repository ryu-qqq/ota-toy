package com.ryuqq.otatoy.application.supplier.parser;

import com.ryuqq.otatoy.domain.supplier.SupplierApiType;
import com.ryuqq.otatoy.domain.supplier.SupplierTaskType;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ApiType + TaskType 조합으로 RawDataParser를 라우팅하는 Provider.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Component
public class SupplierRawDataParserProvider {

    private final Map<String, SupplierRawDataParser<?>> parserMap;

    public SupplierRawDataParserProvider(List<SupplierRawDataParser<?>> parsers) {
        this.parserMap = parsers.stream()
                .collect(Collectors.toMap(
                        p -> parserKey(p.supportedApiType(), p.supportedTaskType()),
                        p -> p
                ));
    }

    @SuppressWarnings("unchecked")
    public <T> SupplierRawDataParser<T> getParser(SupplierApiType apiType, SupplierTaskType taskType) {
        String key = parserKey(apiType, taskType);
        SupplierRawDataParser<?> parser = parserMap.get(key);
        if (parser == null) {
            throw new IllegalArgumentException(
                    "지원하지 않는 파서 조합: apiType=" + apiType + ", taskType=" + taskType);
        }
        return (SupplierRawDataParser<T>) parser;
    }

    private static String parserKey(SupplierApiType apiType, SupplierTaskType taskType) {
        return apiType.name() + ":" + taskType.name();
    }
}
