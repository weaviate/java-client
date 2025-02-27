
package io.weaviate.client.v1.rbac.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class UsersPermission extends Permission<UsersPermission> {
  public UsersPermission(Action... actions) {
    super(actions);
  }

  UsersPermission(String action) {
    this(RbacAction.fromString(Action.class, action));
  }

  @AllArgsConstructor
  public enum Action implements RbacAction {
    READ("read_users"),
    ASSIGN_AND_REVOKE("assign_and_revoke_users");

    @Getter
    private final String value;
  }
}
