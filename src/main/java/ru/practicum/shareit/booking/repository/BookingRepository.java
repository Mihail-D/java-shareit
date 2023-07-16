package ru.practicum.shareit.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.booking.enums.Status;
import ru.practicum.shareit.booking.model.Booking;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> getByBookerIdOrderByStartDesc(long bookerId);

    List<Booking> getByBookerIdAndStartBeforeAndEndAfterOrderByStartAsc(long bookerId, LocalDateTime start, LocalDateTime end);

    List<Booking> getByBookerIdAndStatusOrderByStartDesc(long bookerId, Status status);

    List<Booking> getAllByBookerIdAndEndBeforeOrderByStartDesc(long bookerId, LocalDateTime dateTime);

    List<Booking> getAllByBookerIdAndStartAfterOrderByStartDesc(long bookerId, LocalDateTime dateTime);

    Optional<Booking> getFirstByItemIdAndStatusAndStartBeforeOrderByStartDesc(long itemId, Status status, LocalDateTime dateTime);

    Optional<Booking> getFirstByItemIdAndStatusAndStartAfterOrderByStartAsc(long itemId, Status status, LocalDateTime dateTime);

    Optional<Booking> getFirstByItemIdAndBookerIdAndStatusAndEndBefore(long itemId, long bookerId, Status status, LocalDateTime dateTime);

    List<Booking> getAllByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartAsc(long ownerId, LocalDateTime start, LocalDateTime end);

    List<Booking> getAllByItemOwnerIdAndStatusOrderByStartDesc(long ownerId, Status status);

    List<Booking> getAllByItemOwnerIdOrderByStartDesc(long ownerId);

    List<Booking> getAllByItemOwnerIdAndEndBeforeOrderByStartDesc(long ownerId, LocalDateTime dateTime);

    List<Booking> getAllByItemOwnerIdAndStartAfterOrderByStartDesc(long ownerId, LocalDateTime dateTime);
}
