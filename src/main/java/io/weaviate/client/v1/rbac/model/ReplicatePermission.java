package io.weaviate.client.v1.rbac.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = true)
public class ReplicatePermission extends Permission<ReplicatePermission> {
  final String collection;
  final String shard;

  public final String getShard() {
    return shard != null ? shard : "*";
  }

  public ReplicatePermission(String collection, String shard, Action... actions) {
    super(actions);
    this.collection = collection;
    this.shard = shard;
  }

  ReplicatePermission(String collection, String shard, String action) {
    this(collection, shard, RbacAction.fromString(Action.class, action));
  }

  @AllArgsConstructor
  public enum Action implements RbacAction {
    CREATE("create_replicate"),
    READ("read_replicate"),
    UPDATE("update_replicate"),
    DELETE("delete_replicate");

    @Getter
    private final String value;
  }
}
