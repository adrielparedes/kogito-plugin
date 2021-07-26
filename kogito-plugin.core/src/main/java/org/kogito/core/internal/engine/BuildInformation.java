package org.kogito.core.internal.engine;

public class BuildInformation {

    private final String uri;
    private final String originalText;
    private final String text;
    private final int line;
    private final int position;

    public BuildInformation(String uri, String originalText, String text, int line, int position) {
        this.uri = uri;
        this.originalText = originalText;
        this.text = text;
        this.line = line;
        this.position = position;
    }

    public String getText() {
        return text;
    }

    public int getLine() {
        return line;
    }

    public int getPosition() {
        return position;
    }

    public String getUri() {
        return uri;
    }

    public String getOriginalText() {
        return originalText;
    }
}
