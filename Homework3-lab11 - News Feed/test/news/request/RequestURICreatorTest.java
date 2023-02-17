package news.request;

import news.exception.KeywordsNotProvidedException;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class RequestURICreatorTest {
    private static final int DEFAULT_PAGE_SIZE = 20;
    @Test
    void testCreateUriWithoutKeywords() {
        assertThrows(KeywordsNotProvidedException.class, RequestUriCreator::builder,
                "Unexpected RequestUriBuilder returned without keywords provided - " +
                        "expected KeywordsNotProvidedException to be thrown");
    }

    @Test
    void testCreateUriWithNullKeywords() {
        assertThrows(IllegalArgumentException.class, () -> RequestUriCreator.builder((String) null),
                "Unexpected RequestUriBuilder returned with null keywords provided - " +
                        "expected IllegalArgumentException to be thrown");
    }

    @Test
    void testCreateUriWithBlankKeywords() {
        assertThrows(IllegalArgumentException.class, () -> RequestUriCreator.builder("   "),
                "Unexpected RequestUriBuilder returned with blank keywords provided - " +
                        "expected IllegalArgumentException to be thrown");
    }

    @Test
    void testCreateUriWithEmptyKeywords() {
        assertThrows(IllegalArgumentException.class, () -> RequestUriCreator.builder(""),
                "Unexpected RequestUriBuilder returned with empty keywords provided - " +
                        "expected IllegalArgumentException to be thrown");
    }

    @Test
    void testCreateUriValidKeywords() throws URISyntaxException {
        String expected = "https://newsapi.org/v2/top-headlines?q=keyword1&q=keyword2&pageSize=" +
                DEFAULT_PAGE_SIZE + "&apiKey=2fe0f21ce80546e6a809025de8fd43f9";

        String actual = RequestUriCreator.builder("keyword1", "keyword2").build().toURI().toString();

        assertEquals(expected, actual, "Unexpected uri returned");
    }

    @Test
    void testCreateUriWithCountryParameter() throws URISyntaxException {
        String expected = "https://newsapi.org/v2/top-headlines?q=keyword1&q=keyword2&country=bg&pageSize=" +
                DEFAULT_PAGE_SIZE + "&apiKey=2fe0f21ce80546e6a809025de8fd43f9";

        String actual = RequestUriCreator.builder("keyword1", "keyword2").country("bg")
                .build()
                .toURI()
                .toString();

        assertEquals(expected, actual, "Unexpected uri returned");
    }

    @Test
    void testCreateUriWithCategoryParameter() throws URISyntaxException {
        String expected = "https://newsapi.org/v2/top-headlines?q=keyword1&q=keyword2&category=business&pageSize=" +
                DEFAULT_PAGE_SIZE + "&apiKey=2fe0f21ce80546e6a809025de8fd43f9";

        String actual = RequestUriCreator.builder("keyword1", "keyword2").category("business")
                .build()
                .toURI()
                .toString();

        assertEquals(expected, actual, "Unexpected uri returned");
    }

    @Test
    void testCreateUriWithPageNumberParameter() throws URISyntaxException {
        String expected = "https://newsapi.org/v2/top-headlines?q=keyword1&q=keyword2&pageSize=" +
                DEFAULT_PAGE_SIZE + "&page=2&apiKey=2fe0f21ce80546e6a809025de8fd43f9";

        String actual = RequestUriCreator.builder("keyword1", "keyword2").pageNumber(2)
                .build()
                .toURI()
                .toString();

        assertEquals(expected, actual, "Unexpected uri returned");
    }

    @Test
    void testCreateUriWithPageSizeParameter() throws URISyntaxException {
        String expected = "https://newsapi.org/v2/top-headlines?q=keyword1&q=keyword2&pageSize=50" +
                "&apiKey=2fe0f21ce80546e6a809025de8fd43f9";

        String actual = RequestUriCreator.builder("keyword1", "keyword2").pageSize(50)
                .build()
                .toURI()
                .toString();

        assertEquals(expected, actual, "Unexpected uri returned");
    }

    @Test
    void testCreateUriWithApiKeyParameter() throws URISyntaxException {
        String expected = "https://newsapi.org/v2/top-headlines?q=keyword1&q=keyword2&pageSize=" +
                DEFAULT_PAGE_SIZE + "&apiKey=customApiKey123";

        String actual = RequestUriCreator.builder("keyword1", "keyword2").apiKey("customApiKey123")
                .build()
                .toURI()
                .toString();

        assertEquals(expected, actual, "Unexpected uri returned");
    }

    @Test
    void testCreateUriWithDifferentParameters() throws URISyntaxException {
        String expected = "https://newsapi.org/v2/top-headlines?q=keyword1&q=keyword2&category=business&country=bg" +
                "&pageSize=" + DEFAULT_PAGE_SIZE + "&apiKey=2fe0f21ce80546e6a809025de8fd43f9";

        String actual = RequestUriCreator.builder("keyword1", "keyword2")
                .country("bg")
                .category("business")
                .build()
                .toURI()
                .toString();

        assertEquals(expected, actual, "Unexpected uri returned");
    }

    @Test
    void testCreateUriInvalidCategoryString() {
        assertThrows(IllegalArgumentException.class, () -> RequestUriCreator.builder("keyword").category(null),
                "Expected IllegalArgumentException to be thrown with null category string, but it was not");
        assertThrows(IllegalArgumentException.class, () -> RequestUriCreator.builder("keyword").category(""),
                "Expected IllegalArgumentException to be thrown with empty category string, but it was not");
        assertThrows(IllegalArgumentException.class, () -> RequestUriCreator.builder("keyword").category("  "),
                "Expected IllegalArgumentException to be thrown with blank category string, but it was not");
    }

    @Test
    void testCreateUriInvalidCountryString() {
        assertThrows(IllegalArgumentException.class, () -> RequestUriCreator.builder("keyword").country(null),
                "Expected IllegalArgumentException to be thrown with null country string, but it was not");
        assertThrows(IllegalArgumentException.class, () -> RequestUriCreator.builder("keyword").country(""),
                "Expected IllegalArgumentException to be thrown with empty country string, but it was not");
        assertThrows(IllegalArgumentException.class, () -> RequestUriCreator.builder("keyword").country("  "),
                "Expected IllegalArgumentException to be thrown with blank country string, but it was not");
    }

    @Test
    void testCreateUriInvalidApiKeyString() {
        assertThrows(IllegalArgumentException.class, () -> RequestUriCreator.builder("keyword").apiKey(null),
                "Expected IllegalArgumentException to be thrown with null apiKey string, but it was not");
        assertThrows(IllegalArgumentException.class, () -> RequestUriCreator.builder("keyword").apiKey(""),
                "Expected IllegalArgumentException to be thrown with empty apiKey string, but it was not");
        assertThrows(IllegalArgumentException.class, () -> RequestUriCreator.builder("keyword").apiKey("  "),
                "Expected IllegalArgumentException to be thrown with blank apiKey string, but it was not");
    }

    @Test
    void testCreateUriNegativePageNumber() {
        assertThrows(IllegalArgumentException.class, () -> RequestUriCreator.builder("keyword").pageNumber(-1),
                "Expected IllegalArgumentException to be thrown with negative page number, but it was not");
    }

    @Test
    void testCreateUriNegativePageSize() {
        assertThrows(IllegalArgumentException.class, () -> RequestUriCreator.builder("keyword").pageSize(-1),
                "Expected IllegalArgumentException to be thrown with negative page size, but it was not");
    }

    @Test
    void testCreateUriPageSizeGreaterThanMaxPageSize() {
        assertThrows(IllegalArgumentException.class, () -> RequestUriCreator.builder("keyword").pageSize(200),
                "Expected IllegalArgumentException to be thrown with big page number, but it was not");
    }
}
