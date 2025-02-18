package io.weaviate.client.v1.rbac.model;

import com.google.gson.annotations.SerializedName;

import io.weaviate.client.v1.rbac.api.WeaviatePermission;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = true)
public class RolesPermission extends Permission<RolesPermission> {
  final String role;
  final Scope scope;

  public RolesPermission(String role, Action action) {
    this(role, null, action);
  }

  public RolesPermission(String role, Scope scope, Action action) {
    super(action);
    this.role = role;
    this.scope = scope;
  }

  RolesPermission(String role, Scope scope, String action) {
    this(role, scope, RbacAction.fromString(Action.class, action));
  }

  @Override
  public WeaviatePermission toWeaviate() {
    return new WeaviatePermission(this.action, this);
  }

  @AllArgsConstructor
  public enum Action implements RbacAction {
    CREATE("create_roles"),
    READ("read_roles"),
    UPDATE("update_roles"),
    DELETE("delete_roles"),

    /*
     * DO NOT CREATE NEW PERMISSIONS WITH THIS ACTION.
     * It is preserved for backward compatibility with 1.28
     * and should only be used internally to read legacy permissions.
     */
    @Deprecated
    MANAGE("manage_roles");

    @Getter
    private final String value;
  }

  public enum Scope {
    @SerializedName("all")
    ALL,
    @SerializedName("match")
    MATCH;
  }
}
