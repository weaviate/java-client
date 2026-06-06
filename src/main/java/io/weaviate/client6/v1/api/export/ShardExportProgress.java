package io.weaviate.client6.v1.api.export;

import com.google.gson.annotations.SerializedName;

public record ShardExportProgress(
    @SerializedName("status") ShardExportStatus status,
    @SerializedName("objectsExported") Integer objectsExported,
    @SerializedName("error") String error,
    @SerializedName("skipReason") String skipReason) {
}
