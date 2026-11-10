package com.roommates.user;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.EntityManager;

import org.springframework.stereotype.Repository;

@Repository
public class UserRepositoryImpl implements UserRepository {
    private final EntityManager entityManager;

    public UserRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public User save(User user) {
        entityManager.persist(user);
        return user;
    }

    @Override
    public Optional<User> findById(Long id) {
        User kullanici = entityManager.find(User.class, id);
        return Optional.ofNullable(kullanici);
    }

    @Override
    public List<User> findAll() {
        return entityManager
                .createQuery("SELECT u FROM User u ORDER BY u.id", User.class)
                .getResultList();
    }

    @Override
    public User update(User user) {
        return entityManager.merge(user);
    }

    @Override
    public void delete(User user) {
        entityManager.remove(user);
    }

    @Override
    public List<User> findByEnabled(Boolean enabled) {
        return entityManager
                .createQuery("SELECT u FROM User u "
                        + "WHERE (:enabled IS NULL OR u.enabled = :enabled) "
                        + "ORDER BY u.id", User.class)
                .setParameter("enabled", enabled)
                .getResultList();
    }

    @Override
    public Optional<User> findByUsername(String username) {
        List<User> bulunanlar = entityManager
                .createQuery("SELECT u FROM User u WHERE u.username = :username", User.class)
                .setParameter("username", username)
                .getResultList();
        return ilkiniDondur(bulunanlar);
    }

    @Override
    public Optional<User> findByUsernameWithRoles(String username) {
        List<User> bulunanlar = entityManager
                .createQuery("SELECT u FROM User u LEFT JOIN FETCH u.roles "
                        + "WHERE u.username = :username", User.class)
                .setParameter("username", username)
                .getResultList();
        return ilkiniDondur(bulunanlar);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        List<User> bulunanlar = entityManager
                .createQuery("SELECT u FROM User u WHERE u.email = :email", User.class)
                .setParameter("email", email)
                .getResultList();
        return ilkiniDondur(bulunanlar);
    }

    @Override
    public void flush() {
        entityManager.flush();
    }

    private Optional<User> ilkiniDondur(List<User> liste) {
        if (liste.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(liste.get(0));
    }
}
