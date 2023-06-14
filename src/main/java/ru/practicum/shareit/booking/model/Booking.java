package ru.practicum.shareit.booking.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class Booking {

    private Long id;
    private LocalDateTime start;
    private LocalDateTime end;
    private Item item;
    private User booker;
    private BookingStatus status;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Booking booking = (Booking) o;

        if (!getId().equals(booking.getId())) {
            return false;
        }
        if (!getStart().equals(booking.getStart())) {
            return false;
        }
        if (!getEnd().equals(booking.getEnd())) {
            return false;
        }
        if (!getItem().equals(booking.getItem())) {
            return false;
        }
        if (!getBooker().equals(booking.getBooker())) {
            return false;
        }
        return getStatus() == booking.getStatus();
    }

    @Override
    public int hashCode() {
        int result = getId().hashCode();
        result = 31 * result + getStart().hashCode();
        result = 31 * result + getEnd().hashCode();
        result = 31 * result + getItem().hashCode();
        result = 31 * result + getBooker().hashCode();
        result = 31 * result + getStatus().hashCode();
        return result;
    }
}