package ru.practicum.shareit.request.mappers;

import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class ItemRequestMapper {

    public static ItemRequestDto toDto(ItemRequest request, List<Item> items) {
        if (request == null) return null;

        List<ItemRequestResponseDto> responses = items == null ? List.of() :
                items.stream()
                        .map(item -> new ItemRequestResponseDto(item.getId(), item.getName(), item.getOwner().getId()))
                        .collect(Collectors.toList());

        return ItemRequestDto.builder()
                .id(request.getId())
                .description(request.getDescription())
                .requestorId(request.getRequestor() != null ? request.getRequestor().getId() : null)
                .created(request.getCreated())
                .items(responses)
                .build();
    }

    public static ItemRequest toEntity(ItemRequestDto dto, User requestor) {
        if (dto == null) return null;
        return new ItemRequest(dto.getId(), dto.getDescription(),
                requestor, dto.getCreated() != null ? dto.getCreated() : LocalDateTime.now());
    }
}