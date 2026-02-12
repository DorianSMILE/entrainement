package com.ticketing.entrainement.infrastructure.security;

import com.ticketing.entrainement.application.exceptions.ForbiddenOperationException;
import com.ticketing.entrainement.application.ports.AuthorizationPort;
import com.ticketing.entrainement.domain.auth.Permission;
import com.ticketing.entrainement.domain.auth.Role;
import com.ticketing.entrainement.domain.auth.RolePermissions;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class SpringSecurityAuthorizationAdapter implements AuthorizationPort {

    @Override
    public void check(Permission permission) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            throw new ForbiddenOperationException("Authentication required");
        }

        Set<Role> roles = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> a.startsWith("ROLE_"))
                .map(a -> a.substring("ROLE_".length()))
                .map(String::toUpperCase)
                .map(Role::valueOf)
                .collect(Collectors.toSet());

        boolean allowed = roles.stream()
                .flatMap(r -> RolePermissions.permissionsOf(r).stream())
                .anyMatch(p -> p == permission);

        if (!allowed) {
            throw new ForbiddenOperationException("Missing permission: " + permission.code());
        }
    }
}
