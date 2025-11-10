package ru.practicum.shareit.booking;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;

@Slf4j
@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {

    private static final String USER_HEADER = "X-Sharer-User-Id";

    private final BookingClient bookingClient;

    @PostMapping
    public ResponseEntity<Object> createBooking(@RequestHeader(USER_HEADER) Long userId,
                                                @Valid @RequestBody BookingDto dto) {
        log.info("Gateway: create booking for user {}", userId);
        return bookingClient.createBooking(userId, dto);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> approveBooking(@RequestHeader(USER_HEADER) Long ownerId,
                                                 @PathVariable Long bookingId,
                                                 @RequestParam boolean approved) {
        log.info("Gateway: approve booking {} by owner {} (approved={})", bookingId, ownerId, approved);
        return bookingClient.approveBooking(ownerId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getBooking(@RequestHeader(USER_HEADER) Long userId,
                                             @PathVariable Long bookingId) {
        log.info("Gateway: get booking {} by user {}", bookingId, userId);
        return bookingClient.getBooking(userId, bookingId);
    }

    @GetMapping
    public ResponseEntity<Object> getBookingsByBooker(@RequestHeader(USER_HEADER) Long userId,
                                                      @RequestParam(defaultValue = "ALL") String state) {
        log.info("Gateway: get bookings for booker {} with state {}", userId, state);
        return bookingClient.getBookingsByBooker(userId, state);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getBookingsForOwner(@RequestHeader(USER_HEADER) Long userId,
                                                      @RequestParam(defaultValue = "ALL") String state) {
        log.info("Gateway: get bookings for owner {} with state {}", userId, state);
        return bookingClient.getBookingsForOwner(userId, state);
    }
}