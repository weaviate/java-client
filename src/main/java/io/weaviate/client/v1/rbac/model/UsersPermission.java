
package io.weaviate.client.v1.rbac.model;

import io.weaviate.client.v1.rbac.api.WeaviatePermission;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * UsersPermission controls access to dynamic user management capabilities.
 * These will be introduced in v1.30. Until then the class will remain
 * package-private.
 */
class UsersPermission extends Permission<UsersPermission> {
  public UsersPermission(Action action) {
    super(action);
  }

  UsersPermission(String action) {
    this(CustomAction.fromString(Action.class, action));
  }

  @Override
  public WeaviatePermission toWeaviate() {
    return new WeaviatePermission(this.action);
  }

  @AllArgsConstructor
  public enum Action implements CustomAction {
    MANAGE("manage_users");

    @Getter
    private final String value;
  }
}
