package ru.practicum.shareit.item.mapper;


import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.User;

public class ItemMapper {

    public static ItemDto toItemDto(Item item) {
        if (item == null) return null;
        Long requestId = item.getRequest() != null ? item.getRequest().getId() : null;
        Long ownerId = item.getOwner() != null ? item.getOwner().getId() : null;
        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .ownerId(ownerId)
                .requestId(requestId)
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

