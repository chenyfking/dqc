package com.beagledata.gaea.ruleengine.exception;

import org.kie.api.builder.Results;

/**
 * Created by liulu on 2019/1/4.
 */
public class RuleException extends RuntimeException {
    private static final long serialVersionUID = 4907281431406054247L;

    private Results results;

    public RuleException() {
    }

    public RuleException(String message) {
        super(message);
    }
    public RuleException(String message, Results results) {
        super(message);
        this.results = results;
    }

    public RuleException(String message, Throwable cause) {
        super(message, cause);
    }

    public RuleException(Throwable cause) {
        super(cause);
    }

    public Results getResults() {
        return results;
    }

    public void setResults(Results results) {
        this.results = results;
    }
}
