package ru.practicum.shareit.booking.mapper;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingOutDto;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.booking.enums.Status;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.mappers.ItemMapper;
import ru.practicum.shareit.user.mapper.UserMapper;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class BookingMapper {

    public static Booking returnBooking(BookingDto bookingDto) {
        Status status = bookingDto.getStatus() == null ? Status.WAITING : bookingDto.getStatus();
        return Booking.builder()
                .start(bookingDto.getStart())
                .end(bookingDto.getEnd())
                .status(status)
                .build();
    }

    public static BookingShortDto returnBookingShortDto(Booking booking) {
        return BookingShortDto.builder()
                .id(booking.getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .bookerId(booking.getBooker().getId())
                .build();
    }

    public static List<BookingOutDto> returnBookingDtoList(Iterable<Booking> bookings) {
        return StreamSupport.stream(bookings.spliterator(), false)
                .map(BookingMapper::returnBookingDto)
                .collect(Collectors.toList());
    }

    public static BookingOutDto returnBookingDto(Booking booking) {
        return BookingOutDto.builder()
                .id(booking.getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .status(booking.getStatus())
                .item(ItemMapper.returnItemDto(booking.getItem()))
                .booker(UserMapper.returnUserDto(booking.getBooker()))
                .build();
    }

}

