package bg.sofia.uni.fmi.mjt.markdown;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.file.*;

import static org.junit.jupiter.api.Assertions.*;

public class MarkdownConverterTest {
    private MarkdownConverter converter;
    private static final String SAMPLE_STRING;
    private static final String EXPECTED_STRING;

    static {
        SAMPLE_STRING = "# Heading 1" + System.lineSeparator() +
                "## Heading 2" + System.lineSeparator() +
                "### Heading 3" + System.lineSeparator() +
                "#### Heading 4" + System.lineSeparator() +
                "##### Heading 5" + System.lineSeparator() +
                "###### Heading 6" + System.lineSeparator() +
                "Some **bold** text" + System.lineSeparator() +
                "Some *italic* text" + System.lineSeparator() +
                "A `code snippet`" + System.lineSeparator();

        EXPECTED_STRING = "<html>" + System.lineSeparator() +
                "<body>" + System.lineSeparator() +
                "<h1>Heading 1</h1>" + System.lineSeparator() +
                "<h2>Heading 2</h2>" + System.lineSeparator() +
                "<h3>Heading 3</h3>" + System.lineSeparator() +
                "<h4>Heading 4</h4>" + System.lineSeparator() +
                "<h5>Heading 5</h5>" + System.lineSeparator() +
                "<h6>Heading 6</h6>" + System.lineSeparator() +
                "Some <strong>bold</strong> text" + System.lineSeparator() +
                "Some <em>italic</em> text" + System.lineSeparator() +
                "A <code>code snippet</code>" + System.lineSeparator() +
                "</body>" + System.lineSeparator() +
                "</html>" + System.lineSeparator();
    }

    @BeforeEach
    void initializeMarkdownConverter() {
        converter = new MarkdownConverter();
    }

    @Test
    void testConvertMarkdownReaderWriterInvalidArguments() {
        try (StringReader input = new StringReader("Some String");
             StringWriter output = new StringWriter()) {
            assertThrows(IllegalArgumentException.class, () -> converter.convertMarkdown(input, null),
                    "Output cannot be null - expected IllegalArgumentException");

            assertThrows(IllegalArgumentException.class, () -> converter.convertMarkdown(null, output),
                    "Source cannot be null - expected IllegalArgumentException");
        } catch (IOException e) {
            throw new IllegalStateException("A problem occurred", e);
        }
    }

    @Test
    void testConvertMarkdownStringReaderStringWriter() {


        String actual;
        try (StringReader input = new StringReader(SAMPLE_STRING);
             StringWriter output = new StringWriter()) {
            converter.convertMarkdown(input, output);
            actual = output.toString();

            assertEquals(EXPECTED_STRING, actual, "Incorrect markdown to html conversion");
        } catch (IOException e) {
            throw new IllegalStateException("A problem occurred", e);
        }
    }

    @Test
    void testConvertMarkdownPathInvalidArguments() {
        Path from = Path.of("input.txt");
        Path to = Path.of("output.txt");
        try {
            Files.createFile(from);
            Files.createFile(to);
//            Path from = Files.createTempFile(Path.of(""), "input", ".txt");
//            Path to = Files.createTempFile(Path.of(""), "output", ".txt");

            assertThrows(IllegalArgumentException.class, () -> converter.convertMarkdown(null, to),
                    "The path \"from\" cannot be null");
            assertThrows(IllegalArgumentException.class, () -> converter.convertMarkdown(from, null),
                    "The path \"to\" cannot be null");
        } catch (IOException e) {
            throw new IllegalStateException("Cannot create temp file", e);
        } finally {
            try {
                Files.deleteIfExists(from);
                Files.deleteIfExists(to);
            } catch (IOException e) {
                throw new IllegalStateException("Cannot delete files", e);
            }
        }
    }

