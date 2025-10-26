package ru.practicum.shareit.user.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.common.exceptions.DuplicatedDataException;
import ru.practicum.shareit.common.exceptions.NotFoundException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.storage.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Создаёт нового пользователя.
     *
     * @param dto данные пользователя (имя, email)
     * @return {@link UserDto} с информацией о созданном пользователе
     * @throws DuplicatedDataException если указанный email уже используется
     */
    @Override
    @Transactional
    public UserDto createUser(UserDto dto) {
        log.debug("Создание пользователя: name = {}, email = {}", dto.getName(), dto.getEmail());

        userRepository.findByEmail(dto.getEmail()).ifPresent(u -> {
            throw new DuplicatedDataException("Email уже используется: " + dto.getEmail());
        });

        User user = UserMapper.toUser(dto);
        user = userRepository.save(user);
        log.info("Пользователь создан: id = {}, email = {}", user.getId(), user.getEmail());
        return UserMapper.toUserDto(user);
    }

    /**
     * Обновляет данные пользователя.
     * Можно изменять имя и/или email.
     * Email должен быть уникальным среди всех пользователей.
     *
     * @param userId идентификатор пользователя
     * @param dto    новые данные пользователя
     * @return {@link UserDto} с обновлёнными данными
     * @throws NotFoundException       если пользователь не найден
     * @throws DuplicatedDataException если новый email уже используется другим пользователем
     */
    @Override
    @Transactional
    public UserDto updateUser(Long userId, UserDto dto) {
        log.debug("Обновление пользователя: id = {}, name = {}, email = {}", userId, dto.getName(), dto.getEmail());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден: " + userId));

        if (dto.getName() != null) {
            user.setName(dto.getName());
        }

        if (dto.getEmail() != null) {
            userRepository.findByEmail(dto.getEmail()).ifPresent(existing -> {
                if (!existing.getId().equals(userId)) {
                    throw new DuplicatedDataException("Email уже используется другим пользователем: " + dto.getEmail());
                }
            });
            user.setEmail(dto.getEmail());
        }

        user = userRepository.save(user);
        log.info("Пользователь обновлён: id = {}, email = {}", user.getId(), user.getEmail());
        return UserMapper.toUserDto(user);
    }

    /**
     * Возвращает пользователя по его идентификатору.
     *
     * @param userId идентификатор пользователя
     * @return {@link UserDto} с информацией о пользователе
     * @throws NotFoundException если пользователь не найден
     */
    @Override
    public UserDto getUserById(Long userId) {
        log.debug("Получение пользователя по id = {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден: " + userId));

        return UserMapper.toUserDto(user);
    }

    /**
     * Возвращает список всех пользователей в системе.
     *
     * @return список {@link UserDto}
     */
    @Override
    public List<UserDto> getAllUsers() {
        log.debug("Получение списка всех пользователей");

        return userRepository.findAll().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    /**
     * Удаляет пользователя по его идентификатору.
     *
     * @param userId идентификатор пользователя
     * @throws NotFoundException если пользователь не найден
     */
    @Override
    @Transactional
    public void deleteUser(Long userId) {
        log.debug("Удаление пользователя id = {}", userId);

        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь не найден: " + userId);
        }

        userRepository.deleteById(userId);
        log.info("Пользователь удалён: id = {}", userId);
    }
}