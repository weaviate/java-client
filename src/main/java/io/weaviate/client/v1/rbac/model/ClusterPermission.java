package io.weaviate.client.v1.rbac.model;

import io.weaviate.client.v1.rbac.api.WeaviatePermission;
import lombok.AllArgsConstructor;
import lombok.Getter;

public class ClusterPermission implements Permission<ClusterPermission> {
  final String action;

  public ClusterPermission(Action action) {
    this.action = action.getValue();
  }

  @Override
  public WeaviatePermission toWeaviate() {
    return new WeaviatePermission(this.action);
  }

  @Override
  public ClusterPermission fromWeaviate(WeaviatePermission perm) {
    return null;
  }

  @AllArgsConstructor
  public enum Action {
    READ("read_cluster");

    @Getter
    private final String value;
  }
}
