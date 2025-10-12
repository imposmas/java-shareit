package ru.practicum.shareit.user.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.common.exceptions.DuplicatedDataException;
import ru.practicum.shareit.common.exceptions.NotFoundException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.storage.InMemoryUserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private final InMemoryUserRepository userRepository;

    public UserServiceImpl(InMemoryUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDto createUser(UserDto dto) {
        log.debug("UserServiceImpl createUser parameters: name = {}, email = {}", dto.getName(), dto.getEmail());
        userRepository.findByEmail(dto.getEmail()).ifPresent(u -> {
            throw new DuplicatedDataException("Email already exists: " + dto.getEmail());
        });
        User user = UserMapper.toUser(dto);
        user = userRepository.save(user);
        return UserMapper.toUserDto(user);
    }

    @Override
    public UserDto updateUser(Long userId, UserDto dto) {
        log.debug("UserServiceImpl updateUser parameters: id = {}, name = {}, email = {}",
                dto.getId(), dto.getName(), dto.getEmail());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));

        if (dto.getName() != null) user.setName(dto.getName());
        if (dto.getEmail() != null) {
            userRepository.findByEmail(dto.getEmail()).ifPresent(existing -> {
                if (!existing.getId().equals(userId)) throw new DuplicatedDataException("Email already used");
            });
            user.setEmail(dto.getEmail());
        }

        user = userRepository.update(user);
        return UserMapper.toUserDto(user);
    }

    @Override
    public UserDto getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));
        return UserMapper.toUserDto(user);
    }

    @Override
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteUser(Long userId) {
        if (!userRepository.findById(userId).isPresent()) {
            throw new NotFoundException("User not found: " + userId);
        }
        userRepository.deleteById(userId);
    }
}
