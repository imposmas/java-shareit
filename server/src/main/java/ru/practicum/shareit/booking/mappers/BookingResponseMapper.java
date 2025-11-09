package ru.practicum.shareit.booking.mappers;

import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

public class BookingResponseMapper {

    public static BookingResponseDto toBookingResponseDto(Booking b) {
        if (b == null) return null;

        UserDto bookerDto = null;
        if (b.getBooker() != null) {
            bookerDto = UserDto.builder()
                    .id(b.getBooker().getId())
                    .name(b.getBooker().getName())
                    .email(b.getBooker().getEmail())
                    .build();
        }

        ItemDto itemDto = null;
        if (b.getItem() != null) {
            itemDto = ItemDto.builder()
                    .id(b.getItem().getId())
                    .name(b.getItem().getName())
                    .description(b.getItem().getDescription())
                    .available(b.getItem().getAvailable())
                    .ownerId(b.getItem().getOwner() != null ? b.getItem().getOwner().getId() : null)
                    .requestId(b.getItem().getRequest() != null ? b.getItem().getRequest().getId() : null)
                    .build();
        }

        return BookingResponseDto.builder()
                .id(b.getId())
                .start(b.getStart())
                .end(b.getEnd())
                .status(b.getStatus())
                .booker(bookerDto)
                .item(itemDto)
                .build();
    }
}