package com.roommates.user;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    User save(User user);
    Optional<User> findById(Long id);
    List<User> findAll();
    User update(User user);
    void delete(User user);

    List<User> findByEnabled(Boolean enabled);

    Optional<User> findByUsername(String username);

    Optional<User> findByUsernameWithRoles(String username);

    Optional<User> findByEmail(String email);

    void flush();
}
