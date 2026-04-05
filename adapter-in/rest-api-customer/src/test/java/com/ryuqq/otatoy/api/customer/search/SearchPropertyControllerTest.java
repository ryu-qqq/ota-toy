package com.ryuqq.otatoy.api.customer.search;

import com.ryuqq.otatoy.api.core.ErrorMapperRegistry;
import com.ryuqq.otatoy.api.core.GlobalExceptionHandler;
import com.ryuqq.otatoy.application.property.dto.result.CustomerPropertySliceResult;
import com.ryuqq.otatoy.application.property.dto.result.PropertySummary;
import com.ryuqq.otatoy.application.property.port.in.CustomerSearchPropertyUseCase;
import com.ryuqq.otatoy.domain.common.query.SliceMeta;
import com.ryuqq.otatoy.domain.common.vo.Money;
import com.ryuqq.otatoy.domain.property.Location;
import com.ryuqq.otatoy.domain.property.PropertyId;
import com.ryuqq.otatoy.domain.property.PropertyName;
import com.ryuqq.otatoy.domain.propertytype.PropertyTypeId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SearchPropertyController.class)
@Import({GlobalExceptionHandler.class, ErrorMapperRegistry.class})
class SearchPropertyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CustomerSearchPropertyUseCase customerSearchPropertyUseCase;

    @Test
    @DisplayName("숙소 검색 성공 시 200 응답")
    void 숙소_검색_성공시_200_응답() throws Exception {
        // given
        PropertySummary summary = new PropertySummary(
                PropertyId.of(1L),
                PropertyName.of("서울 호텔"),
                PropertyTypeId.of(1L),
                Location.of("서울시 강남구", 37.5, 127.0, "강남", "서울"),
                Money.of(BigDecimal.valueOf(100000))
        );

        CustomerPropertySliceResult result = CustomerPropertySliceResult.of(
                List.of(summary), new SliceMeta(true, 2L));

        given(customerSearchPropertyUseCase.execute(any())).willReturn(result);

        // when & then
        mockMvc.perform(get("/api/v1/search/properties")
                        .param("checkIn", "2026-05-01")
                        .param("checkOut", "2026-05-02")
                        .param("guests", "2")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].propertyId").value(1))
                .andExpect(jsonPath("$.data.content[0].name").value("서울 호텔"))
                .andExpect(jsonPath("$.data.content[0].lowestPrice").value(100000))
                .andExpect(jsonPath("$.data.hasNext").value(true))
                .andExpect(jsonPath("$.data.nextCursor").value(2));
    }

    @Test
    @DisplayName("검색 결과가 비어있으면 빈 목록 반환")
    void 검색_결과_없으면_빈_목록_반환() throws Exception {
        given(customerSearchPropertyUseCase.execute(any()))
                .willReturn(CustomerPropertySliceResult.empty());

        mockMvc.perform(get("/api/v1/search/properties")
                        .param("checkIn", "2026-05-01")
                        .param("checkOut", "2026-05-02")
                        .param("guests", "2")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isEmpty())
                .andExpect(jsonPath("$.data.hasNext").value(false));
    }

    @Test
    @DisplayName("체크인 날짜 누락 시 400 응답")
    void 체크인_날짜_누락시_400_응답() throws Exception {
        mockMvc.perform(get("/api/v1/search/properties")
                        .param("checkOut", "2026-05-02")
                        .param("guests", "2")
                        .param("size", "10"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("체크아웃 날짜 누락 시 400 응답")
    void 체크아웃_날짜_누락시_400_응답() throws Exception {
        mockMvc.perform(get("/api/v1/search/properties")
                        .param("checkIn", "2026-05-01")
                        .param("guests", "2")
                        .param("size", "10"))
                .andExpect(status().isBadRequest());
    }
}
