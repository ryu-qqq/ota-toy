package com.ryuqq.otatoy.domain.common.query;

import com.ryuqq.otatoy.domain.common.sort.SortDirection;
import com.ryuqq.otatoy.domain.common.sort.SortKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class QueryTest {

    // 테스트용 SortKey 구현
    enum TestSortKey implements SortKey {
        NAME("name"),
        CREATED_AT("created_at");

        private final String fieldName;

        TestSortKey(String fieldName) {
            this.fieldName = fieldName;
        }

        @Override
        public String fieldName() {
            return fieldName;
        }
    }

    @Nested
    @DisplayName("PageRequest 검증")
    class PageRequestTest {

        @Test
        @DisplayName("유효한 값으로 생성할 수 있다")
        void shouldCreateWithValidValues() {
            PageRequest request = new PageRequest(0, 20);

            assertThat(request.page()).isEqualTo(0);
            assertThat(request.size()).isEqualTo(20);
        }

        @Test
        @DisplayName("offset을 계산할 수 있다")
        void shouldCalculateOffset() {
            PageRequest request = new PageRequest(2, 10);

            assertThat(request.offset()).isEqualTo(20);
        }

        @Test
        @DisplayName("page 0의 offset은 0이다")
        void firstPageOffsetShouldBeZero() {
            PageRequest request = new PageRequest(0, 10);

            assertThat(request.offset()).isEqualTo(0);
        }

        @Test
        @DisplayName("page가 음수이면 예외가 발생한다")
        void shouldThrowWhenPageNegative() {
            assertThatThrownBy(() -> new PageRequest(-1, 10))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("page는 0 이상");
        }

        @Test
        @DisplayName("size가 0이면 예외가 발생한다")
        void shouldThrowWhenSizeZero() {
            assertThatThrownBy(() -> new PageRequest(0, 0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("size는 1~100");
        }

        @Test
        @DisplayName("size가 101이면 예외가 발생한다")
        void shouldThrowWhenSizeOver100() {
            assertThatThrownBy(() -> new PageRequest(0, 101))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("size는 1~100");
        }

        @Test
        @DisplayName("size 경계값 1로 생성할 수 있다")
        void shouldCreateWithSizeOne() {
            PageRequest request = new PageRequest(0, 1);
            assertThat(request.size()).isEqualTo(1);
        }

        @Test
        @DisplayName("size 경계값 100으로 생성할 수 있다")
        void shouldCreateWithSize100() {
            PageRequest request = new PageRequest(0, 100);
            assertThat(request.size()).isEqualTo(100);
        }
    }

    @Nested
    @DisplayName("CursorPageRequest 검증")
    class CursorPageRequestTest {

        @Test
        @DisplayName("유효한 값으로 생성할 수 있다")
        void shouldCreateWithValidValues() {
            CursorPageRequest<Long> request = new CursorPageRequest<>(100L, 20);

            assertThat(request.cursor()).isEqualTo(100L);
            assertThat(request.size()).isEqualTo(20);
        }

        @Test
        @DisplayName("cursor가 null이면 첫 페이지 요청이다")
        void shouldAllowNullCursor() {
            CursorPageRequest<Long> request = new CursorPageRequest<>(null, 20);
            assertThat(request.cursor()).isNull();
        }

        @Test
        @DisplayName("size가 0이면 예외가 발생한다")
        void shouldThrowWhenSizeZero() {
            assertThatThrownBy(() -> new CursorPageRequest<>(null, 0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("size는 1~100");
        }

        @Test
        @DisplayName("size가 101이면 예외가 발생한다")
        void shouldThrowWhenSizeOver100() {
            assertThatThrownBy(() -> new CursorPageRequest<>(null, 101))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("size는 1~100");
        }
    }

    @Nested
    @DisplayName("PageMeta 검증")
    class PageMetaTest {

        @Test
        @DisplayName("페이지 메타 정보를 생성할 수 있다")
        void shouldCreatePageMeta() {
            PageMeta meta = new PageMeta(0, 10, 100, 10);

            assertThat(meta.page()).isEqualTo(0);
            assertThat(meta.size()).isEqualTo(10);
            assertThat(meta.totalElements()).isEqualTo(100);
            assertThat(meta.totalPages()).isEqualTo(10);
        }
    }

    @Nested
    @DisplayName("SliceMeta 검증")
    class SliceMetaTest {

        @Test
        @DisplayName("다음 페이지가 있는 슬라이스 메타를 생성할 수 있다")
        void shouldCreateWithHasNext() {
            SliceMeta meta = new SliceMeta(true, 100L);

            assertThat(meta.hasNext()).isTrue();
            assertThat(meta.nextCursor()).isEqualTo(100L);
        }

        @Test
        @DisplayName("마지막 페이지의 슬라이스 메타를 생성할 수 있다")
        void shouldCreateLastPage() {
            SliceMeta meta = new SliceMeta(false, null);

            assertThat(meta.hasNext()).isFalse();
            assertThat(meta.nextCursor()).isNull();
        }
    }

    @Nested
    @DisplayName("QueryContext 검증")
    class QueryContextTest {

        @Test
        @DisplayName("정렬 키, 방향, 크기, 커서로 생성할 수 있다")
        void shouldCreateWithAllFields() {
            QueryContext<TestSortKey> context = new QueryContext<>(
                    TestSortKey.NAME, SortDirection.ASC, 20, 100L);

            assertThat(context.sortKey()).isEqualTo(TestSortKey.NAME);
            assertThat(context.direction()).isEqualTo(SortDirection.ASC);
            assertThat(context.size()).isEqualTo(20);
            assertThat(context.cursor()).isEqualTo(100L);
        }
    }

    @Nested
    @DisplayName("SortDirection 검증")
    class SortDirectionTest {

        @Test
        @DisplayName("ASC, DESC 두 값이 존재한다")
        void shouldHaveTwoValues() {
            assertThat(SortDirection.values()).hasSize(2);
        }

        @Test
        @DisplayName("각 방향은 displayName을 반환한다")
        void shouldReturnDisplayName() {
            assertThat(SortDirection.ASC.displayName()).isEqualTo("오름차순");
            assertThat(SortDirection.DESC.displayName()).isEqualTo("내림차순");
        }
    }

    @Nested
    @DisplayName("SortKey 검증")
    class SortKeyTest {

        @Test
        @DisplayName("SortKey 구현체는 fieldName을 반환한다")
        void shouldReturnFieldName() {
            assertThat(TestSortKey.NAME.fieldName()).isEqualTo("name");
            assertThat(TestSortKey.CREATED_AT.fieldName()).isEqualTo("created_at");
        }
    }
}
