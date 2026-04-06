package com.ryuqq.otatoy.application.property.service;

import com.ryuqq.otatoy.application.property.assembler.PropertySearchResultAssembler;
import com.ryuqq.otatoy.application.property.dto.PropertyDetailBundle;
import com.ryuqq.otatoy.application.property.dto.result.PropertyDetail;
import com.ryuqq.otatoy.application.property.manager.PropertyDetailReadManager;
import com.ryuqq.otatoy.domain.property.*;
import com.ryuqq.otatoy.domain.roomtype.RoomTypes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class GetPropertyDetailServiceTest {

    @Mock PropertyDetailReadManager detailReadManager;
    @Mock PropertySearchResultAssembler assembler;
    @InjectMocks GetPropertyDetailService service;

    @Nested
    @DisplayName("정상 흐름")
    class Success {

        @Test
        @DisplayName("DetailReadManager → Assembler 순서로 호출하여 PropertyDetail 반환")
        void shouldReturnPropertyDetail() {
            var propertyId = PropertyId.of(1L);
            var bundle = new PropertyDetailBundle(
                null, PropertyPhotos.reconstitute(List.of()),
                PropertyAmenities.reconstitute(List.of()),
                PropertyAttributeValues.reconstitute(List.of()),
                RoomTypes.from(List.of())
            );
            var detail = PropertyDetail.of(null,
                PropertyPhotos.reconstitute(List.of()),
                PropertyAmenities.reconstitute(List.of()),
                PropertyAttributeValues.reconstitute(List.of()),
                RoomTypes.from(List.of())
            );

            given(detailReadManager.getById(propertyId)).willReturn(bundle);
            given(assembler.toDetail(bundle)).willReturn(detail);

            PropertyDetail result = service.execute(propertyId);

            assertThat(result).isEqualTo(detail);
        }
    }

    @Nested
    @DisplayName("실패 흐름")
    class Failure {

        @Test
        @DisplayName("PropertyNotFoundException 전파")
        void shouldPropagatePropertyNotFoundException() {
            var propertyId = PropertyId.of(999L);
            given(detailReadManager.getById(propertyId))
                .willThrow(new PropertyNotFoundException());

            assertThatThrownBy(() -> service.execute(propertyId))
                .isInstanceOf(PropertyNotFoundException.class);

            then(assembler).should(never()).toDetail(any());
        }
    }

    @Nested
    @DisplayName("호출 순서")
    class InvocationOrder {

        @Test
        @DisplayName("detailReadManager → assembler 순서")
        void shouldCallInOrder() {
            var propertyId = PropertyId.of(1L);
            var bundle = new PropertyDetailBundle(
                null, PropertyPhotos.reconstitute(List.of()),
                PropertyAmenities.reconstitute(List.of()),
                PropertyAttributeValues.reconstitute(List.of()),
                RoomTypes.from(List.of())
            );

            given(detailReadManager.getById(propertyId)).willReturn(bundle);
            given(assembler.toDetail(bundle)).willReturn(null);

            service.execute(propertyId);

            InOrder inOrder = inOrder(detailReadManager, assembler);
            inOrder.verify(detailReadManager).getById(propertyId);
            inOrder.verify(assembler).toDetail(bundle);
        }
    }
}
