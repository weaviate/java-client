package io.weaviate.client.v1.rbac.model;

import io.weaviate.client.v1.rbac.api.WeaviatePermission;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = true)
public class RolesPermission extends Permission<RolesPermission> {
  final String role;

  public RolesPermission(String role, Action action) {
    super(action);
    this.role = role;
  }

  RolesPermission(String role, String action) {
    this(role, RbacAction.fromString(Action.class, action));
  }

  @Override
  public WeaviatePermission toWeaviate() {
    return new WeaviatePermission(this.action, this);
  }

  @AllArgsConstructor
  public enum Action implements RbacAction {
    READ("read_roles"),
    MANAGE("manage_roles");

    @Getter
    private final String value;
  }
}
