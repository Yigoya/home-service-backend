package com.home.service.models.enums;

public enum BookingStatus {
    PENDING(1),
    ACCEPTED(2),
    STARTED(3),
    CONFIRMED(4),
    DENIED(5),
    COMPLETED(6),
    CANCELED(7);

    private final int priority;

    BookingStatus(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }
}
