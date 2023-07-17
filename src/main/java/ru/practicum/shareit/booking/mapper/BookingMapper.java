package ru.practicum.shareit.booking.mapper;

import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingOutDto;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.booking.enums.Status;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.mappers.ItemMapper;
import ru.practicum.shareit.user.mapper.UserMapper;

import javax.inject.Singleton;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Singleton
@Component
@Mapper
public class BookingMapper {

    private static BookingMapper instance = new BookingMapper();
    private final ItemMapper itemMapper = ItemMapper.getInstance();

    BookingMapper() {
    }

    public static BookingMapper getInstance() {
        if (instance == null) {
            instance = new BookingMapper();
        }
        return instance;
    }


    public Booking returnBooking(BookingDto bookingDto) {
        Status status = bookingDto.getStatus() == null ? Status.WAITING : bookingDto.getStatus();
        return Booking.builder()
                .start(bookingDto.getStart())
                .end(bookingDto.getEnd())
                .status(status)
                .build();
    }

    public BookingShortDto returnBookingShortDto(Booking booking) {
        return BookingShortDto.builder()
                .id(booking.getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .bookerId(booking.getBooker().getId())
                .build();
    }

    public List<BookingOutDto> returnBookingDtoList(Iterable<Booking> bookings) {
        return StreamSupport.stream(bookings.spliterator(), false)
                .map(this::returnBookingDto)
                .collect(Collectors.toList());
    }

    public BookingOutDto returnBookingDto(Booking booking) {
        return BookingOutDto.builder()
                .id(booking.getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .status(booking.getStatus())
                .item(itemMapper.returnItemDto(booking.getItem()))
                .booker(UserMapper.returnUserDto(booking.getBooker()))
                .build();
    }

}

