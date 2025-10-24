package io.weaviate.client6.v1.api.collections;

import com.google.gson.annotations.SerializedName;

public record GeoCoordinates(
    @SerializedName("latitude") float latitude,
    @SerializedName("longitude") float longitude) {
}
