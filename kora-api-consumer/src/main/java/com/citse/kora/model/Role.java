package com.citse.kora.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.citse.kora.model.Permission.*;

/** Mismos roles y permisos que Kundu API (la columna tsg_users.role guarda el nombre). */
@Getter
@RequiredArgsConstructor
public enum Role {

    USER(Collections.emptySet()),
    TUTOR(Set.of(USER_READ, TUTOR_UPDATE, TUTOR_CREATE, TUTOR_DELETE)),
    ADMIN(Set.of(ADMIN_READ, ADMIN_UPDATE, ADMIN_DELETE, ADMIN_CREATE)),
    DEVELOPER(Set.of(ADMIN_READ, ADMIN_UPDATE, ADMIN_DELETE, ADMIN_CREATE, TUTOR_DELETE));

    private final Set<Permission> permissions;

    public List<String> getAuthorities() {
        return getPermissions()
                .stream()
                .map(Permission::getPermission)
                .collect(Collectors.toList());
    }
}
