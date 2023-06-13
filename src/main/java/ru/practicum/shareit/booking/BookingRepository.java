package ru.practicum.shareit.booking;

import java.util.List;
import java.util.Optional;

public interface BookingRepository {

    List<Booking> findAllBookings();

    Optional<Booking> findBookingById(Long bookingId);

    Booking createBooking(Booking booking);

    Booking updateBooking(Booking booking);

    void deleteBookingById(Long bookingId);

    boolean bookingExists(Long bookingId);
}
