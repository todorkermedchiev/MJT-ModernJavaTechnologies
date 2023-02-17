package news.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public record ResponseBody(String status,
                           int totalResults,
                           @SerializedName("articles") List<News> news,
                           @SerializedName("code") String errorCode,
                           @SerializedName("message") String errorMessage) {
}
