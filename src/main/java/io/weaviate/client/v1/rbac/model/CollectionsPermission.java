package io.weaviate.client.v1.rbac.model;

import io.weaviate.client.v1.rbac.api.WeaviatePermission;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = true)
public class CollectionsPermission extends Permission<CollectionsPermission> {
  final String collection;
  final String tenant;

  public CollectionsPermission(String collection, Action action) {
    this(collection, "*", action);
  }

  CollectionsPermission(String collection, String action) {
    this(collection, RbacAction.fromString(Action.class, action));
  }

  private CollectionsPermission(String collection, String tenant, Action action) {
    super(action);
    this.collection = collection;
    this.tenant = tenant;
  }

  @Override
  public WeaviatePermission toWeaviate() {
    return new WeaviatePermission(this.action, this);
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
