package org.kogito.core.internal.engine.exceptions;

public class EngineException extends RuntimeException {

    public EngineException(String message, Exception e) {
        super(message, e);
    }
}
