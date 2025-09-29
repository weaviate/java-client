package io.weaviate.client6.v1.api.backup;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public record Backup(
    @SerializedName("id") String id,
    @SerializedName("path") String path,
    @SerializedName("backend") String backend,
    @SerializedName("classes") List<String> includesCollections,
    @SerializedName("status") BackupStatus status,
    @SerializedName("error") String error) {
}
