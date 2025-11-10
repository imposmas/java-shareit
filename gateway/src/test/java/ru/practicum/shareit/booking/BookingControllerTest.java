package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Интеграционный тест для {@link BookingController}.
 * <p>
 * Проверяются все основные эндпоинты контроллера, включая:
 * <ul>
 *     <li>Создание бронирования {@code POST /bookings}</li>
 *     <li>Подтверждение бронирования {@code PATCH /bookings/{bookingId}}</li>
 *     <li>Получение бронирования по ID {@code GET /bookings/{bookingId}}</li>
 *     <li>Получение всех бронирований пользователя {@code GET /bookings}</li>
 *     <li>Получение всех бронирований для владельца вещей {@code GET /bookings/owner}</li>
 * </ul>
 * <p>
 * В тестах используется {@link MockMvc} и {@link Mockito} для имитации работы {@link BookingClient}.
 */
@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingClient bookingClient;

    private BookingDto bookingDto;

    @BeforeEach
    void setUp() {
        bookingDto = BookingDto.builder()
                .id(1L)
                .itemId(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();
    }

    @Test
    void createBooking_ReturnsOk() throws Exception {
        Mockito.when(bookingClient.createBooking(eq(1L), any(BookingDto.class)))
                .thenReturn(ResponseEntity.ok(bookingDto));

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingDto.getId()))
                .andExpect(jsonPath("$.itemId").value(bookingDto.getItemId()));
    }

    @Test
    void approveBooking_ReturnsOk() throws Exception {
        Mockito.when(bookingClient.approveBooking(1L, 1L, true))
                .thenReturn(ResponseEntity.ok(bookingDto));

        mockMvc.perform(patch("/bookings/1")
                        .header("X-Sharer-User-Id", 1L)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingDto.getId()))
                .andExpect(jsonPath("$.itemId").value(bookingDto.getItemId()));
    }

    @Test
    void getBooking_ReturnsOk() throws Exception {
        Mockito.when(bookingClient.getBooking(1L, 1L))
                .thenReturn(ResponseEntity.ok(bookingDto));

        mockMvc.perform(get("/bookings/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingDto.getId()))
                .andExpect(jsonPath("$.itemId").value(bookingDto.getItemId()));
    }

    @Test
    void getBookingsByBooker_ReturnsOk() throws Exception {
        Mockito.when(bookingClient.getBookingsByBooker(1L, "ALL"))
                .thenReturn(ResponseEntity.ok(List.of(bookingDto)));

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(bookingDto.getId()));
    }

    @Test
    void getBookingsForOwner_ReturnsOk() throws Exception {
        Mockito.when(bookingClient.getBookingsForOwner(1L, "ALL"))
                .thenReturn(ResponseEntity.ok(List.of(bookingDto)));

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(bookingDto.getId()));
    }
}