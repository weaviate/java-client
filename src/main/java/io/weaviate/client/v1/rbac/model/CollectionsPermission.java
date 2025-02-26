package io.weaviate.client.v1.rbac.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = true)
public class CollectionsPermission extends Permission<CollectionsPermission> {
  final String collection;

  public CollectionsPermission(String collection, Action action) {
    super(action);
    this.collection = collection;
  }

  CollectionsPermission(String collection, String action) {
    this(collection, RbacAction.fromString(Action.class, action));
  }

  @AllArgsConstructor
  public enum Action implements RbacAction {
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
