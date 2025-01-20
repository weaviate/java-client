package io.weaviate.client.v1.rbac.model;

import java.util.Arrays;
import java.util.List;

public class Role {
  public final String name;
  public final List<Permission<?>> permissions;

  public Role(String name, Permission<?>... permissions) {
    this.name = name;
    this.permissions = Arrays.asList(permissions);
  }

}
