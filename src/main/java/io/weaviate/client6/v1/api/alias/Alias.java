package io.weaviate.client6.v1.api.alias;

import com.google.gson.annotations.SerializedName;

public record Alias(
    /** Original collection name. */
    @SerializedName("class") String collection,
    /** Collection alias. */
    @SerializedName("alias") String alias) {
}
