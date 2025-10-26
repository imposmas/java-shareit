package ru.practicum.shareit.item.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.constants.BookingStatus;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.common.exceptions.NotAuthorizedException;
import ru.practicum.shareit.common.exceptions.NotFoundException;
import ru.practicum.shareit.common.exceptions.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.CommentRepository;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    public ItemServiceImpl(ItemRepository itemRepository,
                           UserRepository userRepository,
                           BookingRepository bookingRepository,
                           CommentRepository commentRepository) {
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.commentRepository = commentRepository;
    }

    /**
     * Создает новую вещь для пользователя-владельца.
     *
     * @param userId идентификатор владельца вещи
     * @param dto    данные вещи (название, описание, доступность)
     * @return {@link ItemDto} с информацией о созданной вещи
     * @throws NotFoundException        если пользователь не найден
     * @throws IllegalArgumentException если поле {@code available} не указано
     */
    @Override
    @Transactional
    public ItemDto createItem(Long userId, ItemDto dto) {
        log.debug("Создание вещи пользователем id = {}: {}", userId, dto);

        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь-владелец не найден: " + userId));

        if (dto.getAvailable() == null) {
            throw new IllegalArgumentException("Поле 'available' обязательно для заполнения");
        }

        Item item = ItemMapper.toItem(dto, owner, null);
        item = itemRepository.save(item);

        log.info("Создана вещь id = {} пользователем id = {}", item.getId(), owner.getId());
        return ItemMapper.toItemDto(item);
    }

    /**
     * Обновляет существующую вещь.
     * Обновление разрешено только владельцу вещи.
     *
     * @param userId идентификатор пользователя, выполняющего обновление
     * @param itemId идентификатор вещи
     * @param dto    новые данные вещи
     * @return {@link ItemDto} с обновленными данными
     * @throws NotFoundException      если вещь не найдена
     * @throws NotAuthorizedException если пользователь не является владельцем вещи
     */
    @Override
    @Transactional
    public ItemDto updateItem(Long userId, Long itemId, ItemDto dto) {
        log.debug("Обновление вещи id = {} пользователем id = {}", itemId, userId);

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена: " + itemId));

        if (!item.getOwner().getId().equals(userId)) {
            throw new NotAuthorizedException("Только владелец может редактировать вещь");
        }

        if (dto.getName() != null) item.setName(dto.getName());
        if (dto.getDescription() != null) item.setDescription(dto.getDescription());
        if (dto.getAvailable() != null) item.setAvailable(dto.getAvailable());

        item = itemRepository.save(item);

        log.info("Вещь обновлена: id = {} пользователем id = {}", item.getId(), userId);
        return ItemMapper.toItemDto(item);
    }

    /**
     * Возвращает информацию о вещи по её идентификатору.
     * Если пользователь — владелец, могут быть добавлены данные о бронированиях.
     *
     * @param userId идентификатор пользователя, выполняющего запрос
     * @param itemId идентификатор вещи
     * @return {@link ItemDto} с информацией о вещи и комментариями
     * @throws NotFoundException если вещь не найдена
     */
    @Override
    public ItemDto getItemById(Long userId, Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена: " + itemId));

        List<Comment> comments = commentRepository.findAllByItemIdOrderByCreatedDesc(itemId);
        return ItemMapper.toItemDto(item, null, null, comments);
    }

    /**
     * Возвращает все вещи, принадлежащие владельцу.
     * Для каждой вещи добавляются последние и будущие бронирования, а также комментарии.
     *
     * @param userId идентификатор владельца
     * @return список {@link ItemDto} с дополнительными данными
     * @throws NotFoundException если пользователь не найден
     */
    @Override
    public List<ItemDto> getItemsByOwner(Long userId) {
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден: " + userId));

        List<Item> items = itemRepository.findAllByOwnerId(owner.getId());

        return items.stream()
                .map(item -> {
                    List<Comment> comments = commentRepository.findAllByItemIdOrderByCreatedDesc(item.getId());
                    Booking lastBooking = bookingRepository.findLastBooking(item.getId(), Pageable.ofSize(1))
                            .stream().findFirst().orElse(null);
                    Booking nextBooking = bookingRepository.findNextBooking(item.getId(), Pageable.ofSize(1))
                            .stream().findFirst().orElse(null);
                    return ItemMapper.toItemDto(item, lastBooking, nextBooking, comments);
                })
                .collect(Collectors.toList());
    }

    /**
     * Выполняет поиск доступных вещей по тексту (в названии или описании).
     * Поиск нечувствителен к регистру.
     *
     * @param text текст поискового запроса
     * @return список {@link ItemDto}, соответствующих запросу
     */
    @Override
    public List<ItemDto> searchItems(String text) {
        log.debug("Поиск вещей по тексту: '{}'", text);

        if (text == null || text.isBlank()) {
            return List.of();
        }

        return itemRepository.searchAvailableByText(text.toLowerCase()).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    /**
     * Добавляет комментарий к вещи.
     * Комментировать может только пользователь, который ранее бронировал вещь,
     * и только после окончания срока бронирования.
     *
     * @param userId     идентификатор пользователя, оставляющего комментарий
     * @param itemId     идентификатор вещи
     * @param commentDto данные комментария
     * @return {@link CommentDto} созданный комментарий
     * @throws NotFoundException   если пользователь или вещь не найдены
     * @throws ValidationException если пользователь не имеет права комментировать (не бронировал или аренда не завершена)
     */
    @Override
    @Transactional
    public CommentDto addComment(Long userId, Long itemId, CommentDto commentDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден: " + userId));
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена " + itemId));

        boolean hasBooking = bookingRepository.existsByItemIdAndBookerIdAndStatusAndEndBefore(
                itemId, userId, BookingStatus.APPROVED, LocalDateTime.now()
        );

        if (!hasBooking) {
            throw new ValidationException("Комментирование разрешено только после окончания бронирования");
        }

        Comment comment = CommentMapper.toComment(commentDto, user, item);
        comment = commentRepository.save(comment);
        return CommentMapper.toDto(comment);
    }

    /**
     * Возвращает список комментариев к указанной вещи.
     *
     * @param itemId идентификатор вещи
     * @return список {@link CommentDto}, отсортированный по дате создания (новые первыми)
     */
    @Override
    public List<CommentDto> getCommentsForItem(Long itemId) {
        return commentRepository.findAllByItemIdOrderByCreatedDesc(itemId).stream()
                .map(CommentMapper::toDto)
                .collect(Collectors.toList());
    }
}