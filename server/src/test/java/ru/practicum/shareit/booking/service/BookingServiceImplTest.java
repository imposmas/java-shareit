package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.constants.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.common.exceptions.NotFoundException;
import ru.practicum.shareit.common.exceptions.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BookingServiceImplTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    private User user;
    private User owner;
    private Item item;

    @BeforeEach
    void setup() {
        user = new User();
        user.setName("Alice");
        user.setEmail("alice@example.com");
        user = userRepository.save(user);

        owner = new User();
        owner.setName("Bob");
        owner.setEmail("bob@example.com");
        owner = userRepository.save(owner);

        item = new Item();
        item.setName("Laptop");
        item.setDescription("Gaming laptop");
        item.setAvailable(true);
        item.setOwner(owner);
        item = itemRepository.save(item);
    }

    @Test
    void createBooking_shouldReturnBooking_whenValidData() {
        BookingDto dto = BookingDto.builder()
                .itemId(item.getId())
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusDays(1))
                .build();

        BookingResponseDto response = bookingService.createBooking(user.getId(), dto);

        assertNotNull(response);
        assertEquals(BookingStatus.WAITING, response.getStatus());
        assertEquals(item.getId(), response.getItem().getId());
        assertEquals(user.getId(), response.getBooker().getId());
    }

    @Test
    void createBooking_shouldThrowNotFoundException_whenUserNotExist() {
        BookingDto dto = BookingDto.builder()
                .itemId(item.getId())
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusDays(1))
                .build();

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> bookingService.createBooking(999L, dto));

        assertTrue(ex.getMessage().contains("Пользователь не найден"));
    }

    @Test
    void createBooking_shouldThrowNotFoundException_whenItemNotExist() {
        BookingDto dto = BookingDto.builder()
                .itemId(999L)
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusDays(1))
                .build();

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> bookingService.createBooking(user.getId(), dto));

        assertTrue(ex.getMessage().contains("Вещь не найдена"));
    }

    @Test
    void createBooking_shouldThrowValidationException_whenItemUnavailable() {
        item.setAvailable(false);
        itemRepository.save(item);

        BookingDto dto = BookingDto.builder()
                .itemId(item.getId())
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusDays(1))
                .build();

        ValidationException ex = assertThrows(ValidationException.class,
                () -> bookingService.createBooking(user.getId(), dto));

        assertTrue(ex.getMessage().contains("Вещь недоступна"));
    }

    @Test
    void createBooking_shouldThrowValidationException_whenOwnerBooksOwnItem() {
        BookingDto dto = BookingDto.builder()
                .itemId(item.getId())
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusDays(1))
                .build();

        ValidationException ex = assertThrows(ValidationException.class,
                () -> bookingService.createBooking(owner.getId(), dto));

        assertTrue(ex.getMessage().contains("Владелец не может бронировать собственную вещь"));
    }

    @Test
    void createBooking_shouldThrowValidationException_whenEndBeforeStart() {
        BookingDto dto = BookingDto.builder()
                .itemId(item.getId())
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusHours(1))
                .build();

        ValidationException ex = assertThrows(ValidationException.class,
                () -> bookingService.createBooking(user.getId(), dto));

        assertTrue(ex.getMessage().contains("Некорректный диапазон дат бронирования"));
    }
}