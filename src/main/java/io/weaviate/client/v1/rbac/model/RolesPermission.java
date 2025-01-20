package io.weaviate.client.v1.rbac.model;

import io.weaviate.client.v1.rbac.api.WeaviatePermission;
import lombok.AllArgsConstructor;
import lombok.Getter;

public class RolesPermission implements Permission<RolesPermission> {
  final transient String action;
  final String role;

  public RolesPermission(Action action, String role) {
    this.action = action.getValue();
    this.role = role;
  }

  @Override
  public WeaviatePermission toWeaviate() {
    return new WeaviatePermission(this.action, this);
  }

  @Override
  public RolesPermission fromWeaviate(WeaviatePermission perm) {
    return null;
  }

  @AllArgsConstructor
  public enum Action {
    READ("read_roles"),
    MANAGE("manage_roles");

    @Getter
    private final String value;
  }
}
