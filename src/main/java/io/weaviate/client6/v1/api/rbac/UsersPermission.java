package io.weaviate.client6.v1.api.rbac;

import java.util.Arrays;
import java.util.List;

import com.google.gson.annotations.SerializedName;

public record UsersPermission(
    @SerializedName("users") String userId,
    @SerializedName("actions") List<Action> actions) implements Permission {

  public UsersPermission(String userId, Action... actions) {
    this(userId, Arrays.asList(actions));
  }

  @Override
  public Permission.Kind _kind() {
    return Permission.Kind.USERS;
  }

  @Override
  public Object self() {
    return this;
  }

  public enum Action implements RbacAction<Action> {
    @SerializedName("create_users")
    CREATE("create_users"),
    @SerializedName("update_users")
    UPDATE("update_users"),
    @SerializedName("read_users")
    READ("read_users"),
    @SerializedName("delete_users")
    DELETE("delete_users"),
    @SerializedName("assign_and_revoke_users")
    ASSIGN_AND_REVOKE("assign_and_revoke_users");

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
