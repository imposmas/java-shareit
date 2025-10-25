package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

public interface ItemRequestService {
    ItemRequestDto createRequest(Long userId, ItemRequestDto dto);

    List<ItemRequestDto> getRequestsByUser(Long userId);

    List<ItemRequestDto> getAllRequests();
}
