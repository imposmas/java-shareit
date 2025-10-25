package ru.practicum.shareit.request.storage;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.User;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class InMemoryItemRequestRepository {

    private final Map<Long, ItemRequest> requests = new HashMap<>();
    private long currentId = 0;

    private Long getNextId() {
        return ++currentId;
    }

    public ItemRequest save(ItemRequest request) {
        if (request.getId() == null) {
            request.setId(getNextId());
        }
        requests.put(request.getId(), request);
        return request;
    }

    public ItemRequest update(ItemRequest request) {
        Long id = request.getId();
        if (!requests.containsKey(id)) {
            throw new NoSuchElementException("ItemRequest with id=" + id + " not found");
        }
        requests.put(id, request);
        return request;
    }

    public Optional<ItemRequest> findById(Long id) {
        return Optional.ofNullable(requests.get(id));
    }

    public List<ItemRequest> findAll() {
        return new ArrayList<>(requests.values());
    }

    public void deleteById(Long id) {
        requests.remove(id);
    }

    public List<ItemRequest> findAllByRequestor(User requestor) {
        return requests.values().stream()
                .filter(req -> req.getRequestor() != null && req.getRequestor().equals(requestor))
                .collect(Collectors.toList());
    }
}