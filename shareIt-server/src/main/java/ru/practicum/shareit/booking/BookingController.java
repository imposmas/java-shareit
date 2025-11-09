package ru.practicum.shareit.booking;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.constants.BookingState;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.service.BookingService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/bookings")
public class BookingController {

    private final BookingService bookingService;
    private static final String USER_HEADER = "X-Sharer-User-Id";

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<BookingResponseDto> createBooking(@RequestHeader(USER_HEADER) Long userId,
                                                            @RequestBody BookingDto dto) {
        BookingResponseDto created = bookingService.createBooking(userId, dto);
        return ResponseEntity.ok(created);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<BookingResponseDto> approveBooking(@RequestHeader(USER_HEADER) Long ownerId,
                                                             @PathVariable Long bookingId,
                                                             @RequestParam boolean approved) {
        log.info("BookingController approveBooking: bookingId = {}, approved = {} ", bookingId, approved);
        BookingResponseDto updated = bookingService.approveBooking(ownerId, bookingId, approved);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingResponseDto> getBooking(@RequestHeader(USER_HEADER) Long userId,
                                                         @PathVariable Long bookingId) {
        log.info("BookingController getBooking: userId = {}, bookingId = {} ", userId, bookingId);
        BookingResponseDto dto = bookingService.getBookingById(userId, bookingId);
        return ResponseEntity.ok(dto);
    }

    @GetMapping
    public ResponseEntity<List<BookingResponseDto>> getBookingsByBooker(
            @RequestHeader(USER_HEADER) Long userId,
            @RequestParam(defaultValue = "ALL") String state) {

        BookingState bookingState = BookingState.from(state);
        List<BookingResponseDto> list = bookingService.getBookingsByBooker(userId, bookingState);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/owner")
    public ResponseEntity<List<BookingResponseDto>> getBookingsForOwner(
            @RequestHeader(USER_HEADER) Long userId,
            @RequestParam(defaultValue = "ALL") String state) {
        log.info("BookingController getBookingsForOwner: userId = {}, state = {}", userId, state);
        BookingState bookingState = BookingState.from(state);
        List<BookingResponseDto> list = bookingService.getBookingsForOwner(userId, bookingState);
        return ResponseEntity.ok(list);
    }
}