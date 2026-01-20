package io.weaviate.client6.v1.api.rbac;

import java.util.Arrays;
import java.util.List;

import com.google.gson.annotations.SerializedName;

public record CollectionsPermission(
    @SerializedName("collection") String collection,
    @SerializedName("actions") List<Action> actions) implements Permission {

  public CollectionsPermission(String collection, Action... actions) {
    this(collection, Arrays.asList(actions));
  }

  @Override
  public Permission.Kind _kind() {
    return Permission.Kind.COLLECTIONS;
  }

  @Override
  public Object self() {
    return this;
  }

  public enum Action implements RbacAction<Action> {
    @SerializedName("create_collections")
    CREATE("create_collections"),
    @SerializedName("read_collections")
    READ("read_collections"),
    @SerializedName("update_collections")
    UPDATE("update_collections"),
    @SerializedName("delete_collections")
    DELETE("delete_collections");

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
