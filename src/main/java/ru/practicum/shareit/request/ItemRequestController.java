package ru.practicum.shareit.request;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/requests")
public class ItemRequestController {

    private final ItemRequestService requestService;
    private static final String USER_HEADER = "X-Sharer-User-Id";

    public ItemRequestController(ItemRequestService requestService) {
        this.requestService = requestService;
    }

    @PostMapping
    public ResponseEntity<ItemRequestDto> createRequest(@RequestHeader(USER_HEADER) Long userId,
                                                        @RequestBody ItemRequestDto dto) {
        ItemRequestDto created = requestService.createRequest(userId, dto);
        return ResponseEntity.ok(created);
    }

    @GetMapping
    public ResponseEntity<List<ItemRequestDto>> getRequestsByUser(@RequestHeader(USER_HEADER) Long userId) {
        log.info("ItemRequestController getRequestsByUser: userId = {}", userId);
        List<ItemRequestDto> list = requestService.getRequestsByUser(userId);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/all")
    public ResponseEntity<List<ItemRequestDto>> getAllRequests() {
        List<ItemRequestDto> list = requestService.getAllRequests();
        return ResponseEntity.ok(list);
    }
}