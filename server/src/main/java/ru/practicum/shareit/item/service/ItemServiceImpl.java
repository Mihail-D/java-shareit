package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.enums.Status;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.util.UnionService;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemRequestRepository itemRequestRepository;
    private final UnionService unionService;

    @Transactional
    @Override
    public ItemDto addItem(long userId, ItemDto itemDto) {

        unionService.checkUser(userId);

        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new NoSuchElementException("User with id " + userId + " not found");
        }
        User user = userOptional.get();
        Item item = ItemMapper.toItem(itemDto, user);

        if (itemDto.getRequestId() != null) {
            unionService.checkRequest(itemDto.getRequestId());
            Optional<ItemRequest> itemRequestOptional = itemRequestRepository.findById(itemDto.getRequestId());
            if (itemRequestOptional.isEmpty()) {
                throw new NoSuchElementException("Item request with id " + itemDto.getRequestId() + " not found");
            }
            item.setRequest(itemRequestOptional.get());
        }
        itemRepository.save(item);

        return ItemMapper.toItemDto(item);
    }


    @Transactional
    @Override
    public ItemDto updateItem(ItemDto itemDto, long itemId, long userId) {

        unionService.checkUser(userId);
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new NoSuchElementException("User with id " + userId + " not found");
        }
        User user = userOptional.get();

        unionService.checkItem(itemId);
        Item item = ItemMapper.toItem(itemDto, user);

        item.setId(itemId);

        if (!itemRepository.getByOwnerId(userId).contains(item)) {
            throw new NotFoundException(Item.class, "the item was not found with the user id " + userId);
        }

        Optional<Item> newItemOptional = itemRepository.findById(item.getId());
        if (newItemOptional.isEmpty()) {
            throw new NoSuchElementException("Item with id " + item.getId() + " not found");
        }
        Item newItem = newItemOptional.get();

        if (item.getName() != null) {
            newItem.setName(item.getName());
        }

        if (item.getDescription() != null) {
            newItem.setDescription(item.getDescription());
        }

        if (item.getAvailable() != null) {
            newItem.setAvailable(item.getAvailable());
        }

        itemRepository.save(newItem);

        return ItemMapper.toItemDto(newItem);
    }


    @Transactional(readOnly = true)
    @Override
    public ItemDto getItemById(long itemId, long userId) {

        unionService.checkItem(itemId);
        Optional<Item> itemOptional = itemRepository.findById(itemId);
        if (itemOptional.isEmpty()) {
            throw new NoSuchElementException("Item with id " + itemId + " not found");
        }
        Item item = itemOptional.get();

        ItemDto itemDto = ItemMapper.toItemDto(item);

        unionService.checkUser(userId);

        if (item.getOwner().getId() == userId) {

            Optional<Booking> lastBooking = bookingRepository.getFirstByItemIdAndStatusAndStartBeforeOrderByStartDesc(itemId, Status.APPROVED, LocalDateTime.now());
            Optional<Booking> nextBooking = bookingRepository.getFirstByItemIdAndStatusAndStartAfterOrderByStartAsc(itemId, Status.APPROVED, LocalDateTime.now());

            if (lastBooking.isPresent()) {
                itemDto.setLastBooking(BookingMapper.toBookingShortDto(lastBooking.get()));
            } else {
                itemDto.setLastBooking(null);
            }

            if (nextBooking.isPresent()) {
                itemDto.setNextBooking(BookingMapper.toBookingShortDto(nextBooking.get()));
            } else {
                itemDto.setNextBooking(null);
            }
        }

        List<Comment> commentList = commentRepository.getAllByItemId(itemId);

        if (!commentList.isEmpty()) {
            itemDto.setComments(CommentMapper.toCommentDtoList(commentList));
        } else {
            itemDto.setComments(Collections.emptyList());
        }

        return itemDto;
    }


    @Transactional(readOnly = true)
    @Override
    public List<ItemDto> getItemsUser(long userId, Integer from, Integer size) {

        unionService.checkUser(userId);
        PageRequest pageRequest = PageRequest.of(from / size, size);

        List<ItemDto> resultList = new ArrayList<>();

        for (ItemDto itemDto : ItemMapper.toItemDtoList(itemRepository.getByOwnerIdOrderById(userId, pageRequest))) {

            Optional<Booking> lastBooking = bookingRepository.getFirstByItemIdAndStatusAndStartBeforeOrderByStartDesc(itemDto.getId(), Status.APPROVED, LocalDateTime.now());
            Optional<Booking> nextBooking = bookingRepository.getFirstByItemIdAndStatusAndStartAfterOrderByStartAsc(itemDto.getId(), Status.APPROVED, LocalDateTime.now());

            if (lastBooking.isPresent()) {
                itemDto.setLastBooking(BookingMapper.toBookingShortDto(lastBooking.get()));
            } else {
                itemDto.setLastBooking(null);
            }

            if (nextBooking.isPresent()) {
                itemDto.setNextBooking(BookingMapper.toBookingShortDto(nextBooking.get()));
            } else {
                itemDto.setNextBooking(null);
            }

            resultList.add(itemDto);
        }

        for (ItemDto itemDto : resultList) {

            List<Comment> commentList = commentRepository.getAllByItemId(itemDto.getId());

            if (!commentList.isEmpty()) {
                itemDto.setComments(CommentMapper.toCommentDtoList(commentList));
            } else {
                itemDto.setComments(Collections.emptyList());
            }
        }

        return resultList;
    }

    @Transactional(readOnly = true)
    @Override
    public List<ItemDto> searchItem(String text, Integer from, Integer size) {

        PageRequest pageRequest = PageRequest.of(from / size, size);

        if (text.isEmpty()) {
            return Collections.emptyList();
        } else {
            return ItemMapper.toItemDtoList(itemRepository.search(text, pageRequest));
        }
    }

    @Transactional
    @Override
    public CommentDto addComment(long userId, long itemId, CommentDto commentDto) {

        unionService.checkUser(userId);
        User user = userRepository.findById(userId).get();

        unionService.checkItem(itemId);
        Item item = itemRepository.findById(itemId).get();

        LocalDateTime dateTime = LocalDateTime.now();

        Optional<Booking> booking = bookingRepository.getFirstByItemIdAndBookerIdAndStatusAndEndBefore(itemId, userId, Status.APPROVED, dateTime);

        if (booking.isEmpty()) {
            throw new ValidationException("User " + userId + " not booking this item " + itemId);
        }

        Comment comment = CommentMapper.toComment(commentDto, item, user, dateTime);

        return CommentMapper.toCommentDto(commentRepository.save(comment));
    }
}