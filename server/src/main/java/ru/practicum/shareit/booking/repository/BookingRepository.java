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

    Page<Booking> getAllByBookerIdOrderByStartDesc(long bookerId, Pageable pageable);

    Page<Booking> getAllByBookerIdAndStartBeforeAndEndAfterOrderByStartAsc(long bookerId, LocalDateTime start, LocalDateTime end, Pageable pageable);

    Page<Booking> getAllByBookerIdAndEndBeforeOrderByStartDesc(long bookerId, LocalDateTime dateTime, Pageable pageable);

    Page<Booking> getAllByBookerIdAndStartAfterOrderByStartDesc(long bookerId, LocalDateTime dateTime, Pageable pageable);

    Page<Booking> getAllByBookerIdAndStatusOrderByStartDesc(long bookerId, Status status, Pageable pageable);

    Page<Booking> getAllByItemOwnerIdOrderByStartDesc(long ownerId, Pageable pageable);

    Page<Booking> getAllByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartAsc(long ownerId, LocalDateTime start, LocalDateTime end, Pageable pageable);

    Page<Booking> getAllByItemOwnerIdAndEndBeforeOrderByStartDesc(long ownerId, LocalDateTime dateTime, Pageable pageable);

    Page<Booking> getAllByItemOwnerIdAndStartAfterOrderByStartDesc(long ownerId, LocalDateTime dateTime, Pageable pageable);

    Page<Booking> getAllByItemOwnerIdAndStatusOrderByStartDesc(long ownerId, Status status, Pageable pageable);

    Optional<Booking> getFirstByItemIdAndStatusAndStartAfterOrderByStartAsc(long itemId, Status status, LocalDateTime dateTime);

    Optional<Booking> getFirstByItemIdAndStatusAndStartBeforeOrderByStartDesc(long itemId, Status status, LocalDateTime dateTime);

    Optional<Booking> getFirstByItemIdAndBookerIdAndStatusAndEndBefore(long itemId, long bookerId, Status status, LocalDateTime dateTime);
}