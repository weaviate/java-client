
package io.weaviate.client.v1.rbac.model;

import io.weaviate.client.v1.rbac.api.WeaviatePermission;
import lombok.AllArgsConstructor;
import lombok.Getter;

public class UsersPermission implements Permission<UsersPermission> {
  final String action;

  public UsersPermission(Action action) {
    this.action = action.getValue();
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
