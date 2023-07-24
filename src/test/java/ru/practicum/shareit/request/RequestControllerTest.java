package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.controller.ItemRequestController;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.practicum.shareit.util.Constant.HEADER_USER;

@WebMvcTest(controllers = ItemRequestController.class)
public class RequestControllerTest {

    @MockBean
    private ItemRequestService itemRequestService;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private MockMvc mvc;

    private ItemRequestDto firstItemRequestDto;
    private ItemRequestDto secondItemRequestDto;

    @BeforeEach
    void setUp() {
        firstItemRequestDto = createItemRequestDto(1L, "ItemRequest 1");
        secondItemRequestDto = createItemRequestDto(2L, "ItemRequest 2");
    }

    private ItemRequestDto createItemRequestDto(Long id, String description) {
        return ItemRequestDto.builder()
                .id(id)
                .description(description)
                .created(LocalDateTime.now())
                .build();
    }

    @Test
    void addRequest() throws Exception {
        when(itemRequestService.addRequest(any(ItemRequestDto.class), anyLong())).thenReturn(firstItemRequestDto);

        mvc.perform(post("/requests")
                        .content(mapper.writeValueAsString(firstItemRequestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HEADER_USER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(firstItemRequestDto.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(firstItemRequestDto.getDescription()), String.class));

        verify(itemRequestService, times(1)).addRequest(firstItemRequestDto, 1L);
    }

    @Test
    void getRequests() throws Exception {
        when(itemRequestService.getRequests(anyLong())).thenReturn(List.of(firstItemRequestDto, secondItemRequestDto));

        mvc.perform(get("/requests")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HEADER_USER, 1L))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(List.of(firstItemRequestDto, secondItemRequestDto))));

        verify(itemRequestService).getRequests(1L);
    }

    @Test
    void getAllRequests() throws Exception {
        when(itemRequestService.getAllRequests(anyLong(), anyInt(), anyInt())).thenReturn(List.of(firstItemRequestDto, secondItemRequestDto));

        mvc.perform(get("/requests/all")
                        .param("from", String.valueOf(0))
                        .param("size", String.valueOf(10))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HEADER_USER, 1L))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(List.of(firstItemRequestDto, secondItemRequestDto))));

        verify(itemRequestService).getAllRequests(1L, 0, 10);
    }

    @Test
    void getRequestById() throws Exception {
        when(itemRequestService.getRequestById(anyLong(), anyLong())).thenReturn(firstItemRequestDto);

        mvc.perform(get("/requests/{requestId}", firstItemRequestDto.getId())
                        .content(mapper.writeValueAsString(firstItemRequestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HEADER_USER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(firstItemRequestDto.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(firstItemRequestDto.getDescription()), String.class));

        verify(itemRequestService).getRequestById(1L, 1L);
    }
}