package com.ryuqq.otatoy.api.extranet.property.mapper;

import com.ryuqq.otatoy.application.property.dto.query.ExtranetSearchPropertyQuery;
import com.ryuqq.otatoy.domain.property.PropertyId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * PropertyQueryApiMapper 단위 테스트.
 * 조회 요청 파라미터 -> Query/VO 변환 및 응답 변환 정확성을 검증한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@DisplayName("PropertyQueryApiMapper")
class PropertyQueryApiMapperTest {

    @Nested
    @DisplayName("toExtranetSearchQuery")
    class ToExtranetSearchQuery {

        @Test
        @DisplayName("partnerId, size, cursor가 정확하게 변환된다")
        void shouldMapAllParameters() {
            ExtranetSearchPropertyQuery query =
                PropertyQueryApiMapper.toExtranetSearchQuery(1L, 20, 100L);

            assertThat(query.partnerId().value()).isEqualTo(1L);
            assertThat(query.size()).isEqualTo(20);
            assertThat(query.cursor()).isEqualTo(100L);
        }

        @Test
        @DisplayName("cursor가 null이면 그대로 null이 전달된다")
        void shouldPassNullCursor() {
            ExtranetSearchPropertyQuery query =
                PropertyQueryApiMapper.toExtranetSearchQuery(1L, 20, null);

            assertThat(query.cursor()).isNull();
        }
    }

    @Nested
    @DisplayName("toPropertyId")
    class ToPropertyId {

        @Test
        @DisplayName("Long 값이 PropertyId VO로 정확하게 변환된다")
        void shouldConvertLongToPropertyId() {
            PropertyId propertyId = PropertyQueryApiMapper.toPropertyId(42L);

            assertThat(propertyId.value()).isEqualTo(42L);
        }
    }
}
