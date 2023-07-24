package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.mappers.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.util.UnionService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final ItemRequestRepository itemRequestRepository;
    private final UnionService unionService;

    @Transactional
    @Override
    public ItemRequestDto addRequest(ItemRequestDto itemRequestDto, long userId) {
        unionService.checkUser(userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(User.class, "User not found"));
        ItemRequest itemRequest = ItemRequestMapper.toItemRequest(itemRequestDto, user);
        itemRequest = itemRequestRepository.save(itemRequest);
        return ItemRequestMapper.toItemRequestDto(itemRequest);
    }

    @Override
    public List<ItemRequestDto> getRequests(long userId) {
        unionService.checkUser(userId);
        List<ItemRequest> itemRequests = itemRequestRepository.findByRequesterIdOrderByCreatedAsc(userId);
        return convertItemRequestsToDto(itemRequests);
    }

    @Override
    public List<ItemRequestDto> getAllRequests(Long userId, Integer from, Integer size) {
        PageRequest pageRequest = unionService.checkPageSize(from, size);
        Page<ItemRequest> itemRequests = itemRequestRepository.findByIdIsNotOrderByCreatedAsc(userId, pageRequest);
        return convertItemRequestsToDto(itemRequests.getContent());
    }

    private List<ItemRequestDto> convertItemRequestsToDto(List<ItemRequest> itemRequests) {
        List<ItemRequestDto> result = new ArrayList<>();
        for (ItemRequest itemRequest : itemRequests) {
            result.add(addItemsToRequest(itemRequest));
        }
        return result;
    }


    @Override
    public ItemRequestDto getRequestById(long userId, long requestId) {
        unionService.checkUser(userId);
        unionService.checkRequest(requestId);
        Optional<ItemRequest> optionalItemRequest = itemRequestRepository.findById(requestId);

        if (optionalItemRequest.isPresent()) {
            ItemRequest itemRequest = optionalItemRequest.get();
            return addItemsToRequest(itemRequest);
        } else {
            throw new NotFoundException(ItemRequest.class, "Request not found with ID: " + requestId);
        }
    }

    @Override
    public ItemRequestDto addItemsToRequest(ItemRequest itemRequest) {

        ItemRequestDto itemRequestDto = ItemRequestMapper.toItemRequestDto(itemRequest);
        List<Item> items = itemRepository.findByRequestId(itemRequest.getId());
        itemRequestDto.setItems(ItemMapper.toItemDtoList(items));

        return itemRequestDto;
    }
}