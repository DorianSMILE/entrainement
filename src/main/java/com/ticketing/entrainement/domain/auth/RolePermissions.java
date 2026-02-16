package com.ticketing.entrainement.domain.auth;

import java.util.EnumSet;
import java.util.Set;

public final class RolePermissions {

    private RolePermissions() {}

    public static Set<Permission> permissionsOf(Role role) {
        return switch (role) {
            case ADMIN -> EnumSet.of(
                    Permission.TICKET_READ,
                    Permission.TICKET_CREATE,
                    Permission.TICKET_UPDATE,
                    Permission.TICKET_DELETE
            );
            case AGENT -> EnumSet.of(
                    Permission.TICKET_READ,
                    Permission.TICKET_CREATE,
                    Permission.TICKET_UPDATE
            );
            case VIEWER -> EnumSet.of(
                    Permission.TICKET_READ
            );
        };
    }
}
