package ru.practicum.shareit.booking;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
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
    public ResponseEntity<BookingDto> createBooking(@RequestHeader(USER_HEADER) Long userId,
                                                    @RequestBody BookingDto dto) {
        BookingDto created = bookingService.createBooking(userId, dto);
        return ResponseEntity.ok(created);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<BookingDto> approveBooking(@RequestHeader(USER_HEADER) Long ownerId,
                                                     @PathVariable Long bookingId,
                                                     @RequestParam boolean approved) {
        log.info("BookingController approveBooking: bookingId = {}, approved = {} ", bookingId, approved);
        BookingDto updated = bookingService.approveBooking(ownerId, bookingId, approved);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingDto> getBooking(@RequestHeader(USER_HEADER) Long userId,
                                                 @PathVariable Long bookingId) {
        log.info("BookingController getBooking: userId = {}, bookingId = {} ", userId, bookingId);
        BookingDto dto = bookingService.getBookingById(userId, bookingId);
        return ResponseEntity.ok(dto);
    }

    @GetMapping
    public ResponseEntity<List<BookingDto>> getBookingsByBooker(@RequestHeader(USER_HEADER) Long userId) {
        log.info("BookingController getBookingsByBooker: userId = {}", userId);
        List<BookingDto> list = bookingService.getBookingsByBooker(userId);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/owner")
    public ResponseEntity<List<BookingDto>> getBookingsForOwner(@RequestHeader(USER_HEADER) Long userId) {
        log.info("BookingController getBookingsForOwner: userId = {}", userId);
        List<BookingDto> list = bookingService.getBookingsForOwner(userId);
        return ResponseEntity.ok(list);
    }
}