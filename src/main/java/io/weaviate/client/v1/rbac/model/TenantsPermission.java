package io.weaviate.client.v1.rbac.model;

import io.weaviate.client.v1.rbac.api.WeaviatePermission;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
public class TenantsPermission implements Permission<TenantsPermission> {
  final transient String action;
  final String tenant;

  public TenantsPermission(Action action) {
    this(action, "*");
  }

  private TenantsPermission(Action action, String tenant) {
    this.action = action.getValue();
    this.tenant = tenant;
  }

  @Override
  public WeaviatePermission toWeaviate() {
    return new WeaviatePermission(this.action, this);
  }

  @Override
  public TenantsPermission fromWeaviate(WeaviatePermission perm) {
    return null;
  }

  @AllArgsConstructor
  public enum Action {
    CREATE("create_tenants"),
    READ("read_tenants"),
    UPDATE("update_tenants"),
    DELETE("delete_tenants");

    @Getter
    private final String value;
  }
}
