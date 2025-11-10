package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.constants.BookingStatus;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.common.exceptions.NotFoundException;
import ru.practicum.shareit.common.exceptions.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.CommentRepository;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ItemServiceImplTest {

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private CommentRepository commentRepository;

    private User user;
    private Item item;

    @BeforeEach
    void setup() {
        user = new User();
        user.setName("John Doe");
        user.setEmail("john@example.com");
        user = userRepository.save(user);

        item = new Item();
        item.setName("Drill");
        item.setDescription("Powerful drill");
        item.setAvailable(true);
        item.setOwner(user);
        item = itemRepository.save(item);
    }

    @Test
    void addComment_shouldCreateComment_whenUserHadCompletedBooking() {
        Booking booking = new Booking();
        booking.setItem(item);
        booking.setBooker(user);
        booking.setStart(LocalDateTime.now().minusDays(5));
        booking.setEnd(LocalDateTime.now().minusDays(1));
        booking.setStatus(BookingStatus.APPROVED);
        bookingRepository.save(booking);

        CommentDto commentDto = new CommentDto();
        commentDto.setText("Great drill!");

        CommentDto saved = itemService.addComment(user.getId(), item.getId(), commentDto);

        assertNotNull(saved.getId());
        assertEquals("Great drill!", saved.getText());
        assertEquals(user.getName(), saved.getAuthorName());
    }

    @Test
    void addComment_shouldThrowValidationException_whenUserNeverBooked() {
        CommentDto commentDto = new CommentDto();
        commentDto.setText("I want to comment!");

        ValidationException exception = assertThrows(ValidationException.class,
                () -> itemService.addComment(user.getId(), item.getId(), commentDto));

        assertEquals("Комментирование разрешено только после окончания бронирования", exception.getMessage());
    }

    @Test
    void addComment_shouldThrowNotFoundException_whenUserOrItemNotFound() {
        CommentDto commentDto = new CommentDto();
        commentDto.setText("Hello!");

        NotFoundException ex1 = assertThrows(NotFoundException.class,
                () -> itemService.addComment(999L, item.getId(), commentDto));
        assertTrue(ex1.getMessage().contains("Пользователь не найден"));

        NotFoundException ex2 = assertThrows(NotFoundException.class,
                () -> itemService.addComment(user.getId(), 999L, commentDto));
        assertTrue(ex2.getMessage().contains("Вещь не найдена"));
    }
}