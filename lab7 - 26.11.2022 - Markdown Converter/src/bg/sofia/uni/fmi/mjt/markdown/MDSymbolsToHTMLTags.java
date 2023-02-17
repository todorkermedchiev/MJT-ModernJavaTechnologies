package bg.sofia.uni.fmi.mjt.markdown;

public enum MDSymbolsToHTMLTags {
    HEADING1("h# ", "<h1>", "</h1>"),
    HEADING2("h## ", "<h2>", "</h2>"),
    HEADING3("h### ", "<h3>", "</h3>"),
    HEADING4("h#### ", "<h4>", "</h4>"),
    HEADING5("h##### ", "<h5>", "</h5>"),
    HEADING6("h###### ", "<h6>", "</h6>"),
    BOLD("**", "<strong>", "</strong>"),
    ITALIC("*", "<em>", "</em>"),
    CODE("`", "<code>", "</code>");

    public final String markdownSymbol;
    public final String openingTag;
    public final String closingTag;

    MDSymbolsToHTMLTags(String markdownSymbol, String openingTag, String closingTag) {
        this.markdownSymbol = markdownSymbol;
        this.openingTag = openingTag;
        this.closingTag = closingTag;
    }
}


//public record HTMLTags(String openingTag, String closingTag) {
//
//    public HTMLTags {
//        if (openingTag == null || openingTag.isBlank()) {
//            throw new IllegalArgumentException("Opening tag cannot be null or blank");
//        }
//        if (closingTag == null || closingTag.isBlank()) {
//            throw new IllegalArgumentException("Closing tag cannot be null or blank");
//        }
//    }
//}