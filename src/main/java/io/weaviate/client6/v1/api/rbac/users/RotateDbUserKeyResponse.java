package io.weaviate.client6.v1.api.rbac.users;

import com.google.gson.annotations.SerializedName;

public record RotateDbUserKeyResponse(@SerializedName("apikey") String apiKey) {
}
