package ru.practicum.shareit.item.storage;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class InMemoryItemRepository {
    private final Map<Long, Item> items = new HashMap<>();
    private long currentId = 0;

    private Long getNextId() {
        return ++currentId;
    }

    public Item save(Item item) {
        if (item.getId() == null) {
            item.setId(getNextId());
        }
        items.put(item.getId(), item);
        return item;
    }

    public Item update(Item item) {
        Long id = item.getId();
        if (!items.containsKey(id)) {
            throw new NoSuchElementException("Item with id=" + id + " not found");
        }
        items.put(id, item);
        return item;
    }

    public Optional<Item> findById(Long id) {
        return Optional.ofNullable(items.get(id));
    }

    public List<Item> findAll() {
        return new ArrayList<>(items.values());
    }

    public void deleteById(Long id) {
        items.remove(id);
    }

    public List<Item> findAllByOwner(User owner) {
        return items.values().stream()
                .filter(item -> item.getOwner() != null && item.getOwner().equals(owner))
                .collect(Collectors.toList());
    }

    public List<Item> searchAvailableByText(String text) {
        if (text == null || text.isBlank()) return List.of();
        String lower = text.toLowerCase();
        return items.values().stream()
                .filter(Item::getAvailable)
                .filter(item -> item.getName().toLowerCase().contains(lower)
                        || item.getDescription().toLowerCase().contains(lower))
                .collect(Collectors.toList());
    }
}