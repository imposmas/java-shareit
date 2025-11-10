package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/items")
public class ItemController {

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    private static final String USER_HEADER = "X-Sharer-User-Id";

    @PostMapping
    public ResponseEntity<ItemDto> createItem(@RequestHeader(USER_HEADER) Long userId,
                                              @RequestBody ItemDto dto) {
        ItemDto created = itemService.createItem(userId, dto);
        return ResponseEntity.ok(created);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<ItemDto> updateItem(@RequestHeader(USER_HEADER) Long userId,
                                              @PathVariable Long itemId,
                                              @RequestBody ItemDto dto) {
        ItemDto updated = itemService.updateItem(userId, itemId, dto);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<ItemDto> getItem(@RequestHeader(USER_HEADER) Long userId,
                                           @PathVariable Long itemId) {
        log.info("ItemController getItem: userId = {}", userId);
        ItemDto dto = itemService.getItemById(userId, itemId);
        return ResponseEntity.ok(dto);
    }

    @GetMapping
    public ResponseEntity<List<ItemDto>> getOwnerItems(@RequestHeader(USER_HEADER) Long userId) {
        log.info("ItemController getOwnerItems: userId = {}", userId);
        List<ItemDto> list = itemService.getItemsByOwner(userId);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ItemDto>> searchItems(@RequestParam(name = "text", required = false) String text) {
        log.info("ItemController searchItems: text = {}", text);
        List<ItemDto> result = itemService.searchItems(text);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<CommentDto> addComment(@RequestHeader(USER_HEADER) Long userId,
                                                 @PathVariable Long itemId,
                                                 @RequestBody CommentDto commentDto) {
        CommentDto created = itemService.addComment(userId, itemId, commentDto);
        return ResponseEntity.ok(created);
    }
}