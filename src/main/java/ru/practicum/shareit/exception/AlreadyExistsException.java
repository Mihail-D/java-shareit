package ru.practicum.shareit.exception;

public class AlreadyExistsException extends RuntimeException {

    public AlreadyExistsException(String message) {
        super(message);
    }

    public AlreadyExistsException(String clsName, Long id) {
        super(String.format("%s with ID %d already exists", clsName, id));
    }

    public AlreadyExistsException(String clsName, String textIdentifier) {
        super(String.format("%s with ID '%s' already exists", clsName, textIdentifier));
    }
}
