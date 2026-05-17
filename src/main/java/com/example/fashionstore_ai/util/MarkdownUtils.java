package com.example.fashionstore_ai.util;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

public class MarkdownUtils {

    private static final Parser PARSER = Parser.builder().build();
    private static final HtmlRenderer RENDERER = HtmlRenderer.builder().build();

    private MarkdownUtils() {}

    public static String render(String text) {
        if (text == null || text.isBlank()) return "";
        Node document = PARSER.parse(text);
        return RENDERER.render(document);
    }
}
