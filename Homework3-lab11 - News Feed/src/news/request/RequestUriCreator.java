package news.request;

import news.exception.KeywordsNotProvidedException;

import java.net.URI;
import java.net.URISyntaxException;

public class RequestUriCreator {
    private static final String DEFAULT_API_KEY = "2fe0f21ce80546e6a809025de8fd43f9";

    private static final String API_ENDPOINT_SCHEME = "https";
    private static final String API_ENDPOINT_HOST = "newsapi.org";
    private static final String API_ENDPOINT_PATH = "/v2/top-headlines";
    private static final String KEYWORD_PARAMETER_NAME = "q=";
    private static final String CATEGORY_PARAMETER_NAME = "category=";
    private static final String COUNTRY_PARAMETER_NAME = "country=";
    private static final String PAGE_NUMBER_PARAMETER_NAME = "page=";
    private static final String PAGE_SIZE_PARAMETER_NAME = "pageSize=";
    private static final String API_KEY_PARAMETER_NAME = "apiKey=";
    private static final String PARAMETER_DELIMITER = "&";

    private static final int MAX_PAGE_SIZE = 100;
    private static final int DEFAULT_PAGE_SIZE = 20;

    private final String[] keywords;
    private final String apiKey;

    private final String category;
    private final String country;
    private final Integer pageNumber;
    private final Integer pageSize;

    public static RequestUriBuilder builder(String... keywords) {
        if (keywords.length == 0) {
            throw new KeywordsNotProvidedException("There must be at least one keyword to search by");
        }
        for (String keyword : keywords) {
            if (keyword == null || keyword.isBlank()) {
                throw new IllegalArgumentException("Keywords cannot be null, empty or blank");
            }
        }

        return new RequestUriBuilder(keywords);
    }

    private RequestUriCreator(RequestUriBuilder builder) {
        this.keywords = builder.keywords;
        this.category = builder.category;
        this.country = builder.country;
        this.apiKey = builder.apiKey;
        this.pageNumber = builder.pageNumber;
        this.pageSize = builder.pageSize;
    }

    private String parametersToString() {
        StringBuilder parameters = new StringBuilder();

        for (String keyword : keywords) {
            parameters.append(KEYWORD_PARAMETER_NAME).append(keyword).append(PARAMETER_DELIMITER);
        }
        if (category != null) {
            parameters.append(CATEGORY_PARAMETER_NAME).append(category).append(PARAMETER_DELIMITER);
        }
        if (country != null) {
            parameters.append(COUNTRY_PARAMETER_NAME).append(country).append(PARAMETER_DELIMITER);
        }
        if (pageSize != null) {
            parameters.append(PAGE_SIZE_PARAMETER_NAME).append(pageSize).append(PARAMETER_DELIMITER);
        }
        if (pageNumber != null) {
            parameters.append(PAGE_NUMBER_PARAMETER_NAME).append(pageNumber).append(PARAMETER_DELIMITER);
        }
        parameters.append(API_KEY_PARAMETER_NAME).append(apiKey);

        return parameters.toString();
    }

    public URI toURI() throws URISyntaxException {
        String parameters = parametersToString();
        return new URI(API_ENDPOINT_SCHEME, API_ENDPOINT_HOST, API_ENDPOINT_PATH, parameters, null);
    }

    public static class RequestUriBuilder {
        private final String[] keywords;
        private String apiKey;

        private String category;
        private String country;
        private Integer pageNumber;
        private Integer pageSize;

        private RequestUriBuilder(String... keywords) {
            this.keywords = keywords;
            this.apiKey = DEFAULT_API_KEY;
            this.pageSize = DEFAULT_PAGE_SIZE;
        }

        public RequestUriBuilder category(String category) {
            if (category == null || category.isBlank()) {
                throw new IllegalArgumentException("Category cannot be null, empty or blank");
            }

            this.category = category;
            return this;
        }

        public RequestUriBuilder country(String country) {
            if (country == null || country.isBlank()) {
                throw new IllegalArgumentException("Country cannot be null, empty or blank");
            }

            this.country = country;
            return this;
        }

        public RequestUriBuilder pageNumber(int pageNumber) {
            if (pageNumber <= 0) {
                throw new IllegalArgumentException("Page number must be positive integer");
            }

            this.pageNumber = pageNumber;
            return this;
        }

        public RequestUriBuilder pageSize(int pageSize) {
            if (pageSize <= 0 || pageSize > MAX_PAGE_SIZE) {
                throw new IllegalArgumentException("Page size must be positive number not greater than " +
                        MAX_PAGE_SIZE);
            }

            this.pageSize = pageSize;
            return this;
        }

        public RequestUriBuilder apiKey(String apiKey) {
            if (apiKey == null || apiKey.isBlank()) {
                throw new IllegalArgumentException("API key cannot be null, empty or blank");
            }

            this.apiKey = apiKey;
            return this;
        }

        public int getPageSize() {
            return pageSize;
        }

        public RequestUriCreator build() {
            return new RequestUriCreator(this);
        }
    }
}
