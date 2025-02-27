package io.weaviate.client.v1.rbac.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = true)
public class DataPermission extends Permission<DataPermission> {
  final String collection;

  public DataPermission(String collection, Action... actions) {
    super(actions);
    this.collection = collection;
  }

  DataPermission(String collection, String action) {
    this(collection, RbacAction.fromString(Action.class, action));
  }

  @AllArgsConstructor
  public enum Action implements RbacAction {
    CREATE("create_data"),
    READ("read_data"),
    UPDATE("update_data"),
    DELETE("delete_data"),
    MANAGE("manage_data");

    @Getter
    private final String value;
  }
}
