package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingDto;

import java.util.List;

public interface BookingService {
    BookingDto createBooking(Long userId, BookingDto dto);
    BookingDto approveBooking(Long ownerId, Long bookingId, boolean approved);
    BookingDto getBookingById(Long userId, Long bookingId);
    List<BookingDto> getBookingsByBooker(Long userId);
    List<BookingDto> getBookingsForOwner(Long userId);
}