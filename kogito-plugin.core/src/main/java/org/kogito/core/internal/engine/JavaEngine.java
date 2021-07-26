package org.kogito.core.internal.engine;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.template.PebbleTemplate;
import org.kogito.core.internal.engine.exceptions.EngineException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JavaEngine {

    private final PebbleEngine engine;
    private final Logger logger = LoggerFactory.getLogger(JavaEngine.class);

    public JavaEngine() {
        this.engine = new PebbleEngine.Builder().build();
    }

    protected String evaluate(String template, Map<String, Object> context) {
        try {
            Writer writer = new StringWriter();
            PebbleTemplate compiledTemplate = this.engine.getTemplate(template);
            compiledTemplate.evaluate(writer, context);
            return writer.toString();
        } catch (Exception e) {
            String message = "Can't evaluate template " + template;
            logger.error(message, e);
            throw new EngineException(message, e);
        }
    }

    public BuildInformation buildImportClass(String uri, String importText) {
        Map<String, Object> context = new HashMap<>();
        context.put("className", getClassName(uri));
        context.put("completeText", importText);

        String content = this.evaluate(Templates.TEMPLATE_CLASS, context);

        return new BuildInformation(uri, getContent(uri), content, 3, getEndOfLinePosition(content, 3));
    }

    public BuildInformation buildPublicContent(String uri, String fqdn, String completeText) {

        Map<String, Object> context = new HashMap<>();
        context.put("className", getClassName(uri));
        context.put("fqdn", fqdn);
        context.put("completeText", completeText);

        String content = this.evaluate(Templates.TEMPLATE_ACCESSORS, context);

        return new BuildInformation(uri, getContent(uri), content, 8, getEndOfLinePosition(content, 8));
    }

    protected int getEndOfLinePosition(String content, int lineNumber) {
        String[] split = content.split("\n");
        logger.info(split[lineNumber]);
        return split[lineNumber].length();
    }

    protected String getClassName(String url) {
        String fileName = Paths.get(url).getFileName().toString();
        if (fileName.contains(".")) {
            return fileName.substring(0, fileName.lastIndexOf("."));
        } else {
            return fileName;
        }
    }

    protected String getContent(String uri) {
        try {
            return Files.readString(Path.of(URI.create(uri)));
        } catch (IOException e) {
            logger.error("Can't read content from: " + uri, e);
            return "";
        }
    }
}
