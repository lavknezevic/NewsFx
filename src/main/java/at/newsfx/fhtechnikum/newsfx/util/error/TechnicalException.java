package at.newsfx.fhtechnikum.newsfx.util.error;

public class TechnicalException extends RuntimeException {

    public TechnicalException(String message, Throwable cause) {
        super(message, cause);
    }
}