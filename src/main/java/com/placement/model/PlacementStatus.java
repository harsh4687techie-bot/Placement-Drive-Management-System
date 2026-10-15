package com.placement.model;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public enum PlacementStatus {
    REGISTERED,
    SHORTLISTED,
    INTERVIEW,
    SELECTED,
    REJECTED,
    WITHDRAWN;

    public boolean canTransitionTo(PlacementStatus target) {
        if (target == null || target == this) {
            return false;
        }
        return getAllowedTransitions().contains(target);
    }

    public Set<PlacementStatus> getAllowedTransitions() {
        return switch (this) {
            case REGISTERED -> EnumSet.of(SHORTLISTED, REJECTED, WITHDRAWN);
            case SHORTLISTED -> EnumSet.of(INTERVIEW, REJECTED, WITHDRAWN);
            case INTERVIEW -> EnumSet.of(SELECTED, REJECTED, WITHDRAWN);
            case SELECTED -> EnumSet.of(WITHDRAWN);
            case REJECTED, WITHDRAWN -> Collections.emptySet();
        };
    }
}
