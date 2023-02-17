package bg.sofia.uni.fmi.mjt.markdown;

import java.util.Map;
import java.util.Set;

public class TagsPlacer {
    private static final String DUMMY_CHARACTER = "h";

    public static String replaceMarkdownSymbols(String source, Map<MDSymbolsToHTMLTags, MarkdownSymbolRegex> tagSet) {
        if (source == null) {
            throw new IllegalArgumentException("Source string cannot be null");
        }
        if (tagSet == null) {
            throw new IllegalArgumentException("The tag set cannot be null");
        }

        Set<MDSymbolsToHTMLTags> symbols = tagSet.keySet();
        String result = new String(source);

        // Needed in order to determine different levels heading - bad design
        if (source.contains("#")) {
            result = new String(DUMMY_CHARACTER);
        } else {
            result = new String("");
        }

        result = result.concat(source);

        for (MDSymbolsToHTMLTags currentSymbol : symbols) {
            if (result.contains(currentSymbol.markdownSymbol)) {
                // Finding bold text - bad design
                if (currentSymbol == MDSymbolsToHTMLTags.ITALIC) {
                    if (result.contains(MDSymbolsToHTMLTags.BOLD.markdownSymbol)) {
                        result = result.replaceFirst(MarkdownSymbolRegex.BOLD.symbolRegex,
                                MDSymbolsToHTMLTags.BOLD.openingTag);
                    }
                    if (result.contains(MDSymbolsToHTMLTags.BOLD.markdownSymbol)) {
                        result = result.replaceFirst(MarkdownSymbolRegex.BOLD.symbolRegex,
                                MDSymbolsToHTMLTags.BOLD.closingTag);
                    }
                    if (result.contains(MDSymbolsToHTMLTags.ITALIC.markdownSymbol)) {
                        result = result.replaceFirst(MarkdownSymbolRegex.ITALIC.symbolRegex,
                                MDSymbolsToHTMLTags.ITALIC.openingTag);
                    }
                    if (result.contains(MDSymbolsToHTMLTags.ITALIC.markdownSymbol)) {
                        result = result.replaceFirst(MarkdownSymbolRegex.ITALIC.symbolRegex,
                                MDSymbolsToHTMLTags.ITALIC.closingTag);
                    }
                    continue;
                }

                result = result.replaceFirst(tagSet.get(currentSymbol).symbolRegex, currentSymbol.openingTag);

                if (result.contains(currentSymbol.markdownSymbol)) {
                    result = result.replaceFirst(tagSet.get(currentSymbol).symbolRegex, currentSymbol.closingTag);
                } else {
                    result = result.concat(currentSymbol.closingTag);
                }
            }
        }

        return result;
    }
}
