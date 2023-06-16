package ru.practicum.shareit.item.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;

import javax.validation.Valid;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Slf4j
public class ItemController {

    private final ItemService itemService;

    @GetMapping
    public List<ItemDto> getAllItems(@RequestHeader("X-Sharer-User-Id") Long userId) {
        log.debug("Выполнено getAllItems with {}.", userId);
        List<ItemDto> items = ItemMapper.toItemDtoList(itemService.getAllItems(userId));
        log.debug("Executed getAllItems with {}.", items);
        return items;
    }

    @GetMapping("/{itemId}")
    public ItemDto getItemById(@RequestHeader("X-Sharer-User-Id") Long userId, @PathVariable Long itemId) {
        log.debug("Executed getItemById with {}.", itemId);
        ItemDto item = ItemMapper.toItemDto(itemService.getItemById(userId, itemId));
        log.debug("Result getItemById with {}.", item);
        return item;
    }

    @GetMapping("/search")
    public List<ItemDto> findItemByParams(@RequestParam(required = false) String text) {
        if (text == null || text.isBlank()) {
            log.debug("Executed findItemByParams text not found.");
            return Collections.emptyList();
        }
        log.debug("Executed findItemByParams with {}.", text);
        List<ItemDto> items = ItemMapper.toItemDtoList(itemService.getItemsByText(text));
        log.debug("Result findItemByParams with {}.", items);
        return items;
    }

    @PostMapping
    public ItemDto createItem(@RequestHeader("X-Sharer-User-Id") Long userId, @Valid @RequestBody ItemDto itemDto) {
        log.debug("Executed createItem with {}.", itemDto);
        Item item = ItemMapper.toItem(itemDto);
        Long requestId = itemDto.getRequest();
        Item createdItem = itemService.createItem(userId, item, requestId);
        ItemDto createdItemDto = ItemMapper.toItemDto(createdItem);
        log.debug("Result createItem with {}.", createdItemDto);
        return createdItemDto;
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @PathVariable Long itemId,
            @RequestBody ItemDto itemDto
    ) {
        log.debug("Executed updateItem with {}.", itemDto);
        Item item = ItemMapper.toItem(itemDto);
        Long requestId = itemDto.getRequest();
        Item updatedItem = itemService.updateItem(userId, itemId, item, requestId);
        ItemDto updatedItemDto = ItemMapper.toItemDto(updatedItem);
        log.debug("Result updateItem with {}.", updatedItemDto);
        return updatedItemDto;
    }

    @DeleteMapping("/{itemId}")
    public void deleteItem(@RequestHeader("X-Sharer-User-Id") Long userId, @PathVariable Long itemId) {
        log.debug("Executed deleteItem with {}.", itemId);
        itemService.deleteItemById(userId, itemId);
    }
}
