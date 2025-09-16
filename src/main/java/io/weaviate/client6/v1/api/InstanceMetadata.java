package io.weaviate.client6.v1.api;

import java.util.Map;

import com.google.gson.annotations.SerializedName;

public record InstanceMetadata(
    @SerializedName("hostname") String hostName,
    @SerializedName("version") String version,
    @SerializedName("modules") Map<String, Object> modules,
    @SerializedName("grpcMaxMessageSize") Integer grpcMaxMessageSize) {
}
