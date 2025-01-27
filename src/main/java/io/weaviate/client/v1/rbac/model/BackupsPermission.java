package io.weaviate.client.v1.rbac.model;

import io.weaviate.client.v1.rbac.api.WeaviatePermission;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = true)
public class BackupsPermission extends Permission<BackupsPermission> {
  final String collection;

  public BackupsPermission(String collection, Action action) {
    super(action);
    this.collection = collection;
  }

  BackupsPermission(String collection, String action) {
    this(collection, CustomAction.fromString(Action.class, action));
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
