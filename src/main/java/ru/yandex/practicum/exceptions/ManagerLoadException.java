package ru.yandex.practicum.exceptions;

public class ManagerLoadException extends RuntimeException {
    public ManagerLoadException(String message, Throwable cause) {
        super(message, cause);
    }

    public ManagerLoadException(String message) {
        super(message);
    }
}
