package io.weaviate.client6.v1.collections;

import java.util.Map;

import com.google.gson.annotations.SerializedName;

public class NoneVectorizer extends Vectorizer {
  @SerializedName("none")
  private final Map<String, Object> _configuration = Map.of();
}
