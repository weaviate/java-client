
package io.weaviate.client.v1.rbac.model;

import io.weaviate.client.v1.rbac.api.WeaviatePermission;
import lombok.AllArgsConstructor;
import lombok.Getter;

public class UsersPermission implements Permission<UsersPermission> {
  final String action;

  public UsersPermission(Action action) {
    this.action = action.getValue();
  }

  @Override
  public WeaviatePermission toWeaviate() {
    return new WeaviatePermission(this.action);
  }

  @Override
  public UsersPermission fromWeaviate(WeaviatePermission perm) {
    return null;
  }

  @AllArgsConstructor
  public enum Action {
    MANAGE("manage_users");

    @Getter
    private final String value;
  }
}
