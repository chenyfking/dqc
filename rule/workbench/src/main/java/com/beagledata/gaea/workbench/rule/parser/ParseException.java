package com.beagledata.gaea.workbench.rule.parser;

/**
 * Created by liulu on 2018/10/10.
 */
public class ParseException extends RuntimeException {
    private static final long serialVersionUID = 5581470577890910705L;

    public ParseException() {
    }

    public ParseException(String message) {
        super(message);
    }

    public ParseException(String message, Throwable cause) {
        super(message, cause);
    }

    public ParseException(Throwable cause) {
        super(cause);
    }
}
