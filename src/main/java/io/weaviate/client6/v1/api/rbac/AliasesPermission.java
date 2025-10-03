package io.weaviate.client6.v1.api.rbac;

import java.util.Arrays;
import java.util.List;

import com.google.gson.annotations.SerializedName;

public record AliasesPermission(
    @SerializedName("alias") String alias,
    @SerializedName("collection") String collection,
    @SerializedName("actions") List<Action> actions) implements Permission {

  public AliasesPermission(String alias, String collection, Action... actions) {
    this(alias, collection, Arrays.asList(actions));
  }

  @Override
  public Permission.Kind _kind() {
    return Permission.Kind.ALIASES;
  }

  @Override
  public Object self() {
    return this;
  }

  public enum Action implements RbacAction<Action> {
    @SerializedName("create_aliases")
    CREATE("create_aliases"),
    @SerializedName("read_aliases")
    READ("read_aliases"),
    @SerializedName("update_aliases")
    UPDATE("update_aliases"),
    @SerializedName("delete_aliases")
    DELETE("delete_aliases");

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
