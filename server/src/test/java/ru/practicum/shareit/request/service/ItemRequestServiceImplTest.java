package ru.practicum.shareit.request.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.common.exceptions.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.storage.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ItemRequestServiceImplTest {

    @Autowired
    private ItemRequestService requestService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRequestRepository requestRepository;

    @Autowired
    private ItemRepository itemRepository;

    private User user;
    private ItemRequest request;
    private Item item;

    @BeforeEach
    void setup() {
        user = new User();
        user.setName("Alice");
        user.setEmail("alice@example.com");
        user = userRepository.save(user);

        request = new ItemRequest();
        request.setDescription("Need a laptop");
        request.setRequestor(user);
        request.setCreated(LocalDateTime.now());
        request = requestRepository.save(request);

        item = new Item();
        item.setName("Laptop");
        item.setDescription("Gaming laptop");
        item.setAvailable(true);
        item.setOwner(user);
        item.setRequest(request);
        item = itemRepository.save(item);
    }

    @Test
    void getRequestById_shouldReturnRequestWithItems() {
        ItemRequestDto dto = requestService.getRequestById(user.getId(), request.getId());

        assertNotNull(dto);
        assertEquals(request.getDescription(), dto.getDescription());
        assertNotNull(dto.getItems());
        assertEquals(1, dto.getItems().size());
        assertEquals(item.getName(), dto.getItems().get(0).getName());
    }

    @Test
    void getRequestById_shouldThrowNotFoundException_whenUserNotFound() {
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> requestService.getRequestById(999L, request.getId()));

        assertTrue(ex.getMessage().contains("Пользователь не найден"));
    }

    @Test
    void getRequestById_shouldThrowNotFoundException_whenRequestNotFound() {
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> requestService.getRequestById(user.getId(), 999L));

        assertTrue(ex.getMessage().contains("Запрос не найден"));
    }
}