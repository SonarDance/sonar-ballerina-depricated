package org.wso2.ballerina.api;

import org.sonar.api.batch.fs.TextPointer;

public class ParseException extends RuntimeException {
    private final TextPointer position;

    public ParseException(String message, TextPointer position, Throwable cause) {
        super(message, cause);
        this.position = position;
    }

    public ParseException(String message, TextPointer position) {
        this(message, position, null);
    }

    public ParseException(String message, Throwable cause) {
        this(message, null, cause);
    }

    public ParseException(String message) {
        this(message, null, null);
    }

    public TextPointer getPosition() {
        return position;
    }
}