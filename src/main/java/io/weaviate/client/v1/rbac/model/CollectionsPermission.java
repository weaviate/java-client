package io.weaviate.client.v1.rbac.model;

import io.weaviate.client.v1.rbac.api.WeaviatePermission;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
public class CollectionsPermission extends Permission<CollectionsPermission> {
  final String collection;
  final String tenant;

  public CollectionsPermission(Action action, String collection) {
    this(action, collection, "*");
  }

  CollectionsPermission(String action, String collection) {
    this(CustomAction.fromString(Action.class, action), collection);
  }

  private CollectionsPermission(Action action, String collection, String tenant) {
    super(action);
    this.collection = collection;
    this.tenant = tenant;
  }

  @Override
  public WeaviatePermission toWeaviate() {
    return new WeaviatePermission(this.action, this);
  }

  @AllArgsConstructor
  public enum Action implements CustomAction {
    CREATE("create_collections"),
    READ("read_collections"),
    UPDATE("update_collections"),
    DELETE("delete_collections");

    // Not part of the public API yet.
    // MANAGE("manage_collections");

    @Getter
    private final String value;
  }
}
