package com.roommates.user;

import java.util.List;
import java.util.Optional;

public interface RoleRepository {
    Role save(Role role);

    Optional<Role> findByName(String name);

    List<Role> findAll();
}
