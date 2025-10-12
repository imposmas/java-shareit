package ru.practicum.shareit.request.service;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.common.exceptions.NotFoundException;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.mappers.ItemRequestMapper;
import ru.practicum.shareit.request.storage.InMemoryItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.storage.InMemoryUserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Не используется в данном спринте. Будет доработано позже.
 */

@Service
public class ItemRequestServiceImpl implements ItemRequestService {

    private final InMemoryItemRequestRepository requestRepository;
    private final InMemoryUserRepository userRepository;

    public ItemRequestServiceImpl(InMemoryItemRequestRepository requestRepository, InMemoryUserRepository userRepository) {
        this.requestRepository = requestRepository;
        this.userRepository = userRepository;
    }

    @Override
    public ItemRequestDto createRequest(Long userId, ItemRequestDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));
        ItemRequest request = new ItemRequest(null, dto.getDescription(), user, LocalDateTime.now());
        request = requestRepository.save(request);
        return ItemRequestMapper.toDto(request);
    }

    @Override
    public List<ItemRequestDto> getRequestsByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));
        return requestRepository.findAllByRequestor(user).stream()
                .map(ItemRequestMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemRequestDto> getAllRequests() {
        return requestRepository.findAll().stream()
                .map(ItemRequestMapper::toDto)
                .collect(Collectors.toList());
    }
}