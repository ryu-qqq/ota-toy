package com.ryuqq.otatoy.persistence.property.adapter;

import com.ryuqq.otatoy.application.common.dto.SliceResult;
import com.ryuqq.otatoy.application.property.port.out.PropertySearchQueryPort;
import com.ryuqq.otatoy.domain.common.query.SliceMeta;
import com.ryuqq.otatoy.domain.property.Property;
import com.ryuqq.otatoy.domain.property.PropertySliceCriteria;
import com.ryuqq.otatoy.persistence.property.mapper.PropertyEntityMapper;
import com.ryuqq.otatoy.persistence.property.repository.PropertyQueryDslRepository;
import com.ryuqq.otatoy.persistence.property.repository.PropertySearchQueryDslRepository;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 고객 숙소 검색 전용 Query Adapter.
 * PropertySearchQueryPort를 구현하며, QueryDslRepository만 의존한다.
 * 크로스 BC 조인 쿼리를 통해 검색 조건에 맞는 숙소를 조회한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Component
public class PropertySearchQueryAdapter implements PropertySearchQueryPort {

    private final PropertySearchQueryDslRepository searchQueryDslRepository;
    private final PropertyQueryDslRepository propertyQueryDslRepository;
    private final PropertyEntityMapper mapper;

    public PropertySearchQueryAdapter(PropertySearchQueryDslRepository searchQueryDslRepository,
                                       PropertyQueryDslRepository propertyQueryDslRepository,
                                       PropertyEntityMapper mapper) {
        this.searchQueryDslRepository = searchQueryDslRepository;
        this.propertyQueryDslRepository = propertyQueryDslRepository;
        this.mapper = mapper;
    }

    @Override
    public SliceResult<Property> searchByCondition(PropertySliceCriteria criteria) {
        // 1단계: 검색 조건에 맞는 Property ID 목록 조회 (크로스 BC 조인)
        List<Long> propertyIds = searchQueryDslRepository.searchPropertyIds(
                criteria.keyword(),
                criteria.region(),
                criteria.propertyTypeId() != null ? criteria.propertyTypeId().value() : null,
                criteria.checkIn(),
                criteria.checkOut(),
                criteria.guests(),
                criteria.minPrice(),
                criteria.maxPrice(),
                criteria.amenityTypes(),
                criteria.freeCancellationOnly(),
                criteria.starRating(),
                criteria.sortKey(),
                criteria.direction(),
                criteria.size(),
                criteria.cursor()
        );

        if (propertyIds.isEmpty()) {
            return SliceResult.empty();
        }

        // hasNext 판단
        boolean hasNext = propertyIds.size() > criteria.size();
        List<Long> targetIds = hasNext
                ? propertyIds.subList(0, criteria.size())
                : propertyIds;

        // 2단계: Property ID로 상세 정보 조회
        List<Property> content = targetIds.stream()
                .map(id -> propertyQueryDslRepository.findById(id).orElse(null))
                .filter(entity -> entity != null)
                .map(mapper::toDomain)
                .toList();

        Long nextCursor = hasNext && !content.isEmpty()
                ? content.get(content.size() - 1).id().value()
                : null;

        return SliceResult.of(content, new SliceMeta(hasNext, nextCursor));
    }
}
