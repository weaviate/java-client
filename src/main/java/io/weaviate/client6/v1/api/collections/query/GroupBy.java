package io.weaviate.client6.v1.api.collections.query;

import java.util.List;

import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoSearchGet;

public record GroupBy(List<String> path, int maxGroups, int maxObjectsPerGroup) {
  public static GroupBy property(String property, int maxGroups, int maxObjectsPerGroup) {
    return new GroupBy(List.of(property), maxGroups, maxObjectsPerGroup);
  }

  void appendTo(WeaviateProtoSearchGet.SearchRequest.Builder req) {
    req.setGroupBy(WeaviateProtoSearchGet.GroupBy.newBuilder()
        .addAllPath(path)
        .setNumberOfGroups(maxGroups)
        .setObjectsPerGroup(maxObjectsPerGroup));
  }
}
