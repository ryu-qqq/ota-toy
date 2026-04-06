package com.ryuqq.otatoy.application.property.factory;

import com.ryuqq.otatoy.application.common.factory.TimeProvider;
import com.ryuqq.otatoy.application.property.dto.command.SetPropertyPhotosCommand;
import com.ryuqq.otatoy.application.property.dto.command.SetPropertyPhotosCommandFixture;
import com.ryuqq.otatoy.domain.accommodation.PhotoType;
import com.ryuqq.otatoy.domain.common.vo.CdnUrl;
import com.ryuqq.otatoy.domain.common.vo.OriginUrl;
import com.ryuqq.otatoy.domain.property.PropertyId;
import com.ryuqq.otatoy.domain.property.PropertyPhoto;
import com.ryuqq.otatoy.domain.property.PropertyPhotos;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

/**
 * PropertyPhotoFactory 단위 테스트.
 * TimeProvider를 Mock으로 대체하여 시간 제어 및 도메인 객체 생성을 검증한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@ExtendWith(MockitoExtension.class)
class PropertyPhotoFactoryTest {

    @Mock
    TimeProvider timeProvider;

    @InjectMocks
    PropertyPhotoFactory factory;

    private static final Instant FIXED_NOW = Instant.parse("2026-04-06T10:00:00Z");

    @Nested
    @DisplayName("정상 흐름")
    class Success {

        @Test
        @DisplayName("Command의 사진 항목이 PropertyPhotos로 올바르게 변환된다")
        void shouldCreatePhotosFromCommand() {
            // given
            given(timeProvider.now()).willReturn(FIXED_NOW);
            SetPropertyPhotosCommand command = SetPropertyPhotosCommandFixture.aSetPropertyPhotosCommand();

            // when
            PropertyPhotos result = factory.createPhotos(command);

            // then
            assertThat(result.size()).isEqualTo(2);
            List<PropertyPhoto> items = result.items();
            assertThat(items.get(0).propertyId()).isEqualTo(PropertyId.of(1L));
            assertThat(items.get(0).photoType()).isEqualTo(PhotoType.EXTERIOR);
            assertThat(items.get(0).originUrl()).isEqualTo(OriginUrl.of("https://origin.com/1.jpg"));
            assertThat(items.get(0).cdnUrl()).isEqualTo(CdnUrl.of("https://cdn.com/1.jpg"));
            assertThat(items.get(0).sortOrder()).isEqualTo(1);

            assertThat(items.get(1).photoType()).isEqualTo(PhotoType.LOBBY);
            assertThat(items.get(1).sortOrder()).isEqualTo(2);
        }

        @Test
        @DisplayName("TimeProvider에서 제공한 시간이 모든 사진 항목에 설정된다")
        void shouldUseTimeProviderForTimestamps() {
            // given
            given(timeProvider.now()).willReturn(FIXED_NOW);
            SetPropertyPhotosCommand command = SetPropertyPhotosCommandFixture.aSetPropertyPhotosCommand();

            // when
            PropertyPhotos result = factory.createPhotos(command);

            // then
            result.items().forEach(item ->
                assertThat(item.createdAt()).isEqualTo(FIXED_NOW)
            );
        }

        @Test
        @DisplayName("빈 사진 목록이면 빈 PropertyPhotos가 생성된다")
        void shouldCreateEmptyPhotosWhenEmptyList() {
            // given
            given(timeProvider.now()).willReturn(FIXED_NOW);
            SetPropertyPhotosCommand command = SetPropertyPhotosCommand.of(
                PropertyId.of(1L), List.of()
            );

            // when
            PropertyPhotos result = factory.createPhotos(command);

            // then
            assertThat(result.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("모든 사진 항목에 동일한 propertyId가 설정된다")
        void shouldSetSamePropertyIdForAllItems() {
            // given
            given(timeProvider.now()).willReturn(FIXED_NOW);
            SetPropertyPhotosCommand command = SetPropertyPhotosCommandFixture.aSetPropertyPhotosCommand();

            // when
            PropertyPhotos result = factory.createPhotos(command);

            // then
            result.items().forEach(item ->
                assertThat(item.propertyId()).isEqualTo(PropertyId.of(1L))
            );
        }
    }
}
