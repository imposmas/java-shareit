package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.common.exceptions.DuplicatedDataException;
import ru.practicum.shareit.common.exceptions.NotFoundException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.storage.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class UserServiceImplTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        user1 = userRepository.save(new User(null, "Panda", "panda@example.com"));
        user2 = userRepository.save(new User(null, "Bob", "bob@example.com"));
    }

    @Test
    void testUpdateUserSuccess() {
        UserDto updateDto = UserDto.builder()
                .name("Panda Updated")
                .email("panda.updated@example.com")
                .build();

        UserDto updated = userService.updateUser(user1.getId(), updateDto);

        assertThat(updated.getId()).isEqualTo(user1.getId());
        assertThat(updated.getName()).isEqualTo("Panda Updated");
        assertThat(updated.getEmail()).isEqualTo("panda.updated@example.com");

        User persisted = userRepository.findById(user1.getId()).get();
        assertThat(persisted.getName()).isEqualTo("Panda Updated");
        assertThat(persisted.getEmail()).isEqualTo("panda.updated@example.com");
    }

    @Test
    void testUpdateUserNotFound() {
        UserDto updateDto = UserDto.builder().name("New Name").build();

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userService.updateUser(999L, updateDto));

        assertThat(exception.getMessage()).contains("Пользователь не найден");
    }

    @Test
    void testUpdateUserDuplicateEmail() {
        UserDto updateDto = UserDto.builder()
                .email("bob@example.com")
                .build();

        DuplicatedDataException exception = assertThrows(DuplicatedDataException.class,
                () -> userService.updateUser(user1.getId(), updateDto));

        assertThat(exception.getMessage()).contains("Email уже используется другим пользователем");
    }

    @Test
    void testUpdateUserPartialUpdate() {
        UserDto updateDto = UserDto.builder()
                .name("Panda Only Name Updated")
                .build();

        UserDto updated = userService.updateUser(user1.getId(), updateDto);

        assertThat(updated.getName()).isEqualTo("Panda Only Name Updated");
        assertThat(updated.getEmail()).isEqualTo("panda@example.com");
    }
}