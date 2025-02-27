package io.weaviate.client.v1.users.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.weaviate.client.v1.rbac.model.Role;
import lombok.Getter;

@Getter
public class User {
  String userId;
  Map<String, Role> roles = new HashMap<>();

  public User(String id, List<Role> roles) {
    this.userId = id;
    this.roles = roles.stream().collect(Collectors.toMap(Role::getName, r -> r));
  }
}
