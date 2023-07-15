package ru.practicum.shareit.item.repository;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.OperationAccessException;
import ru.practicum.shareit.item.model.Item;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class InMemoryItemRepository implements ItemRepository {

    private final Map<Long, Item> items = new HashMap<>();
    private Long nextId = 1L;

    public InMemoryItemRepository() {

    }

    @Override
    public List<Item> getAllItems(Long userId) {
        return items.values().stream()
                .filter(item -> userId.equals(item.getOwner().getId()))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Item> getItemById(Long itemId) {
        return Optional.ofNullable(items.get(itemId));
    }

    @Override
    public List<Item> getItemsByText(String text) {
        return items.values().stream()
                .filter(Item::getAvailable)
                .filter(item ->
                        item.getName().toLowerCase().contains(text)
                                || item.getDescription().toLowerCase().contains(text)
                )
                .collect(Collectors.toList());
    }

    @Override
    public Item createItem(Long userId, Item item) {
        if (item.getId() == null) {
            item.setId(nextId++);
        }
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Item updateItem(Long userId, Long itemId, Item item) {
        Item oldItem = getItemById(itemId).orElseThrow(
                () -> new NotFoundException(Item.class.toString(), itemId)
        );
        if (!userId.equals(oldItem.getOwner().getId())) {
            throw new OperationAccessException(userId);
        }
        if (item.getName() != null) {
            oldItem.setName(item.getName());
        }
        if (item.getDescription() != null) {
            oldItem.setDescription(item.getDescription());
        }
        if (item.getAvailable() != null) {
            oldItem.setAvailable(item.getAvailable());
        }
        return oldItem;
    }

    @Override
    public void deleteItemById(Long itemId) {
        if (!items.containsKey(itemId)) {
            throw new NotFoundException(Item.class.toString(), itemId);
        }
        items.remove(itemId);
    }

    @Override
    public boolean itemExists(Long itemId) {
        return items.containsKey(itemId);
    }
}
