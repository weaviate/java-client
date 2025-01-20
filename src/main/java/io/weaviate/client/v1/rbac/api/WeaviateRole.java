package io.weaviate.client.v1.rbac.api;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.weaviate.client.v1.rbac.model.Permission;
import io.weaviate.client.v1.rbac.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class WeaviateRole {
  String name;
  List<Map<String, Map<String, ?>>> permissions;

  public WeaviateRole(Role role) {
    this.name = role.name;
    this.permissions = mergePermissions(role.permissions);
  }

  private static List<Map<String, Map<String, ?>>> mergePermissions(List<Permission<?>> permissions) {
    return null;
  }

  public Role toRole() {
    return null;
  }
}
