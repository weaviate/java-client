package io.weaviate.client6.v1.api.rbac.roles;

import com.google.gson.annotations.SerializedName;

import io.weaviate.client6.v1.api.rbac.groups.GroupType;

public record GroupAssignment(
    @SerializedName("groupId") String groupId,
    @SerializedName("groupType") GroupType groupType) {
}
