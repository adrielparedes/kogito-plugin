package org.kogito.core.internal;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.ls.core.internal.IDelegateCommandHandler;
import org.kogito.core.internal.engine.JavaEngine;
import org.kogito.core.internal.handlers.GetClassesHandler;
import org.kogito.core.internal.handlers.Handler;
import org.kogito.core.internal.handlers.HandlerConstants;

public class DelegateHandler implements IDelegateCommandHandler {

    private static final JavaEngine JAVA_ENGINE = new JavaEngine();

    private static final List<Handler> handlers = new ArrayList<>() {{
        add(new GetClassesHandler(HandlerConstants.GET_CLASSES, JAVA_ENGINE));
    }};

    public Object executeCommand(String commandId, List<Object> arguments, IProgressMonitor progress) throws Exception {

        return handlers.stream().filter(handler -> handler.canHandle(commandId))
                .findFirst()
                .map(handler -> handler.handle(arguments, progress))
                .orElseThrow(() -> new UnsupportedOperationException(String.format("Unsupported command '%s'!", commandId)));
    }
}