    @Test
    void testConvertMarkdownPathCorrectArguments() {
        Path from = Path.of("input.txt");
        Path to = Path.of("output.txt");
        try {
            Files.createFile(from);
            Files.createFile(to);

            Files.writeString(from, SAMPLE_STRING);

            converter.convertMarkdown(from, to);

            String actual = Files.readString(to);

            assertEquals(EXPECTED_STRING, actual, "Incorrect markdown to html conversion");
        } catch (IOException e) {
            throw new IllegalStateException("Cannot create temp file", e);
        } finally {
            try {
                Files.deleteIfExists(from);
                Files.deleteIfExists(to);
            } catch (IOException e) {
                throw new IllegalStateException("Cannot delete files", e);
            }
        }
    }

    @Test
    void testConvertAllMarkdownFilesCorrectPaths() {
        Path inputPath = Path.of("testInput");
        Path outputPath = Path.of("testOutput");

        try {
            Files.createDirectories(inputPath);
        } catch (FileAlreadyExistsException e) {
            throw new IllegalStateException("Cannot create test directory = file with this name already exist", e);
        } catch (IOException e) {
            throw new IllegalStateException("A problem occurred while creating the test directory", e);
        }

        Path test1 = Path.of("testInput" + File.separator + "test1.md");
        Path test2 = Path.of("testInput" + File.separator + "test2.md");
        Path test3 = Path.of("testInput" + File.separator + "test3.md");
        Path test4 = Path.of("testInput" + File.separator + "test4.txt");
        Path test5 = Path.of("testInput" + File.separator + "test5.txt");
        try {
            Files.createFile(test1);
            Files.createFile(test2);
            Files.createFile(test3);
            Files.createFile(test4);
            Files.createFile(test5);

            Files.writeString(test1, SAMPLE_STRING);
            Files.writeString(test2, SAMPLE_STRING);
            Files.writeString(test3, SAMPLE_STRING);
            Files.writeString(test4, SAMPLE_STRING);
            Files.writeString(test5, SAMPLE_STRING);

            converter.convertAllMarkdownFiles(inputPath, outputPath);

            try (DirectoryStream<Path> outputStream = Files.newDirectoryStream(outputPath)) {
                for (Path currentOutput : outputStream) {
                    String actualOutput = Files.readString(currentOutput);

                    assertTrue(currentOutput.toString().endsWith(".html"), "Converted not markdown file");
                    assertEquals(EXPECTED_STRING, actualOutput, "The file not converted as expected");
                }
            } catch (IOException e) {
                throw new IllegalStateException("A problem occurred", e);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Cannot create temp files", e);
        } finally {
            try {
                Files.deleteIfExists(test1);
                Files.deleteIfExists(test2);
                Files.deleteIfExists(test3);
                Files.deleteIfExists(test4);
                Files.deleteIfExists(test5);

                Files.deleteIfExists(Path.of("testOutput" + File.separator + "test1.html"));
                Files.deleteIfExists(Path.of("testOutput" + File.separator + "test2.html"));
                Files.deleteIfExists(Path.of("testOutput" + File.separator + "test3.html"));
                Files.deleteIfExists(Path.of("testOutput" + File.separator + "test4.html"));
                Files.deleteIfExists(Path.of("testOutput" + File.separator + "test5.html"));

                Files.deleteIfExists(inputPath);
                Files.deleteIfExists(outputPath);
            } catch (IOException e) {
                throw new IllegalStateException("Cannot delete files", e);
            }
        }
    }

    @Test
    void testChangeExtensionInvalidArguments() {
        assertThrows(IllegalArgumentException.class, () -> MarkdownConverter.changeExtension(null, ".txt"),
                "FileName cannot be null - expected IllegalArgumentException");
        assertThrows(IllegalArgumentException.class, () -> MarkdownConverter.changeExtension("fileName.txt", null),
                "NewExtension cannot be null - expected IllegalArgumentException");
    }

    @Test
    void testChangeExtension() {
        String actual = MarkdownConverter.changeExtension("file.txt", ".html");
        assertEquals("file.html", actual, "Extension not changed properly");
    }
}

