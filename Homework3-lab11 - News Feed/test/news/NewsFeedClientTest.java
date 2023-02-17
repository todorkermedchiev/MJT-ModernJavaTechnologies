package news;

import news.exception.http.ApiKeyIsMissingException;
import news.exception.http.BadRequestException;
import news.exception.http.TooManyRequestsException;
import news.exception.http.UnknownServerErrorException;
import news.model.News;
import news.model.Source;
import news.request.RequestUriCreator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class NewsFeedClientTest {
    private static final int TOO_MANY_REQUESTS_CODE = 429;
    private static final String VALID_JSON_RESPONSE = """
            {
                "status": "ok",
                "totalResults": 2,
                "articles": [
                    {
                        "source": {
                            "id": "123",
                            "name": "Source1"
                        },
                        "author": "Author1",
                        "title": "Title 1",
                        "description": "Description 1",
                        "url": "url1",
                        "urlToImage": "urlToImage1",
                        "publishedAt": "date1",
                        "content": "Content 1 containing keyword1"
                    },
                    {
                        "source": {
                            "id": "234",
                            "name": "Source2"
                        },
                        "author": "Author2",
                        "title": "Title 2",
                        "description": "Description 2",
                        "url": "url2",
                        "urlToImage": "urlToImage2",
                        "publishedAt": "date2",
                        "content": "Content 2 containing keyword2"
                    }
                ]
            }
            """;

    @Mock
    private HttpClient newsFeedClientMock;

    @Mock
    private HttpResponse<String> httpResponseMock;

    @InjectMocks
    private NewsFeedClient client;

    @BeforeEach
    void setUp() throws IOException, InterruptedException {
        when(newsFeedClientMock.send(Mockito.any(HttpRequest.class), ArgumentMatchers.<BodyHandler<String>>any()))
                .thenReturn(httpResponseMock);
    }

    @Test
    void testGetNewsNullBuilderProvided() {
        assertThrows(IllegalArgumentException.class, () -> client.get(null, 2),
                "Expected IllegalArgumentException to be thrown when uriBuilder is null");
    }

    @Test
    void testGetNewsNegativePagesCount() {
        var builder = RequestUriCreator.builder("keyword");

        assertThrows(IllegalArgumentException.class, () -> client.get(builder, -1),
                "Expected IllegalArgumentException to be thrown when pagesCount is negative");
    }

    @Test
    void testGetNewsHttpOkResponseCode() throws IOException, InterruptedException {
        var builder = RequestUriCreator.builder("keyword");

        when(httpResponseMock.statusCode()).thenReturn(HttpURLConnection.HTTP_OK);
        when(httpResponseMock.body()).thenReturn(VALID_JSON_RESPONSE);

        News news1 = new News("Author1",
                "Title 1",
                "Description 1",
                "Content 1 containing keyword1",
                new Source("123", "Source1"),
                "url1",
                "urlToImage1",
                "date1");
        News news2 = new News("Author2",
                "Title 2",
                "Description 2",
                "Content 2 containing keyword2",
                new Source("234", "Source2"),
                "url2",
                "urlToImage2",
                "date2");

        List<News> expected = List.of(news1, news2);

        List<News> results = client.get(builder, 2);

        assertEquals(expected, results, "The response json not converted properly into object");
    }

    @Test
    void testGetNewsApiKeyIsMissingResponseCode() {
        var builder = RequestUriCreator.builder("keyword");

        when(httpResponseMock.statusCode()).thenReturn(HttpURLConnection.HTTP_UNAUTHORIZED);

        assertThrows(ApiKeyIsMissingException.class, () -> client.get(builder, 2),
                "Expected ApiKeyIsMissingException to be thrown when the response code from the server is " +
                        "HTTP_UNAUTHORIZED, but it was not thrown");
    }

    @Test
    void testGetNewsBadRequestCode() {
        var builder = RequestUriCreator.builder("keyword");

        when(httpResponseMock.statusCode()).thenReturn(HttpURLConnection.HTTP_BAD_REQUEST);

        assertThrows(BadRequestException.class, () -> client.get(builder, 2),
                "Expected BadRequestException to be thrown when the response code from the server is " +
                        "HTTP_BAD_REQUEST, but it was not thrown");
    }

    @Test
    void testGetNewsTooManyRequestsCode() {
        var builder = RequestUriCreator.builder("keyword");

        when(httpResponseMock.statusCode()).thenReturn(TOO_MANY_REQUESTS_CODE);

        assertThrows(TooManyRequestsException.class, () -> client.get(builder, 2),
                "Expected TooManyRequestsException to be thrown when the response code from the server is " +
                        "TOO_MANY_REQUESTS_CODE (429), but it was not thrown");
    }

    @Test
    void testGetNewsUnknownHttpErrorCode() {
        var builder = RequestUriCreator.builder("keyword");

        when(httpResponseMock.statusCode()).thenReturn(500);

        assertThrows(UnknownServerErrorException.class, () -> client.get(builder, 2),
                "Expected UnknownServerErrorException to be thrown when the response code from the server is " +
                        "500, but it was not thrown");
    }
}
