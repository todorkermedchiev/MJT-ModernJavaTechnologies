package news;

import news.model.News;
import news.request.RequestUriCreator;

import java.util.List;

public interface Client {
    /**
     *
     * @param uriBuilder Builder to create the required URI. The builder must be set with all criteria to search by.
     *                   Keywords are required, country, category, pageSize and apiKey are optional. Page number is
     *                   insignificant, because it is set by the Client. If apiKey and pageSize are not set,
     *                   the default values will be used
     * @param maxPagesCount The maximum number of pages wanted. The count of all articles provided is not greater than
     *                      pageSize * maxPagesCount
     * @return List of all news found
     */
    List<News> get(RequestUriCreator.RequestUriBuilder uriBuilder, int maxPagesCount);
}
