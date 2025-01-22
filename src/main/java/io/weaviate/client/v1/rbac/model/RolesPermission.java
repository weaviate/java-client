package io.weaviate.client.v1.rbac.model;

import io.weaviate.client.v1.rbac.api.WeaviatePermission;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
public class RolesPermission extends Permission<RolesPermission> {
  final String role;

  public RolesPermission(Action action, String role) {
    super(action);
    this.role = role;
  }

  RolesPermission(String action, String role) {
    this(CustomAction.fromString(Action.class, action), role);
  }

  @Override
  public WeaviatePermission toWeaviate() {
    return new WeaviatePermission(this.action, this);
  }

  @AllArgsConstructor
  public enum Action implements CustomAction {
    READ("read_roles"),
    MANAGE("manage_roles");

    @Getter
    private final String value;
  }
}
