package ru.practicum.shareit.booking.repository;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.booking.model.Booking;

import java.util.*;

@Repository
public class InMemoryBookingRepository implements BookingRepository {

    private final Map<Long, Booking> bookings = new HashMap<>();
    private Long currentId = 1L;

    public List<Booking> findAllBookings() {
        return new ArrayList<>(bookings.values());
    }

    public Optional<Booking> findBookingById(Long bookingId) {
        return Optional.ofNullable(bookings.get(bookingId));
    }

    public Booking createBooking(Booking booking) {
        if (booking.getId() == null) {
            booking.setId(currentId++);
        }
        bookings.put(booking.getId(), booking);
        return booking;
    }

    public Booking updateBooking(Booking booking) {
        if (booking.getId() == null || !bookings.containsKey(booking.getId())) {
            throw new IllegalArgumentException("Booking ID not found");
        }
        bookings.put(booking.getId(), booking);
        return booking;
    }

    public void deleteBookingById(Long bookingId) {
        bookings.remove(bookingId);
    }

    public boolean bookingExists(Long bookingId) {
        return bookings.containsKey(bookingId);
    }
}