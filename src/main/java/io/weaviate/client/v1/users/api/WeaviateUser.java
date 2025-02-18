package io.weaviate.client.v1.users.api;

import java.util.List;
import java.util.stream.Collectors;

import io.weaviate.client.v1.rbac.api.WeaviateRole;
import io.weaviate.client.v1.users.model.User;

public class WeaviateUser {
  String name;
  String id;
  List<WeaviateRole> roles;

  public User toUser() {
    return new User(name, id, roles.stream().map(WeaviateRole::toRole).collect(Collectors.toList()));
  }
}
