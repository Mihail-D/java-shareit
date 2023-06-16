package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.AlreadyExistsException;
import ru.practicum.shareit.exception.NotFoundException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;

    @Override
    public List<BookingDto> findAllBookings() {
        log.debug("Finding all bookings.");
        List<Booking> bookings = bookingRepository.findAllBookings();
        return bookings.stream().map(bookingMapper::toBookingDto).collect(Collectors.toList());
    }

    @Override
    public BookingDto findBookingById(Long bookingId) {
        Booking booking = bookingRepository.findBookingById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking", bookingId));
        log.debug("Finding booking by id: {}.", booking);
        return bookingMapper.toBookingDto(booking);
    }

    @Override
    public BookingDto createBooking(BookingDto bookingDto) {
        Booking booking = bookingMapper.toBooking(bookingDto);
        if (booking.getId() != null && bookingRepository.bookingExists(booking.getId())) {
            throw new AlreadyExistsException("Booking", booking.getId());
        }
        booking = bookingRepository.createBooking(booking);
        log.debug("Creating booking: {}.", booking);
        return bookingMapper.toBookingDto(booking);
    }

    @Override
    public BookingDto updateBooking(BookingDto bookingDto) {
        Booking booking = bookingMapper.toBooking(bookingDto);
        if (!bookingRepository.bookingExists(booking.getId())) {
            throw new NotFoundException("Booking", booking.getId());
        }
        booking = bookingRepository.updateBooking(booking);
        log.debug("Updating booking: {}.", booking);
        return bookingMapper.toBookingDto(booking);
    }

    @Override
    public void deleteBookingById(Long bookingId) {
        if (!bookingRepository.bookingExists(bookingId)) {
            throw new NotFoundException("Booking", bookingId);
        }
    }
}