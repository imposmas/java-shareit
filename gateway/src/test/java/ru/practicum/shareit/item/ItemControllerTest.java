package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingInfoDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Интеграционные тесты контроллера {@link ItemController}.
 *
 * <p>
 * Тестируется взаимодействие REST API с клиентом {@link ItemClient} через MockMvc:
 * <ul>
 *     <li>Создание новой вещи (POST /items)</li>
 *     <li>Обновление существующей вещи (PATCH /items/{id})</li>
 *     <li>Получение информации о вещи по ID (GET /items/{id})</li>
 *     <li>Получение всех вещей владельца (GET /items)</li>
 *     <li>Поиск вещей по тексту (GET /items/search)</li>
 *     <li>Добавление комментария к вещи (POST /items/{id}/comment)</li>
 * </ul>
 *
 * <p>
 * Используются Mockito-заглушки для {@link ItemClient}, чтобы проверить корректность HTTP-запросов
 * и сериализации/десериализации JSON через {@link ObjectMapper}.
 */

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemClient itemClient;

    private ItemDto itemDto;
    private CommentDto commentDto;

    @BeforeEach
    void setUp() {
        BookingInfoDto lastBooking = new BookingInfoDto(1L, 1L);
        BookingInfoDto nextBooking = new BookingInfoDto(2L, 1L);

        commentDto = CommentDto.builder()
                .id(1L)
                .text("Nice item")
                .authorName("John")
                .created(LocalDateTime.now())
                .build();

        itemDto = ItemDto.builder()
                .id(1L)
                .name("Drill")
                .description("Electric drill")
                .available(true)
                .ownerId(1L)
                .requestId(null)
                .lastBooking(lastBooking)
                .nextBooking(nextBooking)
                .comments(List.of(commentDto))
                .build();
    }

    @Test
    void createItem_ReturnsOk() throws Exception {
        Mockito.when(itemClient.createItem(eq(1L), any(ItemDto.class)))
                .thenReturn(ResponseEntity.ok(itemDto));

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemDto.getId()))
                .andExpect(jsonPath("$.name").value(itemDto.getName()));
    }

    @Test
    void updateItem_ReturnsOk() throws Exception {
        Mockito.when(itemClient.updateItem(eq(1L), eq(1L), any(ItemDto.class)))
                .thenReturn(ResponseEntity.ok(itemDto));

        mockMvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemDto.getId()))
                .andExpect(jsonPath("$.name").value(itemDto.getName()));
    }

    @Test
    void getItem_ReturnsOk() throws Exception {
        Mockito.when(itemClient.getItem(1L, 1L))
                .thenReturn(ResponseEntity.ok(itemDto));

        mockMvc.perform(get("/items/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemDto.getId()))
                .andExpect(jsonPath("$.name").value(itemDto.getName()));
    }

    @Test
    void getOwnerItems_ReturnsOk() throws Exception {
        Mockito.when(itemClient.getOwnerItems(1L))
                .thenReturn(ResponseEntity.ok(List.of(itemDto)));

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(itemDto.getId()));
    }

    @Test
    void searchItems_ReturnsOk() throws Exception {
        Mockito.when(itemClient.searchItems(1L, "drill"))
                .thenReturn(ResponseEntity.ok(List.of(itemDto)));

        mockMvc.perform(get("/items/search")
                        .header("X-Sharer-User-Id", 1L)
                        .param("text", "drill"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(itemDto.getId()));
    }

    @Test
    void addComment_ReturnsOk() throws Exception {
        Mockito.when(itemClient.addComment(eq(1L), eq(1L), any(CommentDto.class)))
                .thenReturn(ResponseEntity.ok(commentDto));

        mockMvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(commentDto.getId()))
                .andExpect(jsonPath("$.text").value(commentDto.getText()));
    }
}