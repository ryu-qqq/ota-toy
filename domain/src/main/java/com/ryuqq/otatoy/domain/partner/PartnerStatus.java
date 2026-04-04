package com.ryuqq.otatoy.domain.partner;

import java.util.Map;
import java.util.Set;

public enum PartnerStatus {

    ACTIVE("활성"),
    SUSPENDED("정지");

    private static final Map<PartnerStatus, Set<PartnerStatus>> TRANSITIONS = Map.of(
            ACTIVE, Set.of(SUSPENDED),
            SUSPENDED, Set.of(ACTIVE)
    );

    private final String displayName;

    PartnerStatus(String displayName) {
        this.displayName = displayName;
    }

    public boolean canTransitTo(PartnerStatus target) {
        return TRANSITIONS.getOrDefault(this, Set.of()).contains(target);
    }

    public PartnerStatus transitTo(PartnerStatus target) {
        if (!canTransitTo(target)) {
            if (this == target && this == SUSPENDED) {
                throw new PartnerAlreadySuspendedException();
            }
            if (this == target && this == ACTIVE) {
                throw new PartnerAlreadyActiveException();
            }
            throw new IllegalStateException(
                    "허용되지 않는 파트너 상태 전이입니다: " + this + " → " + target);
        }
        return target;
    }

    public String displayName() {
        return displayName;
    }
}
