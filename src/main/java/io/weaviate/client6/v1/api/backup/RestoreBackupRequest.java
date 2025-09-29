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

public record RestoreBackupRequest(String backupId, String backend, BackupRestore body) {

  public static Endpoint<RestoreBackupRequest, Backup> _ENDPOINT = new SimpleEndpoint<>(
      request -> "POST",
      request -> "/backups/" + request.backend + "/" + request.backupId + "/restore",
      request -> Collections.emptyMap(),
      request -> JSON.serialize(request.body),
      (statusCode, response) -> JSON.deserialize(response, Backup.class));

  public record BackupRestore(
      @SerializedName("include") List<String> includeCollections,
      @SerializedName("exclude") List<String> excludeCollections,
      @SerializedName("overwriteAlias") Boolean overwriteAlias,
      @SerializedName("config") Config config) {

    public record Config(
        @SerializedName("CPUPercentage") Integer cpuPercentage,
        @SerializedName("Bucket") String bucket,
        @SerializedName("Path") String path,
        @SerializedName("usersOptions") RbacRestoreOption restoreUsers,
        @SerializedName("rolesOptions") RbacRestoreOption restoreRoles) {
    }

    public static BackupRestore of() {
      return of(ObjectBuilder.identity());
    }

    public static BackupRestore of(Function<Builder, ObjectBuilder<BackupRestore>> fn) {
      return fn.apply(new Builder()).build();
    }

    public BackupRestore(Builder builder) {
      this(
          builder.includeCollections,
          builder.excludeCollections,
          builder.overwriteAlias,
          new Config(
              builder.cpuPercentage,
              builder.bucket,
              builder.path,
              builder.restoreUsers,
              builder.restoreRoles));

    }

    public static class Builder implements ObjectBuilder<BackupRestore> {
      private Integer cpuPercentage;
      private String bucket;
      private String path;
      private Boolean overwriteAlias;
      private RbacRestoreOption restoreUsers;
      private RbacRestoreOption restoreRoles;
      private final List<String> includeCollections = new ArrayList<>();
      private final List<String> excludeCollections = new ArrayList<>();

      public Builder includeCollections(String... includeCollections) {
        return includeCollections(Arrays.asList(includeCollections));
      }

      public Builder includeCollections(List<String> includeCollections) {
        this.includeCollections.addAll(includeCollections);
        return this;
      }

      public Builder excludeCollections(String... excludeCollections) {
        return excludeCollections(Arrays.asList(excludeCollections));
      }

      public Builder excludeCollections(List<String> excludeCollections) {
        this.excludeCollections.addAll(excludeCollections);
        return this;
      }

      public Builder cpuPercentage(int cpuPercentage) {
        this.cpuPercentage = cpuPercentage;
        return this;
      }

      public Builder bucket(String bucket) {
        this.bucket = bucket;
        return this;
      }

      public Builder path(String path) {
        this.path = path;
        return this;
      }

      public Builder overwriteAlias(boolean overwriteAlias) {
        this.overwriteAlias = overwriteAlias;
        return this;
      }

      public Builder restoreUsers(RbacRestoreOption restoreUsers) {
        this.restoreUsers = restoreUsers;
        return this;
      }

      public Builder restoreRoles(RbacRestoreOption restoreRoles) {
        this.restoreRoles = restoreRoles;
        return this;
      }

      @Override
      public BackupRestore build() {
        return new BackupRestore(this);
      }
    }
  }
}
