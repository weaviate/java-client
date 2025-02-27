package io.weaviate.client.v1.rbac.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = true)
public class BackupsPermission extends Permission<BackupsPermission> {
  final String collection;

  public BackupsPermission(String collection, Action... actions) {
    super(actions);
    this.collection = collection;
  }

  BackupsPermission(String collection, String action) {
    this(collection, RbacAction.fromString(Action.class, action));
  }

  @AllArgsConstructor
  public enum Action implements RbacAction {
    MANAGE("manage_backups");

    @Getter
    private final String value;
  }
}
