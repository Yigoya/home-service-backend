package com.home.service.models.enums;

public enum BookingStatus {
    PENDING(1),
    ACCEPTED(2),
    CUSTOMER_STARTED(3),
    TECHNICIAN_STARTED(4),
    STARTED(5),
    CONFIRMED(6),
    DENIED(7),
    COMPLETED(8),
    CANCELED(7);

    private final int priority;

    BookingStatus(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }
}
