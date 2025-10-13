package io.weaviate.client6.v1.api.rbac.users;

import java.util.List;

import com.google.gson.annotations.SerializedName;

import io.weaviate.client6.v1.api.rbac.Role;

public record User(
    @SerializedName("username") String id,
    @SerializedName("roles") List<Role> roles) {
}
