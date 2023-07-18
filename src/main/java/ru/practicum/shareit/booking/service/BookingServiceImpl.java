package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
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
import java.util.Optional;

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
        validateBooking(bookingDto, userId);
        Booking booking = createBooking(bookingDto, userId);
        bookingRepository.save(booking);
        return BookingMapper.toBookingDto(booking);
    }

    private void validateBooking(BookingDto bookingDto, long userId) {
        checkItemAndUser(bookingDto.getItemId(), userId);
        Item item = getItem(bookingDto.getItemId());
        User user = getUser(userId);
        checkIfOwnerCanBook(item, user);
        checkIfItemIsAvailable(item);
    }

    private void checkItemAndUser(long itemId, long userId) {
        unionService.checkItem(itemId);
        unionService.checkUser(userId);
    }

    private Item getItem(long itemId) {
        return itemRepository.findById(itemId).orElseThrow(() -> new NotFoundException(Item.class, "Item not found"));
    }

    private User getUser(long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new NotFoundException(User.class, "User not found"));
    }

    private void checkIfOwnerCanBook(Item item, User user) {
        if (item.getOwner().equals(user)) {
            throw new NotFoundException(User.class, "Owner " + user.getId() + " can't book his item");
        }
    }

    private void checkIfItemIsAvailable(Item item) {
        if (!item.getAvailable()) {
            throw new ValidationException("Item " + item.getId() + " is booked");
        }
    }

    private Booking createBooking(BookingDto bookingDto, long userId) {
        Item item = getItem(bookingDto.getItemId());
        User user = getUser(userId);
        Booking booking = createAndSetBooking(bookingDto, item, user);
        validateBookingDates(booking);
        return booking;
    }

    private Booking createAndSetBooking(BookingDto bookingDto, Item item, User user) {
        Booking booking = BookingMapper.toBooking(bookingDto);
        booking.setItem(item);
        booking.setBooker(user);
        return booking;
    }

    private void validateBookingDates(Booking booking) {
        if (booking.getStart().isAfter(booking.getEnd())) {
            throw new ValidationException("Start cannot be later than end");
        }
        if (booking.getStart().isEqual(booking.getEnd())) {
            throw new ValidationException("Start cannot be equal than end");
        }
    }

    @Transactional
    @Override
    public BookingOutDto approveBooking(long userId, long bookingId, Boolean approved) {
        validateBookingApproval(userId, bookingId);
        Booking booking = updateAndSaveBookingStatus(bookingId, approved);
        return BookingMapper.toBookingDto(booking);
    }

    private Booking updateAndSaveBookingStatus(long bookingId, Boolean approved) {
        Booking booking = updateBookingStatus(bookingId, approved);
        bookingRepository.save(booking);
        return booking;
    }

    private void validateBookingApproval(long userId, long bookingId) {
        unionService.checkBooking(bookingId);
        Optional<Booking> bookingOptional = bookingRepository.findById(bookingId);
        if (bookingOptional.isPresent()) {
            Booking booking = bookingOptional.get();
            if (booking.getItem().getOwner().getId() != userId) {
                throw new NotFoundException(User.class, "Only owner " + userId + " items can change booking status");
            }
        } else {
            throw new NotFoundException(Booking.class, "Booking not found with id: " + bookingId);
        }
    }


    private Booking updateBookingStatus(long bookingId, Boolean approved) {
        Optional<Booking> bookingOptional = bookingRepository.findById(bookingId);
        if (bookingOptional.isPresent()) {
            Booking booking = bookingOptional.get();
            if (approved) {
                if (booking.getStatus().equals(Status.APPROVED)) {
                    throw new ValidationException("Incorrect status update request");
                }
                booking.setStatus(Status.APPROVED);
            } else {
                booking.setStatus(Status.REJECTED);
            }
            return booking;
        } else {
            throw new NotFoundException(Booking.class, "Booking not found with id: " + bookingId);
        }
    }


    @Transactional(readOnly = true)
    @Override
    public BookingOutDto getBookingById(long userId, long bookingId) {
        validateBookingAccess(userId, bookingId);
        Optional<Booking> bookingOptional = bookingRepository.findById(bookingId);
        if (bookingOptional.isPresent()) {
            Booking booking = bookingOptional.get();
            return BookingMapper.toBookingDto(booking);
        } else {
            throw new NotFoundException(Booking.class, "Booking not found with id: " + bookingId);
        }
    }


    private void validateBookingAccess(long userId, long bookingId) {
        unionService.checkBooking(bookingId);
        unionService.checkUser(userId);
        Optional<Booking> bookingOptional = bookingRepository.findById(bookingId);
        if (bookingOptional.isPresent()) {
            Booking booking = bookingOptional.get();
            if (booking.getBooker().getId() != userId && booking.getItem().getOwner().getId() != userId) {
                throw new NotFoundException(User.class, "To get information about the reservation, the car of the reservation or the owner {} " + userId + "of the item can");
            }
        } else {
            throw new NotFoundException(Booking.class, "Booking not found with id: " + bookingId);
        }
    }


    @Transactional(readOnly = true)
    @Override
    public List<BookingOutDto> getAllBookingsByBookerId(long userId, String state) {
        unionService.checkUser(userId);
        List<Booking> bookings = getBookingsByState(userId, state);
        assert bookings != null;
        return BookingMapper.toBookingDtoList(bookings);
    }

    private List<Booking> getBookingsByState(long userId, String state) {
        State bookingState = State.getEnumValue(state);
        switch (bookingState) {
            case ALL:
                return bookingRepository.getByBookerIdOrderByStartDesc(userId);
            case CURRENT:
                return bookingRepository.getByBookerIdAndStartBeforeAndEndAfterOrderByStartAsc(userId, LocalDateTime.now(), LocalDateTime.now());
            case PAST:
                return bookingRepository.getAllByBookerIdAndEndBeforeOrderByStartDesc(userId, LocalDateTime.now());
            case FUTURE:
                return bookingRepository.getAllByBookerIdAndStartAfterOrderByStartDesc(userId, LocalDateTime.now());
            case WAITING:
                return bookingRepository.getByBookerIdAndStatusOrderByStartDesc(userId, Status.WAITING);
            case REJECTED:
                return bookingRepository.getByBookerIdAndStatusOrderByStartDesc(userId, Status.REJECTED);
            default:
                return null;
        }
    }

    @Transactional(readOnly = true)
    @Override
    public List<BookingOutDto> getAllBookingsForAllItemsByOwnerId(long userId, String state) {
        validateOwner(userId);
        List<Booking> bookings = getBookingsByStateForOwner(userId, state);
        assert bookings != null;
        return BookingMapper.toBookingDtoList(bookings);
    }

    private void validateOwner(long userId) {
        unionService.checkUser(userId);
        if (itemRepository.findByOwnerId(userId).isEmpty()) {
            throw new ValidationException("User does not have items to booking");
        }
    }

    private List<Booking> getBookingsByStateForOwner(long userId, String state) {
        State bookingState = State.getEnumValue(state);
        switch (bookingState) {
            case ALL:
                return bookingRepository.getAllByItemOwnerIdOrderByStartDesc(userId);
            case CURRENT:
                return bookingRepository.getAllByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartAsc(userId, LocalDateTime.now(), LocalDateTime.now());
            case PAST:
                return bookingRepository.getAllByItemOwnerIdAndEndBeforeOrderByStartDesc(userId, LocalDateTime.now());
            case FUTURE:
                return bookingRepository.getAllByItemOwnerIdAndStartAfterOrderByStartDesc(userId, LocalDateTime.now());
            case WAITING:
                return bookingRepository.getAllByItemOwnerIdAndStatusOrderByStartDesc(userId, Status.WAITING);
            case REJECTED:
                return bookingRepository.getAllByItemOwnerIdAndStatusOrderByStartDesc(userId, Status.REJECTED);
            default:
                return null;
        }
    }
}

