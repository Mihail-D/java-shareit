package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.AlreadyExistsException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserService userService;
    private final ItemRequestService itemRequestService;

    @Override
    public List<Item> getAllItems(Long userId) {
        log.debug("ItemService: getAllItems executed.");
        return itemRepository.getAllItems(userId);
    }

    @Override
    public Item getItemById(Long userId, Long itemId) {
        if (!itemRepository.itemExists(itemId)) {
            throw new NotFoundException(Item.class.toString(), itemId);
        }
        Item item = itemRepository.getItemById(itemId).orElseThrow(
                () -> new NotFoundException(Item.class.toString(), itemId)
        );
        log.debug("ItemService: getItemById executed with {}.", item);
        return item;
    }

    @Override
    public List<Item> getItemsByText(String text) {
        List<Item> searchedItems = itemRepository.getItemsByText(text.toLowerCase());
        log.debug("ItemService: getItemsByText executed with {}.", searchedItems);
        return searchedItems;
    }

    @Override
    public Item createItem(Long userId, Item item, Long requestId) {
        if (item.getId() != null && itemRepository.itemExists(item.getId())) {
            throw new AlreadyExistsException(Item.class.toString(), item.getId());
        }
        item.setOwner(userService.findUserById(userId));
        item.setRequest(requestId != null ? itemRequestService.findItemRequestById(requestId) : null);
        item = itemRepository.createItem(userId, item);
        log.debug("ItemService: createItem executed with {}.", item);
        return item;
    }

    @Override
    public Item updateItem(Long userId, Long itemId, Item item, Long requestId) {
        if (!itemRepository.itemExists(itemId)) {
            throw new NotFoundException(Item.class.toString(), itemId);
        }
        item.setOwner(userService.findUserById(userId));
        item.setRequest(requestId != null ? itemRequestService.findItemRequestById(requestId) : null);
        item = itemRepository.updateItem(userId, itemId, item);
        log.debug("ItemService: updateItem executed with {}.", item);
        return item;
    }

    @Override
    public void deleteItemById(Long userId, Long itemId) {
        if (!itemRepository.itemExists(itemId)) {
            throw new NotFoundException(Item.class.toString(), itemId);
        }
        itemRepository.deleteItemById(itemId);
        log.debug("ItemService: deleteItemById executed with id {}.", itemId);
    }
}
