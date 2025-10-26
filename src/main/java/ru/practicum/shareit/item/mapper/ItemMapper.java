package ru.practicum.shareit.item.mapper;


import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.dto.BookingInfoDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.User;

import java.util.List;
import java.util.stream.Collectors;

public class ItemMapper {

    public static ItemDto toItemDto(Item item) {
        return toItemDto(item, null, null, null);
    }

    public static ItemDto toItemDto(Item item, Booking lastBooking, Booking nextBooking) {
        return toItemDto(item, lastBooking, nextBooking, null);
    }

    public static ItemDto toItemDto(Item item, Booking lastBooking, Booking nextBooking, List<Comment> comments) {
        if (item == null) return null;
        Long requestId = item.getRequest() != null ? item.getRequest().getId() : null;
        Long ownerId = item.getOwner() != null ? item.getOwner().getId() : null;

        List<CommentDto> commentDtos = comments != null
                ? comments.stream().map(CommentMapper::toDto).collect(Collectors.toList())
                : List.of();

        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .ownerId(ownerId)
                .requestId(requestId)
                .lastBooking(lastBooking != null
                        ? new BookingInfoDto(lastBooking.getId(), lastBooking.getBooker().getId())
                        : null)
                .nextBooking(nextBooking != null
                        ? new BookingInfoDto(nextBooking.getId(), nextBooking.getBooker().getId())
                        : null)
                .comments(commentDtos)
                .build();
    }

    public static Item toItem(ItemDto dto, User owner, ItemRequest request) {
        if (dto == null) return null;
        return Item.builder()
                .id(dto.getId())
                .name(dto.getName())
                .description(dto.getDescription())
                .available(dto.getAvailable())
                .owner(owner)
                .request(request)
                .build();
    }
}

