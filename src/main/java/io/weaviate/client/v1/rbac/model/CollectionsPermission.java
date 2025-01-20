package io.weaviate.client.v1.rbac.model;

import io.weaviate.client.v1.rbac.api.WeaviatePermission;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
public class CollectionsPermission implements Permission<CollectionsPermission> {
  final transient String action;
  final String collection;
  final String tenant;

  public CollectionsPermission(Action action, String collection) {
    this(action, collection, "*");
  }

  CollectionsPermission(String action, String collection) {
    this(CustomAction.fromString(Action.class, action), collection);
  }

  private CollectionsPermission(Action action, String collection, String tenant) {
    this.action = action.getValue();
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
    DELETE("delete_collections"),
    MANAGE("manage_collections");

    @Getter
    private final String value;
  }
}
