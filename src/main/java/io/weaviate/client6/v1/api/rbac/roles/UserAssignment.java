package io.weaviate.client6.v1.api.rbac.roles;

import com.google.gson.annotations.SerializedName;

import io.weaviate.client6.v1.api.rbac.users.UserType;

public record UserAssignment(
    @SerializedName("userId") String userId,
    @SerializedName("userType") UserType userType) {
}
