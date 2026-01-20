package io.weaviate.client6.v1.api.cluster;

import java.util.List;

public record ShardReplica(String shardName, List<String> replicas) {
}
