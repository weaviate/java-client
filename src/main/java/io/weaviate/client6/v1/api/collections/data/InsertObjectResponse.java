package io.weaviate.client6.v1.api.collections.data;

import com.google.gson.annotations.SerializedName;

import io.weaviate.client6.v1.api.collections.Vectors;

public record InsertObjectResponse<T>(
    @SerializedName("class") String collectionName,
    @SerializedName("properties") T properties,
    @SerializedName("id") String uuid,
    @SerializedName("vectors") Vectors vectors,
    @SerializedName("creationTimeUnix") Long createdAt,
    @SerializedName("lastUpdateTimeUnix") Long lastUpdatedAt) {
}
