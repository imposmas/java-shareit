package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Тестирование контроллера {@link ItemRequestController}.
 *
 * <p>
 * Основные направления тестирования:
 * <ul>
 *     <li>Проверка корректности работы эндпоинтов REST API с помощью {@link MockMvc}</li>
 *     <li>Взаимодействие с клиентом {@link ItemRequestClient} через Mockito</li>
 * </ul>
 *
 * <p>
 * Проверяются следующие сценарии:
 * <ul>
 *     <li>Создание нового запроса на вещь</li>
 *     <li>Получение всех запросов пользователя</li>
 *     <li>Получение всех запросов, кроме текущего пользователя (с пагинацией)</li>
 *     <li>Получение конкретного запроса по ID</li>
 * </ul>
 */

@WebMvcTest(ItemRequestController.class)
class ItemRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemRequestClient requestClient;

    private ItemRequestDto requestDto;
    private ItemRequestResponseDto responseItem;

    @BeforeEach
    void setUp() {
        responseItem = new ItemRequestResponseDto(1L, "Item name", 1L);

        requestDto = ItemRequestDto.builder()
                .id(1L)
                .description("Need a drill")
                .requestorId(1L)
                .created(LocalDateTime.now())
                .items(List.of(responseItem))
                .build();
    }

    @Test
    void createRequest_ReturnsOk() throws Exception {
        Mockito.when(requestClient.createRequest(eq(1L), any(ItemRequestDto.class)))
                .thenReturn(ResponseEntity.ok(requestDto));

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(requestDto.getId()))
                .andExpect(jsonPath("$.description").value(requestDto.getDescription()));
    }

    @Test
    void getRequestsByUser_ReturnsOk() throws Exception {
        Mockito.when(requestClient.getRequestsByUser(1L))
                .thenReturn(ResponseEntity.ok(List.of(requestDto)));

        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(requestDto.getId()));
    }

    @Test
    void getAllRequests_ReturnsOk() throws Exception {
        Mockito.when(requestClient.getAllRequests(1L, 0, 10))
                .thenReturn(ResponseEntity.ok(List.of(requestDto)));

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1L)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(requestDto.getId()));
    }

    @Test
    void getRequestById_ReturnsOk() throws Exception {
        Mockito.when(requestClient.getRequestById(1L, 1L))
                .thenReturn(ResponseEntity.ok(requestDto));

        mockMvc.perform(get("/requests/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(requestDto.getId()));
    }
}