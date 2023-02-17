package bg.sofia.uni.fmi.mjt.markdown;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TagPlacerTest {

    private static final Map<MDSymbolsToHTMLTags, MarkdownSymbolRegex> SYMBOL_REGEX_MAP;

    static {
        SYMBOL_REGEX_MAP = new HashMap<>();

        SYMBOL_REGEX_MAP.put(MDSymbolsToHTMLTags.HEADING1, MarkdownSymbolRegex.HEADING1);
        SYMBOL_REGEX_MAP.put(MDSymbolsToHTMLTags.HEADING2, MarkdownSymbolRegex.HEADING2);
        SYMBOL_REGEX_MAP.put(MDSymbolsToHTMLTags.HEADING3, MarkdownSymbolRegex.HEADING3);
        SYMBOL_REGEX_MAP.put(MDSymbolsToHTMLTags.HEADING4, MarkdownSymbolRegex.HEADING4);
        SYMBOL_REGEX_MAP.put(MDSymbolsToHTMLTags.HEADING5, MarkdownSymbolRegex.HEADING5);
        SYMBOL_REGEX_MAP.put(MDSymbolsToHTMLTags.HEADING6, MarkdownSymbolRegex.HEADING6);

        SYMBOL_REGEX_MAP.put(MDSymbolsToHTMLTags.ITALIC, MarkdownSymbolRegex.ITALIC);
        SYMBOL_REGEX_MAP.put(MDSymbolsToHTMLTags.BOLD, MarkdownSymbolRegex.BOLD);
        SYMBOL_REGEX_MAP.put(MDSymbolsToHTMLTags.CODE, MarkdownSymbolRegex.CODE);
    }

    @Test
    void testReplaceMarkdownSymbolsHeading1() {
        String source = new String("# Some heading1");
        String actual = TagsPlacer.replaceMarkdownSymbols(source, SYMBOL_REGEX_MAP);

        assertEquals("<h1>Some heading1</h1>", actual,
                "Markdown symbols are not properly replaced with HTML tags");
    }

    @Test
    void testReplaceMarkdownSymbolsHeading2() {
        String source = new String("## Some heading2");
        String actual = TagsPlacer.replaceMarkdownSymbols(source, SYMBOL_REGEX_MAP);

        assertEquals("<h2>Some heading2</h2>", actual,
                "Markdown symbols are not properly replaced with HTML tags");
    }

    @Test
    void testReplaceMarkdownSymbolsBold() {
        String source = new String("Some **bold** text");
        String actual = TagsPlacer.replaceMarkdownSymbols(source, SYMBOL_REGEX_MAP);

        assertEquals("Some <strong>bold</strong> text", actual,
                "Markdown symbols are not properly replaced with HTML tags");
    }

    @Test
    void testReplaceMarkdownSymbolsItalic() {
        String source = new String("Some *italic* text");
        String actual = TagsPlacer.replaceMarkdownSymbols(source, SYMBOL_REGEX_MAP);

        assertEquals("Some <em>italic</em> text", actual,
                "Markdown symbols are not properly replaced with HTML tags");
    }

    @Test
    void testReplaceMarkdownSymbolsBoldAndItalic() {
        String source = new String("Some **bold** and *italic* text");
        String actual = TagsPlacer.replaceMarkdownSymbols(source, SYMBOL_REGEX_MAP);

        assertEquals("Some <strong>bold</strong> and <em>italic</em> text", actual,
                "Markdown symbols are not properly replaced with HTML tags");
    }

    @Test
    void testReplaceMarkdownSymbolsCode() {
        String source = new String("A `code snippet`");
        String actual = TagsPlacer.replaceMarkdownSymbols(source, SYMBOL_REGEX_MAP);

        assertEquals("A <code>code snippet</code>", actual,
                "Markdown symbols are not properly replaced with HTML tags");
    }

    @Test
    void testReplaceMarkdownSymbolsInvalidArguments() {

        assertThrows(IllegalArgumentException.class, () -> TagsPlacer.replaceMarkdownSymbols(null,
                        SYMBOL_REGEX_MAP),
                "Source string cannot be null");

        assertThrows(IllegalArgumentException.class, () -> TagsPlacer.replaceMarkdownSymbols("src",
                        null),
                "Tags cannot be blank");

    }

}
