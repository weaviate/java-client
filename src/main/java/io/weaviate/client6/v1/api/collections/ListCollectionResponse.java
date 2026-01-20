package io.weaviate.client6.v1.api.collections;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public record ListCollectionResponse(@SerializedName("classes") List<CollectionConfig> collections) {
}
