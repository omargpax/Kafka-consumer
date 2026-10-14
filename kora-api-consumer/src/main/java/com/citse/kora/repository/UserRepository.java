package com.citse.kora.repository;

import com.citse.kora.model.entity.User;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;

@ApplicationScoped
public class UserRepository implements PanacheRepositoryBase<User, Integer> {

    public Optional<User> findByUsername(String username) {
        return find("username", username).firstResultOptional();
    }

    /** Comparacion sin distinguir mayusculas/minusculas. */
    public boolean existsByEmail(String email) {
        return count("from User u where lower(u.email) = ?1", email.toLowerCase()) > 0;
    }
}
