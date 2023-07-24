package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingOutDto;
import ru.practicum.shareit.booking.enums.Status;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mappers.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.util.UnionService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@SpringBootTest
public class BookingServiceTest {

    @Autowired
    private BookingService bookingService;

    @MockBean
    private UnionService unionService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private ItemRepository itemRepository;

    @MockBean
    private BookingRepository bookingRepository;

    private User firstUser;

    private User secondUser;

    private Item item;

    private ItemDto itemDto;

    private Booking firstBooking;

    private Booking secondBooking;

    private BookingDto bookingDto;

    @BeforeEach
    void beforeEach() {
        firstUser = User.builder()
                .id(1L)
                .name("Barby")
                .email("barby@gmail.com")
                .build();

        secondUser = User.builder()
                .id(2L)
                .name("Sam")
                .email("sam@gmail.com")
                .build();

        item = Item.builder()
                .id(1L)
                .name("slippers")
                .description("Step into comfort with our cozy slippers!")
                .available(true)
                .owner(firstUser)
                .build();

        itemDto = ItemMapper.toItemDto(item);

        firstBooking = Booking.builder()
                .id(1L)
                .start(LocalDateTime.now())
                .end(LocalDateTime.now())
                .item(item)
                .booker(firstUser)
                .status(Status.APPROVED)
                .build();

        secondBooking = Booking.builder()
                .id(2L)
                .start(LocalDateTime.now())
                .end(LocalDateTime.now())
                .item(item)
                .booker(firstUser)
                .status(Status.WAITING)
                .build();

        bookingDto = BookingDto.builder()
                .itemId(1L)
                .start(LocalDateTime.of(2023, 7, 5, 0, 0))
                .end(LocalDateTime.of(2023, 10, 12, 0, 0))
                .status(Status.APPROVED)
                .build();
    }

    @Test
    void addBooking() {
        when(itemRepository.existsById(anyLong())).thenReturn(true);
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(userRepository.existsById(anyLong())).thenReturn(true);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(secondUser));
        when(bookingRepository.save(any(Booking.class))).thenReturn(firstBooking);

        BookingOutDto bookingOutDtoTest = bookingService.addBooking(bookingDto, anyLong());

        assertEquals(itemDto, bookingOutDtoTest.getItem());
        assertEquals(firstBooking.getStatus(), bookingOutDtoTest.getStatus());
        assertEquals(UserMapper.toUserDto(secondUser), bookingOutDtoTest.getBooker());

