/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.kogito.core.internal.handlers;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.ls.core.internal.JavaLanguageServerPlugin;
import org.eclipse.lsp4j.CompletionItem;
import org.kogito.core.internal.api.GetPublicParameters;
import org.kogito.core.internal.api.GetPublicResult;
import org.kogito.core.internal.engine.BuildInformation;
import org.kogito.core.internal.engine.JavaEngine;

public class GetAccessorsHandler extends Handler<List<GetPublicResult>> {

    private final JavaEngine javaEngine;
    private final AutocompleteHandler autocompleteHandler;

    public GetAccessorsHandler(String id, JavaEngine javaEngine, AutocompleteHandler autocompleteHandler) {
        super(id);
        this.javaEngine = javaEngine;
        this.autocompleteHandler = autocompleteHandler;
    }

    @Override
    public CompletableFuture<List<GetPublicResult>> handle(List<Object> arguments, IProgressMonitor progress) {
        GetPublicParameters parameters = checkParameters(arguments);
        BuildInformation buildInformation = javaEngine.buildPublicContent(this.autocompleteHandler.getUri(),
                                                                          parameters.getFqcn(),
                                                                          parameters.getQuery());
        JavaLanguageServerPlugin.logInfo(buildInformation.getText());
        List<CompletionItem> items = this.autocompleteHandler.handle(arguments, buildInformation);
        List<GetPublicResult> completedClasses = this.transformCompletionItemsToResult(parameters.getFqcn(), items);
        return CompletableFuture.supplyAsync(() -> completedClasses);
    }

    private GetPublicParameters checkParameters(List<Object> arguments) {
        if (arguments.size() < 2) {
            throw new IllegalArgumentException("Not enough arguments for GetClasses command. Need one argument containing a text to be autocompleted");
        } else {
            JavaLanguageServerPlugin.logError("Arguments: " + arguments.get(0));
        }

        GetPublicParameters parameters = new GetPublicParameters();
        parameters.setFqcn((String) arguments.get(0));
        parameters.setQuery((String) arguments.get(1));
        return parameters;
    }

    private List<GetPublicResult> transformCompletionItemsToResult(String fqcn, List<CompletionItem> items) {
        return items.stream()
                .map(item -> {
                    JavaLanguageServerPlugin.logInfo(item.getLabel());
                    GetPublicResult result = new GetPublicResult();
                    result.setFqcn(fqcn);
                    result.setResult(item.getLabel());
                    return result;
                })
                .collect(Collectors.toList());
    }
}
