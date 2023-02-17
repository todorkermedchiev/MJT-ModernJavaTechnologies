package bg.sofia.uni.fmi.mjt.markdown;

import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class MarkdownConverter implements MarkdownConverterAPI {

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

    private static final String DUMMY_CHARACTER = "h";
    private static final String LINE_SEPARATOR = System.lineSeparator();
    private static final String HTML_BODY_OPENING_TAGS = "<html>" + LINE_SEPARATOR + "<body>" + LINE_SEPARATOR;
    private static final String HTML_BODY_CLOSING_TAGS = "</body>" + LINE_SEPARATOR + "</html>" + LINE_SEPARATOR;
    private static final String MARKDOWN_EXTENSION = ".md";
    private static final String HTML_EXTENSION = ".html";

    public MarkdownConverter() {
    }


    @Override
    public void convertMarkdown(Reader source, Writer output) {
        validateObject(source);
        validateObject(output);

        try (var bufferedSource = new BufferedReader(source)) {
            output.write(HTML_BODY_OPENING_TAGS);
            output.flush();

            String inputLine;

            while ((inputLine = bufferedSource.readLine()) != null) {
                String outputLine = TagsPlacer.replaceMarkdownSymbols(inputLine, SYMBOL_REGEX_MAP);

                output.write(outputLine + LINE_SEPARATOR);
                output.flush();
            }

            output.write(HTML_BODY_CLOSING_TAGS);
            output.flush();
        } catch (IOException e) {
            throw new IllegalStateException("A problem occurred while reading from a file", e);
        }
    }

    @Override
    public void convertMarkdown(Path from, Path to) {
        validateObject(from);
        validateObject(to);

        try (var bufferedReader = Files.newBufferedReader(from);
             var bufferedWriter = Files.newBufferedWriter(to)) {
            convertMarkdown(bufferedReader, bufferedWriter);
        } catch (IOException e) {
            throw new IllegalStateException("A problem occurred while reading from a file", e);
        }
    }

    @Override
    public void convertAllMarkdownFiles(Path sourceDir, Path targetDir) {
        validateObject(sourceDir);
        validateObject(targetDir);

        try {
            Files.createDirectories(targetDir);
        } catch (FileAlreadyExistsException e) {
            throw new IllegalStateException("Cannot create directory - file with this name already exist", e);
        } catch (IOException e) {
            throw new IllegalStateException("A problem occurred while creating the target directory", e);
        }

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(sourceDir)) {
            for (Path currentFile : stream) {
                if (currentFile.toString().endsWith(MARKDOWN_EXTENSION)) {
                    String newFileName = changeExtension(currentFile.getFileName().toString(), HTML_EXTENSION);
                    convertMarkdown(currentFile, targetDir.resolve(newFileName));
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("A problem occurred", e);
        }
    }

    static String changeExtension(String fileName, String newExtension) {
        validateString(fileName);
        validateString(newExtension);

        String[] arr = fileName.split("\\.");
        return arr[0] + newExtension;
    }

    private static void validateString(String string) {
        if (string == null || string.isEmpty() || string.isBlank()) {
            throw new IllegalArgumentException("Invalid string");
        }
    }

    private static void validateObject(Object obj) {
        if (obj == null) {
            throw new IllegalArgumentException("Object can not be null");
        }
    }
}


