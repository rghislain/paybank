package com.paybank.hexagonal.domaine;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public enum Role {
    ADMIN(Set.of(
        Permission.USER_CREATE, Permission.USER_UPDATE, Permission.USER_DELETE,
        Permission.CLIENT_CREATE, Permission.CLIENT_UPDATE, Permission.CLIENT_DELETE,
        Permission.ROLE_UPDATE
    )),
    MANAGER(Set.of(
        Permission.USER_CREATE, Permission.USER_UPDATE, // Limité au périmètre de son équipe
        Permission.CLIENT_CREATE, Permission.CLIENT_UPDATE
    )),
    EMPLOYE(Set.of(
        Permission.CLIENT_CREATE // Droits restreints aux opérations clients de base
    ));

    private final Set<Permission> permissions;

    Role(Set<Permission> permissions) {
        this.permissions = permissions;
    }

    public boolean hasPermission(Permission permission) {
        return this.permissions.contains(permission);
    }

	public String toUpperCase() {
		return this.name().toUpperCase();
	}

}