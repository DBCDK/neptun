/*
 * Copyright Dansk Bibliotekscenter a/s. Licensed under GPLv3
 * See license text in LICENSE.txt or at https://opensource.dbc.dk/licenses/gpl-3.0/
 */

package dk.dbc.neptun;

public class AuthParseException extends Exception {
    public AuthParseException(String msg) {
        super(msg);
    }

    public AuthParseException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
