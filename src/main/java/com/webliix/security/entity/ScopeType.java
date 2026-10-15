package com.webliix.security.entity;

public enum ScopeType {
    OWN,
    DEPARTMENT,
    ALL;

    public boolean isBroaderOrEqualTo(ScopeType other) {
        if (this == ALL) return true;
        if (this == DEPARTMENT && (other == DEPARTMENT || other == OWN)) return true;
        return this == other;
    }
}
