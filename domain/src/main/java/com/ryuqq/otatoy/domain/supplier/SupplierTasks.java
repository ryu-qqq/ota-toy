package com.ryuqq.otatoy.domain.supplier;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * SupplierTask 일급 컬렉션.
 * 중복 Task 필터링, 진행 중 Task 식별 등 컬렉션 연산을 캡슐화한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public class SupplierTasks {

    private final List<SupplierTask> items;

    private SupplierTasks(List<SupplierTask> items) {
        this.items = items;
    }

    public static SupplierTasks from(List<SupplierTask> items) {
        if (items == null || items.isEmpty()) {
            return new SupplierTasks(List.of());
        }
        return new SupplierTasks(List.copyOf(items));
    }

    /**
     * 이미 진행 중(PENDING/PROCESSING)인 Task의 supplierId + taskType 조합을 추출한다.
     */
    public Set<String> inProgressKeys() {
        return items.stream()
                .map(task -> taskKey(task.supplierId(), task.taskType()))
                .collect(Collectors.toSet());
    }

    /**
     * 후보 Task 중에서 이미 진행 중인 것을 제외한 목록을 반환한다.
     * 같은 supplierId + taskType 조합이 이미 PENDING/PROCESSING이면 중복으로 판단.
     */
    public SupplierTasks excludeDuplicates(SupplierTasks candidates) {
        Set<String> existingKeys = this.inProgressKeys();
        List<SupplierTask> filtered = candidates.stream()
                .filter(task -> !existingKeys.contains(taskKey(task.supplierId(), task.taskType())))
                .toList();
        return SupplierTasks.from(filtered);
    }

    private static String taskKey(SupplierId supplierId, SupplierTaskType taskType) {
        return supplierId.value() + ":" + taskType.name();
    }

    public Stream<SupplierTask> stream() {
        return items.stream();
    }

    public List<SupplierTask> items() {
        return items;
    }

    public int size() {
        return items.size();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }
}
