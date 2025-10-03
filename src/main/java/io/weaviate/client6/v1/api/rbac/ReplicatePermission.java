package io.weaviate.client6.v1.api.rbac;

import java.util.Arrays;
import java.util.List;

import com.google.gson.annotations.SerializedName;

public record ReplicatePermission(
    @SerializedName("collection") String collection,
    @SerializedName("shard") String shard,
    @SerializedName("actions") List<Action> actions) implements Permission {

  public ReplicatePermission(String collection, String shard, Action... actions) {
    this(collection, shard, Arrays.asList(actions));
  }

  @Override
  public Permission.Kind _kind() {
    return Permission.Kind.REPLICATE;
  }

  @Override
  public Object self() {
    return this;
  }

  public enum Action implements RbacAction<Action> {
    @SerializedName("create_replicate")
    CREATE("create_replicate"),
    @SerializedName("read_replicate")
    READ("read_replicate"),
    @SerializedName("update_replicate")
    UPDATE("update_replicate"),
    @SerializedName("delete_replicate")
    DELETE("delete_replicate");

    private final String jsonValue;

    private Action(String jsonValue) {
      this.jsonValue = jsonValue;
    }

    @Override
    public String jsonValue() {
      return jsonValue;
    }
  }
}
