package io.weaviate.client.v1.rbac.model;

import io.weaviate.client.v1.rbac.api.WeaviatePermission;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
public class BackupsPermission extends Permission<BackupsPermission> {
  final String collection;

  public BackupsPermission(Action action, String collection) {
    super(action);
    this.collection = collection;
  }

  BackupsPermission(String action, String collection) {
    this(CustomAction.fromString(Action.class, action), collection);
  }

  @Override
  public WeaviatePermission toWeaviate() {
    return new WeaviatePermission(this.action, this);
  }

  @AllArgsConstructor
  public enum Action implements CustomAction {
    MANAGE("manage_backups");

    @Getter
    private final String value;
  }
}
