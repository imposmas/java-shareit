package ru.practicum.shareit.booking.service;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.constants.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.mappers.BookingMapper;
import ru.practicum.shareit.booking.storage.InMemoryBookingRepository;
import ru.practicum.shareit.common.exceptions.NotFoundException;
import ru.practicum.shareit.common.exceptions.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.InMemoryItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.storage.InMemoryUserRepository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Не используется в данном спринте. Будет доработано позже.
 */

@Service
public class BookingServiceImpl implements BookingService {

    private final InMemoryBookingRepository bookingRepository;
    private final InMemoryUserRepository userRepository;
    private final InMemoryItemRepository itemRepository;

    public BookingServiceImpl(InMemoryBookingRepository bookingRepository, InMemoryUserRepository userRepository, InMemoryItemRepository itemRepository) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
    }

    @Override
    public BookingDto createBooking(Long userId, BookingDto dto) {
        User booker = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));
        Item item = itemRepository.findById(dto.getItemId())
                .orElseThrow(() -> new NotFoundException("Item not found: " + dto.getItemId()));

        if (!Boolean.TRUE.equals(item.getAvailable())) {
            throw new ValidationException("Item not available for booking");
        }

        if (dto.getStart() == null || dto.getEnd() == null || dto.getEnd().isBefore(dto.getStart())) {
            throw new ValidationException("Invalid booking time range");
        }

        Booking booking = BookingMapper.toBooking(dto, item, booker);
        booking.setStatus(BookingStatus.WAITING);
        booking = bookingRepository.save(booking);
        return BookingMapper.toBookingDto(booking);
    }

    @Override
    public BookingDto approveBooking(Long ownerId, Long bookingId, boolean approved) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found: " + bookingId));

        if (!booking.getItem().getOwner().getId().equals(ownerId)) {
            throw new ValidationException("Only owner can approve booking");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        booking = bookingRepository.update(booking);
        return BookingMapper.toBookingDto(booking);
    }

    @Override
    public BookingDto getBookingById(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found: " + bookingId));

        if (!booking.getBooker().getId().equals(userId) &&
                !booking.getItem().getOwner().getId().equals(userId)) {
            throw new ValidationException("Access denied to booking");
        }

        return BookingMapper.toBookingDto(booking);
    }

    @Override
    public List<BookingDto> getBookingsByBooker(Long userId) {
        User booker = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        return bookingRepository.findAllByBooker(booker).stream()
                .map(BookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingDto> getBookingsForOwner(Long userId) {
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        return bookingRepository.findAllByItemOwner(owner).stream()
                .map(BookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }
}