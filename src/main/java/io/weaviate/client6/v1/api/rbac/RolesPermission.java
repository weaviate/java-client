package io.weaviate.client6.v1.api.rbac;

import java.util.Arrays;
import java.util.List;

import com.google.gson.annotations.SerializedName;

public record RolesPermission(
    @SerializedName("role") String role,
    @SerializedName("scope") Scope scope,
    @SerializedName("actions") List<Action> actions) implements Permission {

  public RolesPermission(String roleName, Scope scope, Action... actions) {
    this(roleName, scope, Arrays.asList(actions));
  }

  @Override
  public Permission.Kind _kind() {
    return Permission.Kind.ROLES;
  }

  @Override
  public Object self() {
    return this;
  }

  public enum Action implements RbacAction<Action> {
    @SerializedName("create_roles")
    CREATE("create_roles"),
    @SerializedName("read_roles")
    READ("read_roles"),
    @SerializedName("update_roles")
    UPDATE("update_roles"),
    @SerializedName("delete_roles")
    DELETE("delete_roles"),

    /*
     * DO NOT CREATE NEW PERMISSIONS WITH THIS ACTION.
     * It is preserved for backward compatibility with 1.28
     * and should only be used internally to read legacy permissions.
     */
    @SerializedName("manage_roles")
    @Deprecated
    MANAGE("manage_roles") {
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

  public enum Scope {
    @SerializedName("all")
    ALL,
    @SerializedName("match")
    MATCH;
  }
}
