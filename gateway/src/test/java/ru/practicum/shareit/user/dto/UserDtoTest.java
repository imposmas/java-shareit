package ru.practicum.shareit.user.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тестирование DTO {@link UserDto}.
 *
 * <p>
 * Основная цель тестов — проверить:
 * <ul>
 *     <li>Сериализацию и десериализацию JSON с помощью {@link ObjectMapper}</li>
 *     <li>Валидацию полей {@link UserDto} с помощью {@link Validator}</li>
 * </ul>
 *
 * <p>
 * Проверяемые сценарии:
 * <ul>
 *     <li>Правильная сериализация/десериализация</li>
 *     <li>Корректный email</li>
 *     <li>Некорректный email (ошибка валидации)</li>
 *     <li>Пустой email (ошибка валидации)</li>
 * </ul>
 */

@JsonTest
class UserDtoTest {

    @Autowired
    private ObjectMapper objectMapper;

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testSerializeDeserialize() throws Exception {
        UserDto user = UserDto.builder()
                .id(1L)
                .name("Peter Parket")
                .email("peter.parker@example.com")
                .build();

        String json = objectMapper.writeValueAsString(user);
        assertThat(json).contains("Peter Parket", "peter.parker@example.com");

        UserDto deserialized = objectMapper.readValue(json, UserDto.class);
        assertThat(deserialized).isEqualTo(user);

        Set<ConstraintViolation<UserDto>> violations = validator.validate(deserialized);
        assertThat(violations).isEmpty();
    }

    @Test
    void testInvalidEmail() throws Exception {
        UserDto user = UserDto.builder()
                .id(1L)
                .name("Peter Parker")
                .email("invalid-email")
                .build();

        Set<ConstraintViolation<UserDto>> violations = validator.validate(user);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .contains("must be a well-formed email address");
    }

    @Test
    void testBlankEmail() throws Exception {
        UserDto user = UserDto.builder()
                .id(1L)
                .name("Peter Parker")
                .email("")
                .build();

        Set<ConstraintViolation<UserDto>> violations = validator.validate(user);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .contains("must not be blank");
    }
}