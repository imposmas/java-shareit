package ru.practicum.shareit.booking.storage;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.user.User;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class InMemoryBookingRepository {

    private final Map<Long, Booking> bookings = new HashMap<>();
    private long currentId = 0;

    private Long getNextId() {
        return ++currentId;
    }

    public Booking save(Booking booking) {
        if (booking.getId() == null) {
            booking.setId(getNextId());
        }
        bookings.put(booking.getId(), booking);
        return booking;
    }

    public Booking update(Booking booking) {
        Long id = booking.getId();
        if (!bookings.containsKey(id)) {
            throw new NoSuchElementException("Booking with id=" + id + " not found");
        }
        bookings.put(id, booking);
        return booking;
    }

    public Optional<Booking> findById(Long id) {
        return Optional.ofNullable(bookings.get(id));
    }

    public List<Booking> findAll() {
        return new ArrayList<>(bookings.values());
    }

    public void deleteById(Long id) {
        bookings.remove(id);
    }

    public List<Booking> findAllByBooker(User booker) {
        return bookings.values().stream()
                .filter(b -> b.getBooker() != null && b.getBooker().equals(booker))
                .collect(Collectors.toList());
    }

    public List<Booking> findAllByItemOwner(User owner) {
        return bookings.values().stream()
                .filter(b -> b.getItem() != null && b.getItem().getOwner() != null)
                .filter(b -> b.getItem().getOwner().equals(owner))
                .collect(Collectors.toList());
    }
}