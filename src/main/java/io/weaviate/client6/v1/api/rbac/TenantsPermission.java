package io.weaviate.client6.v1.api.rbac;

import java.util.Arrays;
import java.util.List;

import com.google.gson.annotations.SerializedName;

public record TenantsPermission(
    @SerializedName("collection") String collection,
    @SerializedName("tenant") String tenant,
    @SerializedName("actions") List<Action> actions) implements Permission {

  public TenantsPermission(String collection, String tenant, Action... actions) {
    this(collection, tenant, Arrays.asList(actions));
  }

  @Override
  public Permission.Kind _kind() {
    return Permission.Kind.TENANTS;
  }

  @Override
  public Object self() {
    return this;
  }

  public enum Action implements RbacAction<Action> {
    @SerializedName("create_tenants")
    CREATE("create_tenants"),
    @SerializedName("read_tenants")
    READ("read_tenants"),
    @SerializedName("update_tenants")
    UPDATE("update_tenants"),
    @SerializedName("delete_tenants")
    DELETE("delete_tenants");

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
