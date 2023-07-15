package ru.practicum.shareit.request.mapper;

import org.mapstruct.Mapper;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;

@Mapper
public class ItemRequestMapper {

    private static ItemRequestMapper instance;

    private ItemRequestMapper() {
    }

    public static synchronized ItemRequestMapper getInstance() {
        if (instance == null) {
            instance = new ItemRequestMapper();
        }
        return instance;
    }

    public static ItemRequestDto toItemRequestDto(ItemRequest itemRequest) {
        return new ItemRequestDto(
                itemRequest.getId(),
                itemRequest.getDescription(),
                itemRequest.getCreator(),
                itemRequest.getCreated()
        );
    }

    public static ItemRequest toItemRequest(ItemRequestDto itemRequestDto) {
        return new ItemRequest(
                itemRequestDto.getId(),
                itemRequestDto.getDescription(),
                itemRequestDto.getCreator(),
                itemRequestDto.getCreated()
        );
    }
}