        verify(bookingRepository, times(1)).save(any(Booking.class));
    }

    @Test
    void addBookingWrongOwner() {
        when(itemRepository.existsById(anyLong())).thenReturn(true);
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(userRepository.existsById(anyLong())).thenReturn(true);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(firstUser));

        assertThrows(NotFoundException.class, () -> bookingService.addBooking(bookingDto, anyLong()));
    }

    @Test
    void addBookingItemBooked() {
        item.setAvailable(false);

        when(itemRepository.existsById(anyLong())).thenReturn(true);
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(userRepository.existsById(anyLong())).thenReturn(true);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(secondUser));

        assertThrows(ValidationException.class, () -> bookingService.addBooking(bookingDto, anyLong()));
    }

    @Test
    void addBookingNotValidEnd() {
        when(itemRepository.existsById(anyLong())).thenReturn(true);
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(userRepository.existsById(anyLong())).thenReturn(true);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(secondUser));

        bookingDto.setEnd(LocalDateTime.of(2022, 10, 12, 0, 0));

        assertThrows(ValidationException.class, () -> bookingService.addBooking(bookingDto, anyLong()));
    }

    @Test
    void addBookingNotValidStart() {
        when(itemRepository.existsById(anyLong())).thenReturn(true);
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(userRepository.existsById(anyLong())).thenReturn(true);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(secondUser));

        bookingDto.setStart(LocalDateTime.of(2023, 10, 12, 0, 0));

        assertThrows(ValidationException.class, () -> bookingService.addBooking(bookingDto, anyLong()));
    }

    @Test
    void approveBooking() {
        BookingOutDto bookingOutDtoTest;

        when(bookingRepository.existsById(anyLong())).thenReturn(true);
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(secondBooking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(secondBooking);

        bookingOutDtoTest = bookingService.approveBooking(firstUser.getId(), item.getId(), true);
        assertEquals(Status.APPROVED, bookingOutDtoTest.getStatus());

        bookingOutDtoTest = bookingService.approveBooking(firstUser.getId(), item.getId(), false);
        assertEquals(Status.REJECTED, bookingOutDtoTest.getStatus());

        verify(bookingRepository, times(2)).save(any(Booking.class));
    }

    @Test
    void approveBookingWrongUser() {
        when(bookingRepository.existsById(anyLong())).thenReturn(true);
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(secondBooking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(secondBooking);

        assertThrows(NotFoundException.class, () -> bookingService.approveBooking(secondUser.getId(), item.getId(), true));
    }

    @Test
    void approveBookingWrongStatus() {
        when(bookingRepository.existsById(anyLong())).thenReturn(true);
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(firstBooking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(firstBooking);

        assertThrows(ValidationException.class, () -> bookingService.approveBooking(firstUser.getId(), item.getId(), true));
    }

    @Test
    void getBookingById() {
        when(bookingRepository.existsById(anyLong())).thenReturn(true);
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(firstBooking));
        when(userRepository.existsById(anyLong())).thenReturn(true);

        BookingOutDto bookingOutDtoTest = bookingService.getBookingById(firstUser.getId(), firstBooking.getId());

        assertEquals(itemDto, bookingOutDtoTest.getItem());
        assertEquals(firstBooking.getStatus(), bookingOutDtoTest.getStatus());
        assertEquals(UserMapper.toUserDto(firstUser), bookingOutDtoTest.getBooker());
    }

    @Test
    void getBookingByIdError() {
        when(bookingRepository.existsById(anyLong())).thenReturn(true);
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(firstBooking));
        when(userRepository.existsById(anyLong())).thenReturn(true);

        assertThrows(NotFoundException.class, () -> bookingService.getBookingById(2L, firstBooking.getId()));
    }

    @Test
    void getAllBookingsByBookerId() {
        when(userRepository.existsById(anyLong())).thenReturn(true);
        when(unionService.checkPageSize(anyInt(), anyInt())).thenReturn(PageRequest.of(5 / 10, 10));
        when(bookingRepository.getByBookerIdOrderByStartDesc(anyLong(), any(PageRequest.class))).thenReturn(new PageImpl<>(List.of(firstBooking)));

        String state = "ALL";

        List<BookingOutDto> bookingOutDtoTest = bookingService.getAllBookingsByBookerId(firstUser.getId(), state, 5, 10);

        assertEquals(firstBooking.getId(), bookingOutDtoTest.get(0).getId());
        assertEquals(firstBooking.getStatus(), bookingOutDtoTest.get(0).getStatus());
        assertEquals(UserMapper.toUserDto(firstUser), bookingOutDtoTest.get(0).getBooker());

        when(bookingRepository.getByBookerIdAndStartBeforeAndEndAfterOrderByStartAsc(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class), any(PageRequest.class))).thenReturn(new PageImpl<>(List.of(firstBooking)));
        state = "CURRENT";

        bookingOutDtoTest = bookingService.getAllBookingsByBookerId(firstUser.getId(), state, 5, 10);

        assertEquals(firstBooking.getId(), bookingOutDtoTest.get(0).getId());
        assertEquals(firstBooking.getStatus(), bookingOutDtoTest.get(0).getStatus());
        assertEquals(UserMapper.toUserDto(firstUser), bookingOutDtoTest.get(0).getBooker());

        when(bookingRepository.getByBookerIdAndEndBeforeOrderByStartDesc(anyLong(), any(LocalDateTime.class), any(PageRequest.class))).thenReturn(new PageImpl<>(List.of(firstBooking)));
        state = "PAST";

        bookingOutDtoTest = bookingService.getAllBookingsByBookerId(firstUser.getId(), state, 5, 10);

        assertEquals(firstBooking.getId(), bookingOutDtoTest.get(0).getId());
        assertEquals(firstBooking.getStatus(), bookingOutDtoTest.get(0).getStatus());
        assertEquals(UserMapper.toUserDto(firstUser), bookingOutDtoTest.get(0).getBooker());

        when(bookingRepository.getByBookerIdAndStartAfterOrderByStartDesc(anyLong(), any(LocalDateTime.class), any(PageRequest.class))).thenReturn(new PageImpl<>(List.of(firstBooking)));
        state = "FUTURE";

        bookingOutDtoTest = bookingService.getAllBookingsByBookerId(firstUser.getId(), state, 5, 10);

        assertEquals(firstBooking.getId(), bookingOutDtoTest.get(0).getId());
        assertEquals(firstBooking.getStatus(), bookingOutDtoTest.get(0).getStatus());
        assertEquals(UserMapper.toUserDto(firstUser), bookingOutDtoTest.get(0).getBooker());

        when(bookingRepository.getByBookerIdAndStatusOrderByStartDesc(anyLong(), any(Status.class), any(PageRequest.class))).thenReturn(new PageImpl<>(List.of(firstBooking)));
        state = "WAITING";

        bookingOutDtoTest = bookingService.getAllBookingsByBookerId(firstUser.getId(), state, 5, 10);

        assertEquals(firstBooking.getId(), bookingOutDtoTest.get(0).getId());
        assertEquals(firstBooking.getStatus(), bookingOutDtoTest.get(0).getStatus());
        assertEquals(UserMapper.toUserDto(firstUser), bookingOutDtoTest.get(0).getBooker());

        when(bookingRepository.getByBookerIdAndStatusOrderByStartDesc(anyLong(), any(Status.class), any(PageRequest.class))).thenReturn(new PageImpl<>(List.of(firstBooking)));
        state = "REJECTED";

        bookingOutDtoTest = bookingService.getAllBookingsByBookerId(firstUser.getId(), state, 5, 10);

        assertEquals(firstBooking.getId(), bookingOutDtoTest.get(0).getId());
        assertEquals(firstBooking.getStatus(), bookingOutDtoTest.get(0).getStatus());
        assertEquals(UserMapper.toUserDto(firstUser), bookingOutDtoTest.get(0).getBooker());
    }

    @Test
    void getAllBookingsForAllItemsByOwnerId() {
        when(userRepository.existsById(anyLong())).thenReturn(true);
        when(itemRepository.findByOwnerId(anyLong())).thenReturn(List.of(item));
        when(unionService.checkPageSize(anyInt(), anyInt())).thenReturn(PageRequest.of(5 / 10, 10));
        when(bookingRepository.getByItemOwnerIdOrderByStartDesc(anyLong(), any(PageRequest.class))).thenReturn(new PageImpl<>(List.of(firstBooking)));

        String state = "ALL";

        List<BookingOutDto> bookingOutDtoTest = bookingService.getAllBookingsForAllItemsByOwnerId(firstUser.getId(), state, 5, 10);

        assertEquals(firstBooking.getId(), bookingOutDtoTest.get(0).getId());
        assertEquals(firstBooking.getStatus(), bookingOutDtoTest.get(0).getStatus());
        assertEquals(UserMapper.toUserDto(firstUser), bookingOutDtoTest.get(0).getBooker());

        when(bookingRepository.getByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartAsc(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class), any(PageRequest.class))).thenReturn(new PageImpl<>(List.of(firstBooking)));
        state = "CURRENT";

        bookingOutDtoTest = bookingService.getAllBookingsForAllItemsByOwnerId(firstUser.getId(), state, 5, 10);

        assertEquals(firstBooking.getId(), bookingOutDtoTest.get(0).getId());
        assertEquals(firstBooking.getStatus(), bookingOutDtoTest.get(0).getStatus());
        assertEquals(UserMapper.toUserDto(firstUser), bookingOutDtoTest.get(0).getBooker());

        when(bookingRepository.getByItemOwnerIdAndEndBeforeOrderByStartDesc(anyLong(), any(LocalDateTime.class), any(PageRequest.class))).thenReturn(new PageImpl<>(List.of(firstBooking)));
        state = "PAST";

        bookingOutDtoTest = bookingService.getAllBookingsForAllItemsByOwnerId(firstUser.getId(), state, 5, 10);

        assertEquals(firstBooking.getId(), bookingOutDtoTest.get(0).getId());
        assertEquals(firstBooking.getStatus(), bookingOutDtoTest.get(0).getStatus());
        assertEquals(UserMapper.toUserDto(firstUser), bookingOutDtoTest.get(0).getBooker());

        when(bookingRepository.getByItemOwnerIdAndStartAfterOrderByStartDesc(anyLong(), any(LocalDateTime.class), any(PageRequest.class))).thenReturn(new PageImpl<>(List.of(firstBooking)));
        state = "FUTURE";

        bookingOutDtoTest = bookingService.getAllBookingsForAllItemsByOwnerId(firstUser.getId(), state, 5, 10);

        assertEquals(firstBooking.getId(), bookingOutDtoTest.get(0).getId());
        assertEquals(firstBooking.getStatus(), bookingOutDtoTest.get(0).getStatus());
        assertEquals(UserMapper.toUserDto(firstUser), bookingOutDtoTest.get(0).getBooker());

        when(bookingRepository.getByItemOwnerIdAndStatusOrderByStartDesc(anyLong(), any(Status.class), any(PageRequest.class))).thenReturn(new PageImpl<>(List.of(firstBooking)));
        state = "WAITING";

        bookingOutDtoTest = bookingService.getAllBookingsForAllItemsByOwnerId(firstUser.getId(), state, 5, 10);

        assertEquals(firstBooking.getId(), bookingOutDtoTest.get(0).getId());
        assertEquals(firstBooking.getStatus(), bookingOutDtoTest.get(0).getStatus());
        assertEquals(UserMapper.toUserDto(firstUser), bookingOutDtoTest.get(0).getBooker());

        when(bookingRepository.getByItemOwnerIdAndStatusOrderByStartDesc(anyLong(), any(Status.class), any(PageRequest.class))).thenReturn(new PageImpl<>(List.of(firstBooking)));
        state = "REJECTED";

        bookingOutDtoTest = bookingService.getAllBookingsForAllItemsByOwnerId(firstUser.getId(), state, 5, 10);

        assertEquals(firstBooking.getId(), bookingOutDtoTest.get(0).getId());
        assertEquals(firstBooking.getStatus(), bookingOutDtoTest.get(0).getStatus());
        assertEquals(UserMapper.toUserDto(firstUser), bookingOutDtoTest.get(0).getBooker());
    }

    @Test
    void getAllBookingsForAllItemsByOwnerIdNotHaveItems() {
        when(userRepository.existsById(anyLong())).thenReturn(true);
        when(itemRepository.findByOwnerId(anyLong())).thenReturn(List.of());

        assertThrows(ValidationException.class, () -> bookingService.getAllBookingsForAllItemsByOwnerId(firstUser.getId(), "APPROVED", 5, 10));
    }
}