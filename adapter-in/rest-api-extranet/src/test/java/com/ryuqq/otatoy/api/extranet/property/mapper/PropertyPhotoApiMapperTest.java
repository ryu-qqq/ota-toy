package com.ryuqq.otatoy.api.extranet.property.mapper;

import com.ryuqq.otatoy.api.extranet.property.dto.request.SetPropertyPhotosApiRequest;
import com.ryuqq.otatoy.application.property.dto.command.SetPropertyPhotosCommand;
import com.ryuqq.otatoy.domain.accommodation.PhotoType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * PropertyPhotoApiMapper 단위 테스트.
 * 사진 설정 Request -> Command 변환 정확성을 검증한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@DisplayName("PropertyPhotoApiMapper")
class PropertyPhotoApiMapperTest {

    @Nested
    @DisplayName("toCommand")
    class ToCommand {

        @Test
        @DisplayName("propertyId와 사진 목록이 정확하게 변환된다")
        void shouldMapPropertyIdAndPhotos() {
            // given
            var request = new SetPropertyPhotosApiRequest(
                List.of(
                    new SetPropertyPhotosApiRequest.PhotoItem(
                        "EXTERIOR", "https://example.com/exterior.jpg",
                        "https://cdn.example.com/exterior.jpg", 1
                    ),
                    new SetPropertyPhotosApiRequest.PhotoItem(
                        "LOBBY", "https://example.com/lobby.jpg",
                        null, 2
                    )
                )
            );

            // when
            SetPropertyPhotosCommand command = PropertyPhotoApiMapper.toCommand(10L, request);

            // then
            assertThat(command.propertyId().value()).isEqualTo(10L);
            assertThat(command.photos()).hasSize(2);
        }

        @Test
        @DisplayName("PhotoType 문자열이 Enum으로 정확하게 변환된다")
        void shouldConvertPhotoTypeStringToEnum() {
            var request = new SetPropertyPhotosApiRequest(
                List.of(
                    new SetPropertyPhotosApiRequest.PhotoItem(
                        "EXTERIOR", "https://example.com/photo.jpg", null, 0
                    )
                )
            );

            SetPropertyPhotosCommand command = PropertyPhotoApiMapper.toCommand(1L, request);

            assertThat(command.photos().get(0).photoType()).isEqualTo(PhotoType.EXTERIOR);
        }

        @Test
        @DisplayName("originUrl이 OriginUrl VO로 정확하게 변환된다")
        void shouldConvertOriginUrlToVo() {
            var request = new SetPropertyPhotosApiRequest(
                List.of(
                    new SetPropertyPhotosApiRequest.PhotoItem(
                        "ROOM", "https://example.com/room.jpg", null, 0
                    )
                )
            );

            SetPropertyPhotosCommand command = PropertyPhotoApiMapper.toCommand(1L, request);

            assertThat(command.photos().get(0).originUrl().value())
                .isEqualTo("https://example.com/room.jpg");
        }

        @Nested
        @DisplayName("cdnUrl nullable 처리")
        class CdnUrlNullable {

            @Test
            @DisplayName("cdnUrl이 있으면 CdnUrl VO로 변환된다")
            void shouldConvertCdnUrlToVoWhenPresent() {
                var request = new SetPropertyPhotosApiRequest(
                    List.of(
                        new SetPropertyPhotosApiRequest.PhotoItem(
                            "EXTERIOR", "https://example.com/photo.jpg",
                            "https://cdn.example.com/photo.jpg", 1
                        )
                    )
                );

                SetPropertyPhotosCommand command = PropertyPhotoApiMapper.toCommand(1L, request);

                assertThat(command.photos().get(0).cdnUrl().value())
                    .isEqualTo("https://cdn.example.com/photo.jpg");
            }

            @Test
            @DisplayName("cdnUrl이 null이면 Command의 cdnUrl도 null이다")
            void shouldMapNullCdnUrlToNull() {
                var request = new SetPropertyPhotosApiRequest(
                    List.of(
                        new SetPropertyPhotosApiRequest.PhotoItem(
                            "LOBBY", "https://example.com/lobby.jpg", null, 2
                        )
                    )
                );

                SetPropertyPhotosCommand command = PropertyPhotoApiMapper.toCommand(1L, request);

                assertThat(command.photos().get(0).cdnUrl()).isNull();
            }
        }

        @Test
        @DisplayName("sortOrder가 그대로 전달된다")
        void shouldPassSortOrderAsIs() {
            var request = new SetPropertyPhotosApiRequest(
                List.of(
                    new SetPropertyPhotosApiRequest.PhotoItem(
                        "ROOM", "https://example.com/room.jpg", null, 5
                    )
                )
            );

            SetPropertyPhotosCommand command = PropertyPhotoApiMapper.toCommand(1L, request);

            assertThat(command.photos().get(0).sortOrder()).isEqualTo(5);
        }
    }
}
