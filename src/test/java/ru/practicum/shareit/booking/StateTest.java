package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.booking.enums.State;
import ru.practicum.shareit.exception.UnsupportedStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class StateTest {

    @Test
    void getEnumValue() {

        String stateStr = "Unknown";
        String finalStateStr = stateStr;
        assertThrows(UnsupportedStatusException.class, () -> State.getEnumValue(finalStateStr));

        stateStr = "ALL";
        State actualState = State.getEnumValue(stateStr);
        assertEquals(actualState, State.ALL);

        stateStr = "CURRENT";
        actualState = State.getEnumValue(stateStr);
        assertEquals(actualState, State.CURRENT);

        stateStr = "PAST";
        actualState = State.getEnumValue(stateStr);
        assertEquals(actualState, State.PAST);

        stateStr = "FUTURE";
        actualState = State.getEnumValue(stateStr);
        assertEquals(actualState, State.FUTURE);

        stateStr = "REJECTED";
        actualState = State.getEnumValue(stateStr);
        assertEquals(actualState, State.REJECTED);

        stateStr = "WAITING";
        actualState = State.getEnumValue(stateStr);
        assertEquals(actualState, State.WAITING);
    }
}