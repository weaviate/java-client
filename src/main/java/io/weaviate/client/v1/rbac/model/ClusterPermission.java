package io.weaviate.client.v1.rbac.model;

import io.weaviate.client.v1.rbac.api.WeaviatePermission;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = true)
public class ClusterPermission extends Permission<ClusterPermission> {
  public ClusterPermission(Action action) {
    super(action);
  }

  ClusterPermission(String action) {
    this(RbacAction.fromString(Action.class, action));
  }

  @Override
  public WeaviatePermission toWeaviate() {
    return new WeaviatePermission(this.action);
  }

  @AllArgsConstructor
  public enum Action implements RbacAction {
    READ("read_cluster");

    @Getter
    private final String value;
  }
}
