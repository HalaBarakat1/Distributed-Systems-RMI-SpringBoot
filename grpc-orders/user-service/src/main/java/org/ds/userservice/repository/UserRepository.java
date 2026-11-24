package org.ds.userservice.repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.ds.userservice.model.User;
import org.springframework.stereotype.Repository;

/**
 * In-memory store of users, seeded with sample data so the
 * service can be tested without any database.
 */
@Repository
public class UserRepository {

    private final Map<Long, User> users = new ConcurrentHashMap<>();

    public UserRepository() {
        save(new User(101, "Ahmed", "ahmed@example.com"));
        save(new User(102, "Sara", "sara@example.com"));
        save(new User(103, "Omar", "omar@example.com"));
    }

    public Optional<User> findById(long id) {
        return Optional.ofNullable(users.get(id));
    }

    public User save(User user) {
        users.put(user.id(), user);
        return user;
    }
}
