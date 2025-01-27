package io.weaviate.client.v1.rbac.model;

import io.weaviate.client.v1.rbac.api.WeaviatePermission;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = true)
public class DataPermission extends Permission<DataPermission> {
  final String collection;
  final String object;
  final String tenant;

  public DataPermission(String collection, Action action) {
    this(collection, "*", "*", action);
  }

  DataPermission(String collection, String action) {
    this(collection, CustomAction.fromString(Action.class, action));
  }

  private DataPermission(String collection, String object, String tenant, Action action) {
    super(action);
    this.collection = collection;
    this.object = object;
    this.tenant = tenant;
  }

  @Override
  public WeaviatePermission toWeaviate() {
    return new WeaviatePermission(this.action, this);
  }

  @AllArgsConstructor
  public enum Action implements CustomAction {
    CREATE("create_data"),
    READ("read_data"),
    UPDATE("update_data"),
    DELETE("delete_data"),
    MANAGE("manage_data");

    @Getter
    private final String value;
  }
}
