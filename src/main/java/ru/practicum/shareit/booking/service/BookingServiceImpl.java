package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.AlreadyExistsException;
import ru.practicum.shareit.exception.NotFoundException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;

    @Override
    public List<Booking> findAllBookings() {
        log.debug("Finding all bookings.");
        return bookingRepository.findAllBookings();
    }

    @Override
    public Booking findBookingById(Long bookingId) {
        Booking booking = bookingRepository.findBookingById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking", bookingId));
        log.debug("Finding booking by id: {}.", booking);
        return booking;
    }

    @Override
    public Booking createBooking(Booking booking) {
        if (booking.getId() != null && bookingRepository.bookingExists(booking.getId())) {
            throw new AlreadyExistsException("Booking", booking.getId());
        }
        booking = bookingRepository.createBooking(booking);
        log.debug("Creating booking: {}.", booking);
        return booking;
    }

    @Override
    public Booking updateBooking(Booking booking) {
        if (!bookingRepository.bookingExists(booking.getId())) {
            throw new NotFoundException("Booking", booking.getId());
        }
        log.debug("Updating booking: {}.", booking);
        return bookingRepository.updateBooking(booking);
    }

    @Override
    public void deleteBookingById(Long bookingId) {
        if (!bookingRepository.bookingExists(bookingId)) {
            throw new NotFoundException("Booking", bookingId);
        }
        bookingRepository.deleteBookingById(bookingId);
        log.debug("Deleting booking with id: {}.", bookingId);
    }
}