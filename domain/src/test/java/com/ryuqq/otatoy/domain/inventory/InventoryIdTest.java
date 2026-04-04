package com.ryuqq.otatoy.domain.inventory;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InventoryIdTest {

    @Test
    @DisplayName("of()로 생성한 InventoryId와 생성자로 생성한 것이 동등하다")
    void shouldBeEqualWhenCreatedByOf() {
        InventoryId a = InventoryId.of(1L);
        InventoryId b = new InventoryId(1L);

        assertThat(a).isEqualTo(b);
        assertThat(a.value()).isEqualTo(1L);
    }

    @Test
    @DisplayName("null value로 생성 시 isNew는 true이다")
    void shouldBeNewWhenValueIsNull() {
        InventoryId id = InventoryId.of(null);

        assertThat(id.isNew()).isTrue();
    }

    @Test
    @DisplayName("non-null value로 생성 시 isNew는 false이다")
    void shouldNotBeNewWhenValueExists() {
        InventoryId id = InventoryId.of(1L);

        assertThat(id.isNew()).isFalse();
    }

    @Test
    @DisplayName("같은 value를 가진 두 InventoryId는 동등하다 (Record)")
    void shouldBeEqualForSameValue() {
        InventoryId a = InventoryId.of(42L);
        InventoryId b = InventoryId.of(42L);

        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    @Test
    @DisplayName("다른 value를 가진 두 InventoryId는 동등하지 않다")
    void shouldNotBeEqualForDifferentValue() {
        InventoryId a = InventoryId.of(1L);
        InventoryId b = InventoryId.of(2L);

        assertThat(a).isNotEqualTo(b);
    }
}
