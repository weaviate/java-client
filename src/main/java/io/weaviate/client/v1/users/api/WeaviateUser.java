package io.weaviate.client.v1.users.api;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.annotations.SerializedName;

import io.weaviate.client.v1.rbac.api.WeaviateRole;
import io.weaviate.client.v1.users.model.User;

public class WeaviateUser {
  @SerializedName("username")
  String username;

  @SerializedName("user_id")
  String id;

  @SerializedName("roles")
  List<WeaviateRole> roles = new ArrayList<>();

  public User toUser() {
    return new User(id != null ? id : username,
        roles.stream().map(WeaviateRole::toRole).collect(Collectors.toList()));
  }
}
