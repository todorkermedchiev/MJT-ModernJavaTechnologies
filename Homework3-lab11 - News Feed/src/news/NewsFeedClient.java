package news;

import com.google.gson.Gson;
import news.exception.ErrorStatusException;
import news.exception.http.ApiKeyIsMissingException;
import news.exception.http.BadRequestException;
import news.exception.NewsFeedClientException;
import news.exception.http.TooManyRequestsException;
import news.exception.http.UnknownServerErrorException;
import news.model.News;
import news.model.ResponseBody;
import news.request.RequestUriCreator;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class NewsFeedClient implements Client {

    private static final String OK_STATUS = "ok";
    private static final String ERROR_STATUS = "error";
    private static final int TOO_MANY_REQUESTS_CODE = 429;

    private static final Gson GSON = new Gson();

    private final HttpClient client;

    public NewsFeedClient(HttpClient client) {
        this.client = client;
    }

    private ResponseBody getResponse(RequestUriCreator.RequestUriBuilder uriBuilder, int pageNumber) {
        HttpResponse<String> response;

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uriBuilder.pageNumber(pageNumber).build().toURI())
                    .build();
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (URISyntaxException | IOException | InterruptedException e) {
            throw new NewsFeedClientException("Could not receive news", e);
        }

        switch (response.statusCode()) {
            case HttpURLConnection.HTTP_OK:
                return GSON.fromJson(response.body(), ResponseBody.class);
            case HttpURLConnection.HTTP_BAD_REQUEST:
                throw new BadRequestException("The request was unacceptable, " +
                        "due to a missing or misconfigured parameter");
            case HttpURLConnection.HTTP_UNAUTHORIZED:
                throw new ApiKeyIsMissingException("API key is missing from the request, or isn't correct");
            case TOO_MANY_REQUESTS_CODE:
                throw new TooManyRequestsException("Too many requests made within a day");
        }

        throw new UnknownServerErrorException("Unexpected response code from news feed service");
    }

    @Override
    public List<News> get(RequestUriCreator.RequestUriBuilder uriBuilder, int maxPagesCount) {
        if (uriBuilder == null) {
            throw new IllegalArgumentException("The builder given cannot be null");
        }
        if (maxPagesCount <= 0) {
            throw new IllegalArgumentException("The pages count must be positive integer");
        }

        List<News> news = new ArrayList<>();

        int pageNumber = 1;
        int resultsCount = 0;
        do {
            ResponseBody response = getResponse(uriBuilder, pageNumber);

            if (response.status().equalsIgnoreCase(OK_STATUS)) {
                resultsCount = response.totalResults();
                news.addAll(response.news());
            } else if (response.status().equalsIgnoreCase(ERROR_STATUS)) {
                throw new ErrorStatusException("Error: " + response.errorCode() + " - " + response.errorMessage());
            }

            ++pageNumber;
        } while (pageNumber * uriBuilder.getPageSize() < resultsCount && pageNumber <= maxPagesCount);

        return news;
    }
}
