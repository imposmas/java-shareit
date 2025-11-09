package ru.practicum.shareit.booking.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.constants.BookingState;
import ru.practicum.shareit.booking.constants.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.mappers.BookingMapper;
import ru.practicum.shareit.booking.mappers.BookingResponseMapper;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.common.exceptions.NotFoundException;
import ru.practicum.shareit.common.exceptions.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    public BookingServiceImpl(BookingRepository bookingRepository,
                              UserRepository userRepository,
                              ItemRepository itemRepository) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
    }

    /**
     * Создает новое бронирование для указанного пользователя.
     *
     * @param userId идентификатор пользователя, создающего бронирование
     * @param dto    данные о бронировании (даты, вещь, статус и т.д.)
     * @return {@link BookingResponseDto} с полной информацией о созданном бронировании
     * @throws NotFoundException   если пользователь или вещь не найдены
     * @throws ValidationException если бронирование недопустимо (неверные даты, вещь недоступна, владелец бронирует сам у себя)
     */
    @Override
    @Transactional
    public BookingResponseDto createBooking(Long userId, BookingDto dto) {
        log.debug("Создание бронирования пользователем id = {}, данные: {}", userId, dto);

        User booker = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден: " + userId));

        Item item = itemRepository.findById(dto.getItemId())
                .orElseThrow(() -> new NotFoundException("Вещь не найдена: " + dto.getItemId()));

        if (!Boolean.TRUE.equals(item.getAvailable())) {
            throw new ValidationException("Вещь недоступна для бронирования");
        }

        if (dto.getStart() == null || dto.getEnd() == null || !dto.getEnd().isAfter(dto.getStart())) {
            throw new ValidationException("Некорректный диапазон дат бронирования");
        }

        if (item.getOwner().getId().equals(userId)) {
            throw new ValidationException("Владелец не может бронировать собственную вещь");
        }

        Booking booking = BookingMapper.toBooking(dto, item, booker);
        booking.setStatus(BookingStatus.WAITING);

        booking = bookingRepository.save(booking);
        log.info("Бронирование создано: id = {}, статус = {}", booking.getId(), booking.getStatus());

        return BookingResponseMapper.toBookingResponseDto(booking);
    }

    /**
     * Подтверждает или отклоняет бронирование владельцем вещи.
     *
     * @param ownerId   идентификатор владельца вещи
     * @param bookingId идентификатор бронирования
     * @param approved  {@code true}, если бронирование одобрено; {@code false}, если отклонено
     * @return {@link BookingResponseDto} с обновленным статусом бронирования
     * @throws NotFoundException   если бронирование не найдено
     * @throws ValidationException если подтверждение недопустимо (не владелец, повторное подтверждение и т.д.)
     */
    @Override
    @Transactional
    public BookingResponseDto approveBooking(Long ownerId, Long bookingId, boolean approved) {
        log.debug("Подтверждение бронирования id = {} пользователем id = {}, статус: {}",
                bookingId, ownerId, approved ? "APPROVED" : "REJECTED");

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование не найдено: " + bookingId));

        if (!booking.getItem().getOwner().getId().equals(ownerId)) {
            throw new ValidationException("Только владелец может подтверждать бронирование");
        }

        if (booking.getStatus() == BookingStatus.APPROVED && approved) {
            throw new ValidationException("Бронирование уже подтверждено");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        booking = bookingRepository.save(booking);

        log.info("Бронирование обновлено: id = {}, статус = {}", booking.getId(), booking.getStatus());
        return BookingResponseMapper.toBookingResponseDto(booking);
    }

    /**
     * Возвращает информацию о бронировании по ID.
     * Доступ разрешён только владельцу вещи или пользователю, который делал бронирование.
     *
     * @param userId    идентификатор пользователя, запрашивающего информацию
     * @param bookingId идентификатор бронирования
     * @return {@link BookingResponseDto} с полной информацией о бронировании
     * @throws NotFoundException   если бронирование не найдено
     * @throws ValidationException если у пользователя нет доступа к этому бронированию
     */
    @Override
    public BookingResponseDto getBookingById(Long userId, Long bookingId) {
        log.debug("Получение бронирования id = {} пользователем id = {}", bookingId, userId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование не найдено: " + bookingId));

        if (!booking.getBooker().getId().equals(userId) &&
                !booking.getItem().getOwner().getId().equals(userId)) {
            throw new ValidationException("Доступ запрещён к данному бронированию");
        }

        return BookingResponseMapper.toBookingResponseDto(booking);
    }

    /**
     * Возвращает список бронирований, созданных пользователем.
     *
     * @param userId идентификатор пользователя (бронирующего)
     * @param state  фильтр по состоянию бронирования (ALL, CURRENT, PAST, FUTURE, WAITING, REJECTED)
     * @return список {@link BookingResponseDto}
     * @throws NotFoundException если пользователь не найден
     */
    @Override
    public List<BookingResponseDto> getBookingsByBooker(Long userId, BookingState state) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь не найден: " + userId);
        }

        List<Booking> bookings;

        switch (state) {
            case CURRENT:
                bookings = bookingRepository.findCurrentByBooker(userId);
                break;
            case PAST:
                bookings = bookingRepository.findPastByBooker(userId);
                break;
            case FUTURE:
                bookings = bookingRepository.findFutureByBooker(userId);
                break;
            case WAITING:
                bookings = bookingRepository.findByBookerIdAndStatusOrderByStartDesc(userId, BookingStatus.WAITING);
                break;
            case REJECTED:
                bookings = bookingRepository.findByBookerIdAndStatusOrderByStartDesc(userId, BookingStatus.REJECTED);
                break;
            default:
                bookings = bookingRepository.findAllByBookerIdOrderByStartDesc(userId);
        }

        return bookings.stream()
                .map(BookingResponseMapper::toBookingResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Возвращает список бронирований для вещей, принадлежащих пользователю-владельцу.
     *
     * @param userId идентификатор владельца
     * @param state  фильтр по состоянию бронирования (ALL, CURRENT, PAST, FUTURE, WAITING, REJECTED)
     * @return список {@link BookingResponseDto}
     * @throws NotFoundException если пользователь не найден
     */
    @Override
    public List<BookingResponseDto> getBookingsForOwner(Long userId, BookingState state) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь не найден: " + userId);
        }

        List<Booking> bookings;

        switch (state) {
            case CURRENT:
                bookings = bookingRepository.findCurrentByOwner(userId);
                break;
            case PAST:
                bookings = bookingRepository.findPastByOwner(userId);
                break;
            case FUTURE:
                bookings = bookingRepository.findFutureByOwner(userId);
                break;
            case WAITING:
                bookings = bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(userId, BookingStatus.WAITING);
                break;
            case REJECTED:
                bookings = bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(userId, BookingStatus.REJECTED);
                break;
            default:
                bookings = bookingRepository.findAllByItemOwnerIdOrderByStartDesc(userId);
        }

        return bookings.stream()
                .map(BookingResponseMapper::toBookingResponseDto)
                .collect(Collectors.toList());
    }
}