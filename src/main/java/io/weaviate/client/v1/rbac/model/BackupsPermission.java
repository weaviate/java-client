package io.weaviate.client.v1.rbac.model;

import io.weaviate.client.v1.rbac.api.WeaviatePermission;
import lombok.AllArgsConstructor;
import lombok.Getter;

public class BackupsPermission implements Permission<BackupsPermission> {
  final transient String action;
  final String collection;

  public BackupsPermission(Action action, String collection) {
    this.action = action.getValue();
    this.collection = collection;
  }

  @Override
  public WeaviatePermission toWeaviate() {
    return new WeaviatePermission(this.action, this);
  }

  @Override
  public BackupsPermission fromWeaviate(WeaviatePermission perm) {
    return null;
  }

  @AllArgsConstructor
  public enum Action {
    MANAGE("manage_backups");

    @Getter
    private final String value;
  }
}
