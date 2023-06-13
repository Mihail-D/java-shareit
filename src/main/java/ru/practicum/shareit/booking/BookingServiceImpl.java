package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.AlreadyExistsException;
import ru.practicum.shareit.exception.NotFoundException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRep;

    @Override
    public List<Booking> findAllBookings() {
        log.debug("BookingService: done findAllBookings.");
        return bookingRep.findAllBookings();
    }

    @Override
    public Booking findBookingById(Long bookingId) {
        Booking booking =  bookingRep.findBookingById(bookingId).orElseThrow(
                () -> new NotFoundException(Booking.class.toString(), bookingId)
        );
        log.debug("BookingService: done findBookingById - {}.", booking);
        return booking;
    }

    @Override
    public Booking createBooking(Booking booking) {
        if (booking.getId() != null && bookingRep.bookingExists(booking.getId())) {
            throw new AlreadyExistsException(Booking.class.toString(), booking.getId());
        }
        booking = bookingRep.createBooking(booking);
        log.debug("BookingService: done createBooking - {}.", booking);
        return booking;
    }

    @Override
    public Booking updateBooking(Booking booking) {
        if (!bookingRep.bookingExists(booking.getId())) {
            throw new NotFoundException(Booking.class.toString(), booking.getId());
        }
        log.debug("BookingService: done updateBooking - {}.", booking);
        return bookingRep.updateBooking(booking);
    }

    @Override
    public void deleteBookingById(Long bookingId) {
        if (!bookingRep.bookingExists(bookingId)) {
            throw new NotFoundException(Booking.class.toString(), bookingId);
        }
        bookingRep.deleteBookingById(bookingId);
        log.debug("BookingService: done deleteBookingById - ID {}.", bookingId);
    }
}
