package io.weaviate.client6.v1.api.backup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import com.google.gson.annotations.SerializedName;

import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.json.JSON;
import io.weaviate.client6.v1.internal.rest.Endpoint;
import io.weaviate.client6.v1.internal.rest.SimpleEndpoint;

public record CreateBackupRequest(BackupCreate body, String backend) {

  public static Endpoint<CreateBackupRequest, Backup> _ENDPOINT = new SimpleEndpoint<>(
      request -> "POST",
      request -> "/backups/" + request.backend,
      request -> Collections.emptyMap(),
      request -> JSON.serialize(request.body),
      (statusCode, response) -> JSON.deserialize(response, Backup.class));

  public static record BackupCreate(
      @SerializedName("id") String id,
      @SerializedName("include") List<String> includeCollections,
      @SerializedName("exclude") List<String> excludeCollections,
      @SerializedName("config") Config config) {

    private static record Config(
        @SerializedName("CPUPercentage") Integer cpuPercentage,
        @SerializedName("CompressionLevel") CompressionLevel compressionLevel,
        @SerializedName("Bucket") String bucket,
        @SerializedName("Path") String path) {
    }

    public static BackupCreate of(String backupId) {
      return of(backupId, ObjectBuilder.identity());
    }

    public static BackupCreate of(String backupId, Function<Builder, ObjectBuilder<BackupCreate>> fn) {
      return fn.apply(new Builder(backupId)).build();
    }

    public BackupCreate(Builder builder) {
      this(
          builder.backupId,
          builder.includeCollections,
          builder.excludeCollections,
          new Config(
              builder.cpuPercentage,
              builder.compressionLevel,
              builder.bucket,
              builder.path));
    }

    public static class Builder implements ObjectBuilder<BackupCreate> {
      private final String backupId;

      private Integer cpuPercentage;
      private CompressionLevel compressionLevel;
      private String bucket;
      private String path;
      private final List<String> includeCollections = new ArrayList<>();
      private final List<String> excludeCollections = new ArrayList<>();

      public Builder(String backupId) {
        this.backupId = backupId;
      }

      /** Collection that should be included in the backup. */
      public Builder includeCollections(String... includeCollections) {
        return includeCollections(Arrays.asList(includeCollections));
      }

      /** Collection that should be included in the backup. */
      public Builder includeCollections(List<String> includeCollections) {
        this.includeCollections.addAll(includeCollections);
        return this;
      }

      /** Collection that should be excluded from the backup. */
      public Builder excludeCollections(String... excludeCollections) {
        return excludeCollections(Arrays.asList(excludeCollections));
      }

      /** Collection that should be excluded from the backup. */
      public Builder excludeCollections(List<String> excludeCollections) {
        this.excludeCollections.addAll(excludeCollections);
        return this;
      }

      /**
       * Set the desired CPU core utilization.
       *
       * @param cpuPercentage Percent value of the target CPU utilization (1% to 80%).
       */
      public Builder cpuPercentage(int cpuPercentage) {
        this.cpuPercentage = cpuPercentage;
        return this;
      }

      /** Adjust the parameters of the selected compression algorithm. */
      public Builder compressionLevel(CompressionLevel compressionLevel) {
        this.compressionLevel = compressionLevel;
        return this;
      }

      /**
       * Set the bucket where backups are stored.
       * Applicable for cloud storage backends.
       */
      public Builder bucket(String bucket) {
        this.bucket = bucket;
        return this;
      }

      /** Override default backup location. */
      public Builder path(String path) {
        this.path = path;
        return this;
      }

      @Override
      public BackupCreate build() {
        return new BackupCreate(this);
      }
    }
  }
}
