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

    private final ItemRepository itemRepo;
    private final UserService userService;
    private final ItemRequestService requestService;

    @Override
    public List<Item> findAllItems(Long userId) {
        log.debug("ItemService: Executed findAllItems.");
        return itemRepo.findAllItems(userId);
    }

    @Override
    public Item findItemById(Long userId, Long itemId) {
        if (!itemRepo.itemExists(itemId)) {
            throw new NotFoundException(Item.class.toString(), itemId);
        }
        Item item = itemRepo.findItemById(itemId).orElseThrow(
                () -> new NotFoundException(Item.class.toString(), itemId)
        );
        log.debug("ItemService: Executed findItemById {}.", item);
        return item;
    }

    @Override
    public List<Item> findItemsByText(String text) {
        List<Item> searchedItems = itemRepo.findItemsByText(text.toLowerCase());
        log.debug("ItemService: Executed findItemsByText {}.", searchedItems);
        return searchedItems;
    }

    @Override
    public Item createItem(Long userId, Item item, Long requestId) {
        if (item.getId() != null && itemRepo.itemExists(item.getId())) {
            throw new AlreadyExistsException(Item.class.toString(), item.getId());
        }
        item.setOwner(userService.findUserById(userId));
        item.setRequest(requestId != null ? requestService.findItemRequestById(requestId) : null);
        item = itemRepo.createItem(userId, item);
        log.debug("ItemService: Executed createItem {}.", item);
        return item;
    }

    @Override
    public Item updateItem(Long userId, Long itemId, Item item, Long requestId) {
        if (!itemRepo.itemExists(itemId)) {
            throw new NotFoundException(Item.class.toString(), itemId);
        }
        item.setOwner(userService.findUserById(userId));
        item.setRequest(requestId != null ? requestService.findItemRequestById(requestId) : null);
        item = itemRepo.updateItem(userId, itemId, item);
        log.debug("ItemService: Executed updateItem {}.", item);
        return item;
    }

    @Override
    public void deleteItemById(Long userId, Long itemId) {
        if (!itemRepo.itemExists(itemId)) {
            throw new NotFoundException(Item.class.toString(), itemId);
        }
        itemRepo.deleteItemById(itemId);
        log.debug("ItemService: Executed deleteItemById id {}.", itemId);
    }
}
