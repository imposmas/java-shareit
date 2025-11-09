package ru.practicum.shareit.booking.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;

import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тесты для {@link BookingDto}.
 *
 * <p>
 * Проверяется сериализация и десериализация JSON с помощью {@link ObjectMapper},
 * а также валидация полей с использованием аннотаций Bean Validation:
 * <ul>
 *     <li>Проверка обязательности полей {@code itemId}, {@code start}, {@code end}</li>
 *     <li>Проверка положительности {@code itemId}</li>
 *     <li>Проверка даты окончания бронирования {@code end} на будущее</li>
 * </ul>
 *
 * <p>
 * Используется {@link JsonTest} для тестирования JSON-сериализации и {@link Validator} для проверки ограничений.
 */

@JsonTest
class BookingDtoTest {

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
        BookingDto booking = BookingDto.builder()
                .id(1L)
                .itemId(2L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .status(null)
                .build();

        String json = objectMapper.writeValueAsString(booking);
        assertThat(json).contains("1", "2");

        BookingDto deserialized = objectMapper.readValue(json, BookingDto.class);
        assertThat(deserialized).isEqualTo(booking);

        Set<ConstraintViolation<BookingDto>> violations = validator.validate(deserialized);
        assertThat(violations).isEmpty();
    }

    @Test
    void testNullItemId() {
        BookingDto booking = BookingDto.builder()
                .itemId(null)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        Set<ConstraintViolation<BookingDto>> violations = validator.validate(booking);
        assertThat(violations).anyMatch(v -> v.getMessage().contains("ID вещи не может быть пустым"));
    }

    @Test
    void testNegativeItemId() {
        BookingDto booking = BookingDto.builder()
                .itemId(-1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        Set<ConstraintViolation<BookingDto>> violations = validator.validate(booking);
        assertThat(violations).anyMatch(v -> v.getMessage().contains("ID вещи должно быть положительным числом"));
    }

    @Test
    void testNullStart() {
        BookingDto booking = BookingDto.builder()
                .itemId(1L)
                .start(null)
                .end(LocalDateTime.now().plusDays(2))
                .build();

        Set<ConstraintViolation<BookingDto>> violations = validator.validate(booking);
        assertThat(violations).anyMatch(v -> v.getMessage().contains("Дата начала бронирования обязательна"));
    }

    @Test
    void testNullEnd() {
        BookingDto booking = BookingDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(null)
                .build();

        Set<ConstraintViolation<BookingDto>> violations = validator.validate(booking);
        assertThat(violations).anyMatch(v -> v.getMessage().contains("Дата окончания бронирования обязательна"));
    }

    @Test
    void testEndInPast() {
        BookingDto booking = BookingDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().minusDays(1))
                .build();

        Set<ConstraintViolation<BookingDto>> violations = validator.validate(booking);
        assertThat(violations).anyMatch(v -> v.getMessage().contains("Дата окончания бронирования должна быть в будущем"));
    }
}