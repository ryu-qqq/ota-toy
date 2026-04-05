package com.ryuqq.otatoy.application.property.service;

import com.ryuqq.otatoy.application.property.dto.result.PropertyDetail;
import com.ryuqq.otatoy.application.property.manager.PropertyAmenityReadManager;
import com.ryuqq.otatoy.application.property.manager.PropertyAttributeValueReadManager;
import com.ryuqq.otatoy.application.property.manager.PropertyPhotoReadManager;
import com.ryuqq.otatoy.application.property.manager.PropertyReadManager;
import com.ryuqq.otatoy.application.roomtype.manager.RoomTypeReadManager;
import com.ryuqq.otatoy.domain.brand.BrandId;
import com.ryuqq.otatoy.domain.partner.PartnerId;
import com.ryuqq.otatoy.domain.property.Location;
import com.ryuqq.otatoy.domain.property.Property;
import com.ryuqq.otatoy.domain.property.PropertyAmenities;
import com.ryuqq.otatoy.domain.property.PropertyAttributeValues;
import com.ryuqq.otatoy.domain.property.PropertyDescription;
import com.ryuqq.otatoy.domain.property.PropertyId;
import com.ryuqq.otatoy.domain.property.PropertyName;
import com.ryuqq.otatoy.domain.property.PropertyNotFoundException;
import com.ryuqq.otatoy.domain.property.PropertyPhotos;
import com.ryuqq.otatoy.domain.property.PropertyStatus;
import com.ryuqq.otatoy.domain.propertytype.PropertyTypeId;
import com.ryuqq.otatoy.domain.roomtype.RoomType;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeDescription;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeId;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeName;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeStatus;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class GetPropertyDetailServiceTest {

    @Mock
    PropertyReadManager propertyReadManager;

    @Mock
    PropertyPhotoReadManager photoReadManager;

    @Mock
    PropertyAmenityReadManager amenityReadManager;

    @Mock
    PropertyAttributeValueReadManager attributeValueReadManager;

    @Mock
    RoomTypeReadManager roomTypeReadManager;

    @InjectMocks
    GetPropertyDetailService service;

    @Nested
    @DisplayName("정상 흐름")
    class Success {

        @Test
        @DisplayName("여러 ReadManager를 조합하여 PropertyDetail을 반환한다")
        void shouldAssemblePropertyDetailFromMultipleManagers() {
            // given
            PropertyId propertyId = PropertyId.of(1L);
            Property property = createProperty(1L);
            PropertyPhotos photos = PropertyPhotos.reconstitute(List.of());
            PropertyAmenities amenities = PropertyAmenities.reconstitute(List.of());
            PropertyAttributeValues attributeValues = PropertyAttributeValues.reconstitute(List.of());
            RoomType roomType = createRoomType(1L, 1L);

            given(propertyReadManager.getById(propertyId)).willReturn(property);
            given(photoReadManager.getByPropertyId(propertyId)).willReturn(photos);
            given(amenityReadManager.getByPropertyId(propertyId)).willReturn(amenities);
            given(attributeValueReadManager.getByPropertyId(propertyId)).willReturn(attributeValues);
            given(roomTypeReadManager.findByPropertyId(propertyId)).willReturn(List.of(roomType));

            // when
            PropertyDetail result = service.execute(propertyId);

            // then
            assertThat(result.property()).isEqualTo(property);
            assertThat(result.photos()).isEqualTo(photos);
            assertThat(result.amenities()).isEqualTo(amenities);
            assertThat(result.attributeValues()).isEqualTo(attributeValues);
            assertThat(result.roomTypes()).hasSize(1);
        }

        @Test
        @DisplayName("객실이 없어도 빈 리스트로 정상 반환한다")
        void shouldReturnEmptyRoomTypesWhenNone() {
            // given
            PropertyId propertyId = PropertyId.of(1L);
            Property property = createProperty(1L);

            given(propertyReadManager.getById(propertyId)).willReturn(property);
            given(photoReadManager.getByPropertyId(propertyId))
                    .willReturn(PropertyPhotos.reconstitute(List.of()));
            given(amenityReadManager.getByPropertyId(propertyId))
                    .willReturn(PropertyAmenities.reconstitute(List.of()));
            given(attributeValueReadManager.getByPropertyId(propertyId))
                    .willReturn(PropertyAttributeValues.reconstitute(List.of()));
            given(roomTypeReadManager.findByPropertyId(propertyId)).willReturn(List.of());

            // when
            PropertyDetail result = service.execute(propertyId);

            // then
            assertThat(result.roomTypes()).isEmpty();
        }
    }

    @Nested
    @DisplayName("예외 흐름")
    class ExceptionCase {

        @Test
        @DisplayName("숙소가 없으면 PropertyNotFoundException이 발생한다")
        void shouldThrowWhenPropertyNotFound() {
            // given
            PropertyId propertyId = PropertyId.of(999L);
            given(propertyReadManager.getById(propertyId))
                    .willThrow(new PropertyNotFoundException());

            // when & then
            assertThatThrownBy(() -> service.execute(propertyId))
                    .isInstanceOf(PropertyNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("ReadManager 호출 검증")
    class ManagerCallVerification {

        @Test
        @DisplayName("모든 ReadManager를 propertyId로 호출한다")
        void shouldCallAllReadManagers() {
            // given
            PropertyId propertyId = PropertyId.of(1L);
            given(propertyReadManager.getById(propertyId)).willReturn(createProperty(1L));
            given(photoReadManager.getByPropertyId(propertyId))
                    .willReturn(PropertyPhotos.reconstitute(List.of()));
            given(amenityReadManager.getByPropertyId(propertyId))
                    .willReturn(PropertyAmenities.reconstitute(List.of()));
            given(attributeValueReadManager.getByPropertyId(propertyId))
                    .willReturn(PropertyAttributeValues.reconstitute(List.of()));
            given(roomTypeReadManager.findByPropertyId(propertyId)).willReturn(List.of());

            // when
            service.execute(propertyId);

            // then
            then(propertyReadManager).should().getById(propertyId);
            then(photoReadManager).should().getByPropertyId(propertyId);
            then(amenityReadManager).should().getByPropertyId(propertyId);
            then(attributeValueReadManager).should().getByPropertyId(propertyId);
            then(roomTypeReadManager).should().findByPropertyId(propertyId);
        }
    }

    private Property createProperty(Long id) {
        return Property.reconstitute(
                PropertyId.of(id),
                PartnerId.of(1L),
                BrandId.of(10L),
                PropertyTypeId.of(1L),
                PropertyName.of("테스트 호텔"),
                PropertyDescription.of("테스트 설명"),
                Location.of("서울시 강남구", 37.5665, 126.978, "강남", "서울"),
                null,
                PropertyStatus.ACTIVE,
                Instant.now(),
                Instant.now()
        );
    }

    private RoomType createRoomType(Long id, Long propertyId) {
        return RoomType.reconstitute(
                RoomTypeId.of(id),
                PropertyId.of(propertyId),
                RoomTypeName.of("스탠다드 룸"),
                RoomTypeDescription.of("기본 객실"),
                BigDecimal.valueOf(30),
                "9평",
                2, 4, 10,
                LocalTime.of(15, 0),
                LocalTime.of(11, 0),
                RoomTypeStatus.ACTIVE,
                Instant.now(),
                Instant.now()
        );
    }
}
