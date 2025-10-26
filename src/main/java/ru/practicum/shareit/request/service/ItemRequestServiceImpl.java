package ru.practicum.shareit.request.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.common.exceptions.NotFoundException;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.mappers.ItemRequestMapper;
import ru.practicum.shareit.request.storage.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository requestRepository;
    private final UserRepository userRepository;

    public ItemRequestServiceImpl(ItemRequestRepository requestRepository,
                                  UserRepository userRepository) {
        this.requestRepository = requestRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public ItemRequestDto createRequest(Long userId, ItemRequestDto dto) {
        log.debug("Создание запроса на вещь пользователем id = {}, описание: {}", userId, dto.getDescription());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден: " + userId));

        if (dto.getDescription() == null || dto.getDescription().isBlank()) {
            throw new IllegalArgumentException("Описание запроса не может быть пустым");
        }

        ItemRequest request = new ItemRequest();
        request.setDescription(dto.getDescription());
        request.setRequestor(user);
        request.setCreated(LocalDateTime.now());

        request = requestRepository.save(request);

        log.info("Создан запрос id = {} пользователем id = {}", request.getId(), userId);
        return ItemRequestMapper.toDto(request);
    }

    @Override
    public List<ItemRequestDto> getRequestsByUser(Long userId) {
        log.debug("Получение запросов пользователя id = {}", userId);

        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь не найден: " + userId);
        }

        return requestRepository.findAllByRequestorIdOrderByCreatedDesc(userId).stream()
                .map(ItemRequestMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemRequestDto> getAllRequests() {
        log.debug("Получение всех запросов на вещи");

        return requestRepository.findAllByOrderByCreatedDesc().stream()
                .map(ItemRequestMapper::toDto)
                .collect(Collectors.toList());
    }
}