package ru.practicum.shareit.item.mapper;

import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

@Singleton
@Component
@Mapper
public class ItemMapper {

    public static ItemDto toItemDto(Item item) {
        return new ItemDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                item.getRequest() != null ? item.getRequest().getId() : null
        );
    }

    public static Item toItem(ItemDto itemDto) {
        return Item.builder()
                .id(itemDto.getId())
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .available(itemDto.getAvailable())
                .build();
    }

    public static List<ItemDto> toItemDtoList(List<Item> items) {
        List<ItemDto> list = new ArrayList<>();
        for (Item item : items) {
            ItemDto itemDto = toItemDto(item);
            list.add(itemDto);
        }
        return list;
    }
}
