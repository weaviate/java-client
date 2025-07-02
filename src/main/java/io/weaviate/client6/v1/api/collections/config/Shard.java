package io.weaviate.client6.v1.api.collections.config;

public record Shard(String name, String status, long vectorQueueSize) {
}
