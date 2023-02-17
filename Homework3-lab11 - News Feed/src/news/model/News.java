package news.model;

public record News(String author,
                   String title,
                   String description,
                   String content,
                   Source source,
                   String url,
                   String urlToImage,
                   String publishedAt) {
}
