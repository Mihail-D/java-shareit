package ru.practicum.shareit.item.repository;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.OperationAccessException;
import ru.practicum.shareit.item.model.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class ItemRepositoryInMemory implements ItemRepository {

    private final List<Item> items = new ArrayList<>();
    private Long currentId = 1L;

    @Override
    public List<Item> findAllItems(Long userId) {
        return items.stream()
                .filter(item -> userId.equals(item.getOwner().getId()))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Item> findItemById(Long itemId) {
        return items.stream()
                .filter(item -> itemId.equals(item.getId()))
                .findFirst();
    }

    @Override
    public List<Item> findItemsByText(String text) {
        return items.stream()
                .filter(Item::getAvailable)
                .filter(item ->
                        item.getName().toLowerCase().contains(text)
                                || item.getDescription().toLowerCase().contains(text)
                )
                .collect(Collectors.toList());
    }

    @Override
    public Item createItem(Long userId, Item item) {
        item.setId(currentId++);
        items.add(item);
        return item;
    }

    private void checkUserId(Long userId, Item itemToCheck) {
        if (!userId.equals(itemToCheck.getOwner().getId())) {
            throw new OperationAccessException(userId);
        }
    }

    @Override
    public Item updateItem(Long userId, Long itemId, Item updatedItem) {
        Item itemToUpdate = findItemById(itemId)
                .orElseThrow(() -> new NotFoundException(Item.class.toString(), itemId));
        checkUserId(userId, itemToUpdate);
        itemToUpdate.setName(updatedItem.getName());
        itemToUpdate.setDescription(updatedItem.getDescription());
        itemToUpdate.setAvailable(updatedItem.getAvailable());
        return itemToUpdate;
    }

    @Override
    public void deleteItemById(Long itemId) {
        Item itemToDelete = findItemById(itemId)
                .orElseThrow(() -> new NotFoundException(Item.class.toString(), itemId));
        items.remove(itemToDelete);
    }

    @Override
    public boolean itemExists(Long itemId) {
        return items.stream()
                .anyMatch(item -> itemId.equals(item.getId()));
    }
}
