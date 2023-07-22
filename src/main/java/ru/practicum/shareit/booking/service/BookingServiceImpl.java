package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingOutDto;
import ru.practicum.shareit.booking.enums.State;
import ru.practicum.shareit.booking.enums.Status;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.util.UnionService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final UnionService unionService;

    @Transactional
    @Override
    public BookingOutDto addBooking(BookingDto bookingDto, long userId) {
        unionService.checkItem(bookingDto.getItemId());
        Item item = itemRepository.findById(bookingDto.getItemId())
                .orElseThrow(() -> new NotFoundException(Item.class, "Item not found"));
        unionService.checkUser(userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(User.class, "User not found"));
        Booking booking = BookingMapper.toBooking(bookingDto);
        booking.setItem(item);
        booking.setBooker(user);
        validateBooking(item, user, booking);
        bookingRepository.save(booking);
        return BookingMapper.toBookingDto(booking);
    }

    private void validateBooking(Item item, User user, Booking booking) {
        if (item.getOwner().equals(user)) {
            throw new NotFoundException(User.class, "Owner " + user.getId() + " can't book his item");
        }
        if (!item.getAvailable()) {
            throw new ValidationException("Item " + item.getId() + " is booked");
        }
        if (booking.getStart().isAfter(booking.getEnd())) {
            throw new ValidationException("Start cannot be later than end");
        }
        if (booking.getStart().isEqual(booking.getEnd())) {
            throw new ValidationException("Start cannot be equal to end");
        }
    }

    @Transactional
    @Override
    public BookingOutDto approveBooking(long userId, long bookingId, Boolean approved) {
        unionService.checkBooking(bookingId);
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException(Booking.class, "Booking not found"));
        validateApproval(userId, booking);
        if (approved) {
            if (booking.getStatus().equals(Status.APPROVED)) {
                throw new ValidationException("Incorrect status update request");
            }
            booking.setStatus(Status.APPROVED);
        } else {
            booking.setStatus(Status.REJECTED);
        }
        bookingRepository.save(booking);
        return BookingMapper.toBookingDto(booking);
    }

    private void validateApproval(long userId, Booking booking) {
        if (booking.getItem().getOwner().getId() != userId) {
            throw new NotFoundException(User.class, "Only owner " + userId + " items can change booking status");
        }
    }

    @Transactional(readOnly = true)
    @Override
    public BookingOutDto getBookingById(long userId, long bookingId) {
        unionService.checkBooking(bookingId);
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException(Booking.class, "Booking not found"));
        unionService.checkUser(userId);
        validateGetBooking(userId, booking);
        return BookingMapper.toBookingDto(booking);
    }

    private void validateGetBooking(long userId, Booking booking) {
        if (booking.getBooker().getId() != userId && booking.getItem().getOwner().getId() != userId) {
            throw new NotFoundException(User.class, "To get information about the reservation, the car of the reservation, or the owner " + userId + " of the item can");
        }
    }


    @Transactional(readOnly = true)
    @Override
    public List<BookingOutDto> getAllBookingsByBookerId(long userId, String state, Integer from, Integer size) {

        unionService.checkUser(userId);
        PageRequest pageRequest = unionService.checkPageSize(from, size);

        Page<Booking> bookings = null;

        State bookingState = State.getEnumValue(state);

        switch (bookingState) {
            case ALL:
                bookings = bookingRepository.getByBookerIdOrderByStartDesc(userId, pageRequest);
                break;
            case CURRENT:
                bookings = bookingRepository.getByBookerIdAndStartBeforeAndEndAfterOrderByStartAsc(userId, LocalDateTime.now(), LocalDateTime.now(), pageRequest);
                break;
            case PAST:
                bookings = bookingRepository.getByBookerIdAndEndBeforeOrderByStartDesc(userId, LocalDateTime.now(), pageRequest);
                break;
            case FUTURE:
                bookings = bookingRepository.getByBookerIdAndStartAfterOrderByStartDesc(userId, LocalDateTime.now(), pageRequest);
                break;
            case WAITING:
                bookings = bookingRepository.getByBookerIdAndStatusOrderByStartDesc(userId, Status.WAITING, pageRequest);
                break;
            case REJECTED:
                bookings = bookingRepository.getByBookerIdAndStatusOrderByStartDesc(userId, Status.REJECTED, pageRequest);
                break;

        }
        return BookingMapper.toBookingDtoList(bookings);
    }

    @Transactional(readOnly = true)
    @Override
    public List<BookingOutDto> getAllBookingsForAllItemsByOwnerId(long userId, String state, Integer from, Integer size) {

        unionService.checkUser(userId);
        PageRequest pageRequest = unionService.checkPageSize(from, size);

        if (itemRepository.findByOwnerId(userId).isEmpty()) {
            throw new ValidationException("User does not have items to booking");
        }
        Page<Booking> bookings = null;

        State bookingState = State.getEnumValue(state);

        switch (bookingState) {
            case ALL:
                bookings = bookingRepository.getByItemOwnerIdOrderByStartDesc(userId, pageRequest);
                break;
            case CURRENT:
                bookings = bookingRepository.getByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartAsc(userId, LocalDateTime.now(), LocalDateTime.now(), pageRequest);
                break;
            case PAST:
                bookings = bookingRepository.getByItemOwnerIdAndEndBeforeOrderByStartDesc(userId, LocalDateTime.now(), pageRequest);
                break;
            case FUTURE:
                bookings = bookingRepository.getByItemOwnerIdAndStartAfterOrderByStartDesc(userId, LocalDateTime.now(), pageRequest);
                break;
            case WAITING:
                bookings = bookingRepository.getByItemOwnerIdAndStatusOrderByStartDesc(userId, Status.WAITING, pageRequest);
                break;
            case REJECTED:
                bookings = bookingRepository.getByItemOwnerIdAndStatusOrderByStartDesc(userId, Status.REJECTED, pageRequest);
                break;
        }
        return BookingMapper.toBookingDtoList(bookings);
    }
}