package io.weaviate.client.v1.rbac.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = true)
public class TenantsPermission extends Permission<TenantsPermission> {

  public TenantsPermission(Action action) {
    super(action);
  }

  TenantsPermission(String action) {
    this(RbacAction.fromString(Action.class, action));
  }

  @AllArgsConstructor
  public enum Action implements RbacAction {
    CREATE("create_tenants"),
    READ("read_tenants"),
    UPDATE("update_tenants"),
    DELETE("delete_tenants");

    @Getter
    private final String value;
  }
}
