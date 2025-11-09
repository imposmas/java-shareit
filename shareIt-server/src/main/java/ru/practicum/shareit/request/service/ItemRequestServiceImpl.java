package ru.practicum.shareit.request.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.common.exceptions.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
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
    private final ItemRepository itemRepository;

    public ItemRequestServiceImpl(ItemRequestRepository requestRepository,
                                  UserRepository userRepository,
                                  ItemRepository itemRepository) {
        this.requestRepository = requestRepository;
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
    }

    /**
     * Создает новый запрос на вещь от пользователя.
     *
     * @param userId идентификатор пользователя, создающего запрос
     * @param dto    данные запроса, включая описание
     * @return {@link ItemRequestDto} созданного запроса
     * @throws NotFoundException        если пользователь с заданным ID не найден
     * @throws IllegalArgumentException если описание запроса пустое или null
     */
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
        return ItemRequestMapper.toDto(request, List.of());
    }

    /**
     * Получает список всех запросов, созданных конкретным пользователем.
     *
     * @param userId идентификатор пользователя
     * @return список {@link ItemRequestDto} запросов пользователя
     * @throws NotFoundException если пользователь с заданным ID не найден
     */
    @Override
    public List<ItemRequestDto> getRequestsByUser(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден: " + userId));

        return requestRepository.findAllByRequestorIdOrderByCreatedDesc(userId).stream()
                .map(req -> {
                    List<Item> items = itemRepository.findAllByRequestId(req.getId());
                    return ItemRequestMapper.toDto(req, items);
                })
                .collect(Collectors.toList());
    }

    /**
     * Получает список всех запросов, кроме запросов конкретного пользователя.
     *
     * @param userId идентификатор пользователя, запросы которого исключаются
     * @return список {@link ItemRequestDto} всех остальных запросов
     * @throws NotFoundException если пользователь с заданным ID не найден
     */
    @Override
    public List<ItemRequestDto> getAllRequests(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден: " + userId));

        return requestRepository.findAllByRequestorIdNotOrderByCreatedDesc(userId).stream()
                .map(req -> {
                    List<Item> items = itemRepository.findAllByRequestId(req.getId());
                    return ItemRequestMapper.toDto(req, items);
                })
                .collect(Collectors.toList());
    }

    /**
     * Получает конкретный запрос по ID.
     *
     * @param userId    идентификатор пользователя, запрашивающего данные
     * @param requestId идентификатор запроса
     * @return {@link ItemRequestDto} запроса с указанным ID
     * @throws NotFoundException если пользователь или запрос с заданным ID не найден
     */
    @Override
    public ItemRequestDto getRequestById(Long userId, Long requestId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден: " + userId));

        ItemRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос не найден: " + requestId));

        List<Item> items = itemRepository.findAllByRequestId(requestId);
        return ItemRequestMapper.toDto(request, items);
    }
}