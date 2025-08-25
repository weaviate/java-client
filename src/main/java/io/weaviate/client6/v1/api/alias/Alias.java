package io.weaviate.client6.v1.api.alias;

import com.google.gson.annotations.SerializedName;

public record Alias(@SerializedName("class") String collection, @SerializedName("alias") String alias) {
}
