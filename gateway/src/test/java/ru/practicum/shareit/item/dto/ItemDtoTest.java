package ru.practicum.shareit.item.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тестирование DTO {@link ItemDto}.
 *
 * <p>
 * Основные направления тестирования:
 * <ul>
 *     <li>Сериализация и десериализация JSON через {@link ObjectMapper}</li>
 *     <li>Валидация полей с аннотациями Jakarta Validation</li>
 * </ul>
 *
 * <p>
 * Проверяется корректность следующих сценариев:
 * <ul>
 *     <li>Сериализация/десериализация объекта с полными данными</li>
 *     <li>Проверка обязательных полей на null и пустые строки (name, description, available)</li>
 *     <li>Соответствие объекту после десериализации исходному объекту</li>
 * </ul>
 */

@JsonTest
class ItemDtoTest {

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
        ItemDto item = ItemDto.builder()
                .id(1L)
                .name("Drill")
                .description("Powerful drill")
                .available(true)
                .ownerId(2L)
                .requestId(3L)
                .comments(List.of())
                .build();

        String json = objectMapper.writeValueAsString(item);
        assertThat(json).contains("Drill", "Powerful drill", "true");

        ItemDto deserialized = objectMapper.readValue(json, ItemDto.class);
        assertThat(deserialized).isEqualTo(item);

        Set<ConstraintViolation<ItemDto>> violations = validator.validate(deserialized);
        assertThat(violations).isEmpty();
    }

    @Test
    void testNullName() {
        ItemDto item = ItemDto.builder()
                .id(1L)
                .name(null)
                .description("Description")
                .available(true)
                .build();

        Set<ConstraintViolation<ItemDto>> violations = validator.validate(item);
        assertThat(violations).hasSize(2);
    }

    @Test
    void testBlankName() {
        ItemDto item = ItemDto.builder()
                .id(1L)
                .name("")
                .description("Description")
                .available(true)
                .build();

        Set<ConstraintViolation<ItemDto>> violations = validator.validate(item);
        assertThat(violations).hasSize(1);
    }

    @Test
    void testNullDescription() {
        ItemDto item = ItemDto.builder()
                .id(1L)
                .name("Drill")
                .description(null)
                .available(true)
                .build();

        Set<ConstraintViolation<ItemDto>> violations = validator.validate(item);
        assertThat(violations).hasSize(2);
    }

    @Test
    void testBlankDescription() {
        ItemDto item = ItemDto.builder()
                .id(1L)
                .name("Drill")
                .description("")
                .available(true)
                .build();

        Set<ConstraintViolation<ItemDto>> violations = validator.validate(item);
        assertThat(violations).hasSize(1);
    }

    @Test
    void testNullAvailable() {
        ItemDto item = ItemDto.builder()
                .id(1L)
                .name("Drill")
                .description("Powerful drill")
                .available(null)
                .build();

        Set<ConstraintViolation<ItemDto>> violations = validator.validate(item);
        assertThat(violations).hasSize(1);
    }
}