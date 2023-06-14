package ru.practicum.shareit.exception;

public class OperationAccessException extends RuntimeException {

    public OperationAccessException(String message) {
        super(message);
    }

    public OperationAccessException(Long id) {
        super("Operation not available for user with ID " + id);
    }
}