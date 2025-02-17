package io.weaviate.client.v1.users.model;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.weaviate.client.v1.rbac.model.Role;
import lombok.Getter;

@Getter
public class User {
  String name;
  String userId;
  Map<String, Role> roles;

  public User(String name, String id, List<Role> roles) {
    this.name = name;
    this.userId = id;
    this.roles = roles.stream().collect(Collectors.toMap(Role::getName, r -> r));
  }
}
