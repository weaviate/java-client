package io.weaviate.client.v1.rbac.model;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Role {
  public final String name;
  public List<? extends Permission<?>> permissions = new ArrayList<>();

  public String toString() {
    return String.format(
        "Role<name='%s', permissions=[%s]>",
        this.name, permissions.isEmpty()
            ? "none"
            : String.join(", ", permissions.stream().map(Permission::getAction).toList()));
  }
}
