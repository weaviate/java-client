package technology.semi.weaviate.client.v1.misc.model;

import com.google.gson.annotations.SerializedName;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InvertedIndexConfig {
    @SerializedName("bm25")
    BM25Config bm25Config;
    @SerializedName("stopwords")
    StopwordConfig stopwordConfig;

    public void setBm25Config(BM25Config config) {
        this.bm25Config = config;
    }

    public void setStopwordConfig(StopwordConfig config) {
        this.stopwordConfig = config;
    }
}
