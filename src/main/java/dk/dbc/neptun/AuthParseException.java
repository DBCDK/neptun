package dk.dbc.neptun;

public class AuthParseException extends Exception {
    public AuthParseException(String msg) {
        super(msg);
    }
    public AuthParseException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
