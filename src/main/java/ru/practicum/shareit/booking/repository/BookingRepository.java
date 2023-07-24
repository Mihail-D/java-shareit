package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.booking.enums.Status;
import ru.practicum.shareit.booking.model.Booking;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> getFirstByItemIdAndStatusAndStartAfterOrderByStartAsc(long itemId, Status status, LocalDateTime dateTime);

    Optional<Booking> getFirstByItemIdAndStatusAndStartBeforeOrderByStartDesc(long itemId, Status status, LocalDateTime dateTime);

    Optional<Booking> getFirstByItemIdAndBookerIdAndStatusAndEndBefore(long itemId, long bookerId, Status status, LocalDateTime dateTime);

    Page<Booking> getByBookerIdOrderByStartDesc(long bookerId, Pageable pageable);

    Page<Booking> getByBookerIdAndStartBeforeAndEndAfterOrderByStartAsc(long bookerId, LocalDateTime start, LocalDateTime end, Pageable pageable);

    Page<Booking> getByBookerIdAndEndBeforeOrderByStartDesc(long bookerId, LocalDateTime dateTime, Pageable pageable);

    Page<Booking> getByBookerIdAndStartAfterOrderByStartDesc(long bookerId, LocalDateTime dateTime, Pageable pageable);

    Page<Booking> getByBookerIdAndStatusOrderByStartDesc(long bookerId, Status status, Pageable pageable);

    Page<Booking> getByItemOwnerIdOrderByStartDesc(long ownerId, Pageable pageable);

    Page<Booking> getByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartAsc(long ownerId, LocalDateTime start, LocalDateTime end, Pageable pageable);

    Page<Booking> getByItemOwnerIdAndEndBeforeOrderByStartDesc(long ownerId, LocalDateTime dateTime, Pageable pageable);

    Page<Booking> getByItemOwnerIdAndStartAfterOrderByStartDesc(long ownerId, LocalDateTime dateTime, Pageable pageable);

    Page<Booking> getByItemOwnerIdAndStatusOrderByStartDesc(long ownerId, Status status, Pageable pageable);
}
