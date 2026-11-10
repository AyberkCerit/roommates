package com.roommates.user;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.EntityManager;

import org.springframework.stereotype.Repository;

@Repository
public class RoleRepositoryImpl implements RoleRepository {
    private final EntityManager entityManager;

    public RoleRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Role save(Role role) {
        entityManager.persist(role);
        return role;
    }

    @Override
    public Optional<Role> findByName(String name) {
        List<Role> bulunanlar = entityManager
                .createQuery("SELECT r FROM Role r WHERE r.name = :name", Role.class)
                .setParameter("name", name)
                .getResultList();

        if (bulunanlar.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(bulunanlar.get(0));
    }

    @Override
    public List<Role> findAll() {
        return entityManager
                .createQuery("SELECT r FROM Role r ORDER BY r.id", Role.class)
                .getResultList();
    }
}
