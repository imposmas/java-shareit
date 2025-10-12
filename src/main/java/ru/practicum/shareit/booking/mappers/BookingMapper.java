package ru.practicum.shareit.booking.mappers;


import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.constants.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

public class BookingMapper {

    public static BookingDto toBookingDto(Booking b) {
        if (b == null) return null;
        return BookingDto.builder()
                .id(b.getId())
                .itemId(b.getItem() != null ? b.getItem().getId() : null)
                .bookerId(b.getBooker() != null ? b.getBooker().getId() : null)
                .start(b.getStart())
                .end(b.getEnd())
                .status(b.getStatus())
                .build();
    }

    public static Booking toBooking(BookingDto dto, Item item, User booker) {
        if (dto == null) return null;
        return new Booking(
                dto.getId(),
                dto.getStart(),
                dto.getEnd(),
                item,
                booker,
                dto.getStatus() != null ? dto.getStatus() : BookingStatus.WAITING
        );
    }
}
