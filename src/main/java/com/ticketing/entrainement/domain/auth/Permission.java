package com.ticketing.entrainement.domain.auth;

public enum Permission {
    TICKET_READ("ticket:read"),
    TICKET_CREATE("ticket:create"),
    TICKET_UPDATE("ticket:update"),
    TICKET_DELETE("ticket:delete");

    private final String code;

    Permission(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }
}
