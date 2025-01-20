package io.weaviate.client.v1.rbac.model;

import io.weaviate.client.v1.rbac.api.WeaviatePermission;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
public class ClusterPermission implements Permission<ClusterPermission> {
  final String action;

  public ClusterPermission(Action action) {
    this.action = action.getValue();
  }

  ClusterPermission(String action) {
    this(CustomAction.fromString(Action.class, action));
  }

  @Override
  public WeaviatePermission toWeaviate() {
    return new WeaviatePermission(this.action);
  }

  @AllArgsConstructor
  public enum Action implements CustomAction {
    READ("read_cluster");

    @Getter
    private final String value;
  }
}
