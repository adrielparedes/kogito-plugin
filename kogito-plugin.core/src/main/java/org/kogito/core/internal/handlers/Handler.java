package org.kogito.core.internal.handlers;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

public abstract class Handler {

    private final String id;

    public Handler(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    public boolean canHandle(String commandId) {
        return this.getId().equals(commandId);
    }

    public abstract Object handle(List<Object> arguments, IProgressMonitor progress);
}
