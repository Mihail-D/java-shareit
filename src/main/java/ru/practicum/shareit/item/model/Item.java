package ru.practicum.shareit.item.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

@Data
@AllArgsConstructor
@Builder
public class Item {

    private Long id;
    private String name;
    private String description;
    private Boolean available;
    private User owner;
    private ItemRequest request;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Item item = (Item) o;

        if (!getId().equals(item.getId())) {
            return false;
        }
        if (!getName().equals(item.getName())) {
            return false;
        }
        if (!getDescription().equals(item.getDescription())) {
            return false;
        }
        if (!getAvailable().equals(item.getAvailable())) {
            return false;
        }
        if (!getOwner().equals(item.getOwner())) {
            return false;
        }
        return getRequest().equals(item.getRequest());
    }

    @Override
    public int hashCode() {
        int result = getId().hashCode();
        result = 31 * result + getName().hashCode();
        result = 31 * result + getDescription().hashCode();
        result = 31 * result + getAvailable().hashCode();
        result = 31 * result + getOwner().hashCode();
        result = 31 * result + getRequest().hashCode();
        return result;
    }
}
