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
        Item item = itemRepository.findById(bookingDto.getItemId()).get();

        unionService.checkUser(userId);
        User user = userRepository.findById(userId).get();

        Booking booking = BookingMapper.toBooking(bookingDto);
        booking.setItem(item);
        booking.setBooker(user);

        if (item.getOwner().equals(user)) {
            throw new NotFoundException(User.class, "Owner " + userId + " can't book his item");
        }
        if (!item.getAvailable()) {
            throw new ValidationException("Item " + item.getId() + " is booked");
        }
        if (booking.getStart().isAfter(booking.getEnd())) {
            throw new ValidationException("Start cannot be later than end");
        }
        if (booking.getStart().isEqual(booking.getEnd())) {
            throw new ValidationException("Start cannot be equal than end");
        }

        bookingRepository.save(booking);

        return BookingMapper.toBookingDto(booking);
    }

    @Transactional
    @Override
    public BookingOutDto approveBooking(long userId, long bookingId, Boolean approved) {

        unionService.checkBooking(bookingId);
        Booking booking = bookingRepository.findById(bookingId).get();

        if (booking.getItem().getOwner().getId() != userId) {
            throw new NotFoundException(User.class, "Only owner " + userId + " items can change booking status");
        }

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

    @Transactional(readOnly = true)
    @Override
    public BookingOutDto getBookingById(long userId, long bookingId) {

        unionService.checkBooking(bookingId);
        Booking booking = bookingRepository.findById(bookingId).get();

        unionService.checkUser(userId);

        if (booking.getBooker().getId() == userId || booking.getItem().getOwner().getId() == userId) {
            return BookingMapper.toBookingDto(booking);
        } else {
            throw new NotFoundException(User.class, "To get information about the reservation, the car of the reservation or the owner {} " + userId + "of the item can");
        }
    }

    @Transactional(readOnly = true)
    @Override
    public List<BookingOutDto> getAllBookingsByBookerId(long userId, String state, Integer from, Integer size) {

        unionService.checkUser(userId);
        PageRequest pageRequest = PageRequest.of(from / size, size);

        Page<Booking> bookings = null;

        State bookingState = State.getEnumValue(state);

        switch (bookingState) {
            case ALL:
                bookings = bookingRepository.getAllByBookerIdOrderByStartDesc(userId, pageRequest);
                break;
            case CURRENT:
                bookings = bookingRepository.getAllByBookerIdAndStartBeforeAndEndAfterOrderByStartAsc(userId, LocalDateTime.now(), LocalDateTime.now(), pageRequest);
                break;
            case PAST:
                bookings = bookingRepository.getAllByBookerIdAndEndBeforeOrderByStartDesc(userId, LocalDateTime.now(), pageRequest);
                break;
            case FUTURE:
                bookings = bookingRepository.getAllByBookerIdAndStartAfterOrderByStartDesc(userId, LocalDateTime.now(), pageRequest);
                break;
            case WAITING:
                bookings = bookingRepository.getAllByBookerIdAndStatusOrderByStartDesc(userId, Status.WAITING, pageRequest);
                break;
            case REJECTED:
                bookings = bookingRepository.getAllByBookerIdAndStatusOrderByStartDesc(userId, Status.REJECTED, pageRequest);
                break;

        }
        return BookingMapper.toBookingDtoList(bookings);
    }

    @Transactional(readOnly = true)
    @Override
    public List<BookingOutDto> getAllBookingsForAllItemsByOwnerId(long userId, String state, Integer from, Integer size) {

        unionService.checkUser(userId);
        PageRequest pageRequest = PageRequest.of(from / size, size);

        if (itemRepository.getByOwnerId(userId).isEmpty()) {
            throw new ValidationException("User does not have items to booking");
        }
        Page<Booking> bookings = null;

        State bookingState = State.getEnumValue(state);

        switch (bookingState) {
            case ALL:
                bookings = bookingRepository.getAllByItemOwnerIdOrderByStartDesc(userId, pageRequest);
                break;
            case CURRENT:
                bookings = bookingRepository.getAllByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartAsc(userId, LocalDateTime.now(), LocalDateTime.now(), pageRequest);
                break;
            case PAST:
                bookings = bookingRepository.getAllByItemOwnerIdAndEndBeforeOrderByStartDesc(userId, LocalDateTime.now(), pageRequest);
                break;
            case FUTURE:
                bookings = bookingRepository.getAllByItemOwnerIdAndStartAfterOrderByStartDesc(userId, LocalDateTime.now(), pageRequest);
                break;
            case WAITING:
                bookings = bookingRepository.getAllByItemOwnerIdAndStatusOrderByStartDesc(userId, Status.WAITING, pageRequest);
                break;
            case REJECTED:
                bookings = bookingRepository.getAllByItemOwnerIdAndStatusOrderByStartDesc(userId, Status.REJECTED, pageRequest);
                break;
        }
        return BookingMapper.toBookingDtoList(bookings);
    }
}