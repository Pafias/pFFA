package me.pafias.pffa.objects.exceptions;

/**
 * Exception thrown when there is an error loading user data.
 * This can occur if the user data cannot be retrieved from the storage system.
 */
public class UserLoadingException extends RuntimeException {

    public UserLoadingException() {
        super("An error occurred while loading user data.");
    }

    public UserLoadingException(String message) {
        super(message);
    }

    public UserLoadingException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserLoadingException(Throwable cause) {
        super(cause);
    }

}
