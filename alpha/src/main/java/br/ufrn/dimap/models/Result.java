package br.ufrn.dimap.models;

public class Result<T> {
    private final T value;
    private final String message;
    private final int statusCode;

    public Result(T value, String message) {
        this(value, message, value != null ? 200 : 400);
    }

    public Result(T value, String message, int statusCode) {
        this.value = value;
        this.message = message;
        this.statusCode = statusCode;
    }

    public static <T> Result<T> of(T value) {
        return new Result<>(value, null, 200);
    }

    public static <T> Result<T> of(T value, int statusCode) {
        return new Result<>(value, null, statusCode);
    }

    public static <T> Result<T> error(String message) {
        return new Result<>(null, message, 400);
    }

    public static <T> Result<T> error(String message, int statusCode) {
        return new Result<>(null, message, statusCode);
    }

    public T getValue() {
        return value;
    }

    public String getMessage() {
        return message;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public boolean isSuccess() {
        return value != null;
    }
}
