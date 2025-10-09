package io.weaviate.client6.v1.api.rbac;

import java.util.Arrays;
import java.util.List;

import com.google.gson.annotations.SerializedName;

public record DataPermission(
    @SerializedName("collection") String collection,
    @SerializedName("actions") List<Action> actions) implements Permission {

  public DataPermission(String collection, Action... actions) {
    this(collection, Arrays.asList(actions));
  }

  @Override
  public Permission.Kind _kind() {
    return Permission.Kind.DATA;
  }

  @Override
  public Object self() {
    return this;
  }

  public enum Action implements RbacAction<Action> {
    @SerializedName("create_data")
    CREATE("create_data"),
    @SerializedName("read_data")
    READ("read_data"),
    @SerializedName("update_data")
    UPDATE("update_data"),
    @SerializedName("delete_data")
    DELETE("delete_data"),
    /*
     * DO NOT CREATE NEW PERMISSIONS WITH THIS ACTION.
     * It is preserved for backward compatibility with 1.28
     * and should only be used internally to read legacy permissions.
     */
    @SerializedName("manage_data")
    @Deprecated
    MANAGE("manage_data") {
      @Override
      public boolean isDeprecated() {
        return true;
      };
    };

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
