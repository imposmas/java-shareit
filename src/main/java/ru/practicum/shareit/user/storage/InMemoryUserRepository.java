package ru.practicum.shareit.user.storage;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.user.User;

import java.util.*;

@Repository
public class InMemoryUserRepository {
    private final Map<Long, User> users = new HashMap<>();
    private long currentId = 0;

    private Long getNextId() {
        return ++currentId;
    }

    public User save(User user) {
        if (user.getId() == null) {
            user.setId(getNextId());
        }
        users.put(user.getId(), user);
        return user;
    }

    public User update(User user) {
        Long id = user.getId();
        if (!users.containsKey(id)) {
            throw new NoSuchElementException("User with id=" + id + " not found");
        }
        users.put(id, user);
        return user;
    }

    public Optional<User> findById(Long id) {
        return Optional.ofNullable(users.get(id));
    }

    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    public void deleteById(Long id) {
        users.remove(id);
    }

    public Optional<User> findByEmail(String email) {
        return users.values().stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(email))
                .findFirst();
    }
}
