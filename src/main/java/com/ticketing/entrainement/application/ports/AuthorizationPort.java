package com.ticketing.entrainement.application.ports;

import com.ticketing.entrainement.domain.auth.Permission;

public interface AuthorizationPort {
    void check(Permission permission);
}
