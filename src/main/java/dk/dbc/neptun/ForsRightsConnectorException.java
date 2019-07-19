/*
 * Copyright Dansk Bibliotekscenter a/s. Licensed under GPLv3
 * See license text in LICENSE.txt or at https://opensource.dbc.dk/licenses/gpl-3.0/
 */

package dk.dbc.neptun;

public class ForsRightsConnectorException extends Exception {
    public ForsRightsConnectorException(String msg) {
        super(msg);
    }

    public ForsRightsConnectorException(Throwable cause) {
        super(cause);
    }
}
