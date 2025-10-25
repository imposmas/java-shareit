package ru.practicum.shareit.request.mappers;

import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;

public class ItemRequestMapper {

    public static ItemRequestDto toDto(ItemRequest request) {
        if (request == null) return null;
        return ItemRequestDto.builder()
                .id(request.getId())
                .description(request.getDescription())
                .requestorId(request.getRequestor() != null ? request.getRequestor().getId() : null)
                .created(request.getCreated())
                .build();
    }

    public static ItemRequest toEntity(ItemRequestDto dto, User requestor) {
        if (dto == null) return null;
        return new ItemRequest(dto.getId(), dto.getDescription(),
                requestor, dto.getCreated() != null ? dto.getCreated() : LocalDateTime.now());
    }
}
