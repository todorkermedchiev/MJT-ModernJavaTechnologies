package bg.sofia.uni.fmi.mjt.markdown;

public enum MarkdownSymbolRegex {
    HEADING1("h# "),
    HEADING2("h## "),
    HEADING3("h### "),
    HEADING4("h#### "),
    HEADING5("h##### "),
    HEADING6("h###### "),
    BOLD("\\*\\*"),
    ITALIC("\\*"),
    CODE("`");

    public final String symbolRegex;

    MarkdownSymbolRegex(String symbolRegex) {
        this.symbolRegex = symbolRegex;
    }
}
