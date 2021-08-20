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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.ls.core.internal.JavaLanguageServerPlugin;
import org.eclipse.jdt.ls.core.internal.handlers.JDTLanguageServer;
import org.eclipse.lsp4j.CompletionContext;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.CompletionParams;
import org.eclipse.lsp4j.CompletionTriggerKind;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.TextDocumentContentChangeEvent;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.kogito.core.internal.engine.BuildInformation;
import org.kogito.core.internal.engine.JavaEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetClassesHandler extends Handler {

    private final JavaEngine javaEngine;
    private final Logger logger = LoggerFactory.getLogger(GetClassesHandler.class);
    private final java.util.logging.Logger LOG = java.util.logging.Logger.getLogger(GetClassesHandler.class.getName());

    public GetClassesHandler(String id, JavaEngine javaEngine) {
        super(id);
        this.javaEngine = javaEngine;
    }

    public boolean canHandle() {
        return false;
    }

    public Object handle(List<Object> arguments, IProgressMonitor progress) {

        if (arguments.size() < 1) {
            throw new IllegalArgumentException("Not enough arguments for GetClasses command. Need one argument containing a text to be autocompleted");
        } else {
            logger.info("Arguments: {}", arguments.get(0));
        }

        JDTLanguageServer languageServer = (JDTLanguageServer) JavaLanguageServerPlugin.getInstance().getProtocol();

        String completeText = (String) arguments.get(0);

        IProject p = ResourcesPlugin.getWorkspace().getRoot().getProjects()[0];
        String workspace = p.getRawLocation().makeAbsolute().toOSString();

        String uri = "file://" + workspace + "/src/main/java/com/redhat/KogitoJDTLSPlugin.java";
        logger.info(uri);
        BuildInformation buildInformation = javaEngine.buildImportClass(uri, completeText);

        {
            DidOpenTextDocumentParams didOpenTextDocumentParams = new DidOpenTextDocumentParams();
            TextDocumentItem textDocumentItem = new TextDocumentItem();
            textDocumentItem.setLanguageId("java");
            textDocumentItem.setText(buildInformation.getOriginalText());
            textDocumentItem.setUri(buildInformation.getUri());
            textDocumentItem.setVersion(1);
            didOpenTextDocumentParams.setTextDocument(textDocumentItem);
            languageServer.didOpen(didOpenTextDocumentParams);
        }

        {
            DidChangeTextDocumentParams didChangeTextDocumentParams = new DidChangeTextDocumentParams();
            TextDocumentContentChangeEvent textDocumentContentChangeEvent = new TextDocumentContentChangeEvent();
            textDocumentContentChangeEvent.setText(buildInformation.getText());
            VersionedTextDocumentIdentifier versionedTextDocumentIdentifier = new VersionedTextDocumentIdentifier();
            versionedTextDocumentIdentifier.setUri(buildInformation.getUri());
            versionedTextDocumentIdentifier.setVersion(2);
            didChangeTextDocumentParams.setTextDocument(versionedTextDocumentIdentifier);
            didChangeTextDocumentParams.setContentChanges(Collections.singletonList(textDocumentContentChangeEvent));
            languageServer.didChange(didChangeTextDocumentParams);
        }

        CompletionContext context = new CompletionContext();
        context.setTriggerKind(CompletionTriggerKind.Invoked);

        Position pos = new Position();
        pos.setLine(buildInformation.getLine());
        pos.setCharacter(buildInformation.getPosition());

        TextDocumentIdentifier textDocumentIdentifier = new TextDocumentIdentifier();
        textDocumentIdentifier.setUri(uri);

        CompletionParams completionParams = new CompletionParams();
        completionParams.setTextDocument(textDocumentIdentifier);
        completionParams.setPosition(pos);
        completionParams.setContext(context);

        CompletableFuture<Either<List<CompletionItem>, CompletionList>> javaCompletion = languageServer
                .completion(completionParams);

        try {
            Either<List<CompletionItem>, CompletionList> completed = javaCompletion.get();
            List<CompletionItem> items = new ArrayList<>();
            if (completed.isLeft()) {
                items = completed.getLeft();
            } else if (completed.isRight()) {
                items = completed.getRight().getItems();
            }

            return items.stream()
                    .filter(item -> item.getLabel().contains("-"))
                    .map(item -> new HashMap<String, Object>() {{
                        put("fqdn", getFQCN(item.getLabel()));
                    }})
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error trying to get information from Java LSP", e);
            return CompletableFuture.supplyAsync(Collections::emptyList);
        }
    }

    public String getFQCN(String label) {
        String[] split = label.split("-");
        return split[1].trim() + "." + split[0].trim();
    }
}
