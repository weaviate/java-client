package io.weaviate.client6.v1.api.rbac.users;

import java.time.OffsetDateTime;
import java.util.List;

import com.google.gson.annotations.SerializedName;

public record DbUser(
    @SerializedName("userId") String id,
    @SerializedName("dbUserType") UserType userType,
    @SerializedName("active") boolean active,
    @SerializedName("roles") List<String> roleNames,
    @SerializedName("createdAt") OffsetDateTime createdAt,
    @SerializedName("lastUsedAt") OffsetDateTime lastUsedAt,
    @SerializedName("apiKeyFirstLetters") String apiKeyFirstLetters) {
}
