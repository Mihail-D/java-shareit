package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;

public interface ItemRequestService {

    List<ItemRequestDto> getRequests(long userId);

    ItemRequestDto addRequest(ItemRequestDto itemRequestDto, long userId);

    ItemRequestDto addItemsToRequest(ItemRequest itemRequest);

    ItemRequestDto getRequestById(long userId, long requestId);

    List<ItemRequestDto> getAllRequests(Long userId, Integer from, Integer size);
}