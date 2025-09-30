package io.weaviate.client6.v1.api.rbac;

import java.util.Arrays;
import java.util.List;

import com.google.gson.annotations.SerializedName;

public record BackupsPermission(
    @SerializedName("collection") String collection,
    @SerializedName("actions") List<Action> actions) implements Permission {

  public BackupsPermission(String collection, Action... actions) {
    this(collection, Arrays.asList(actions));
  }

  @Override
  public Permission.Kind _kind() {
    return Permission.Kind.BACKUPS;
  }

  @Override
  public Object self() {
    return this;
  }

  public enum Action implements RbacAction<Action> {
    @SerializedName("manage_backups")
    MANAGE("manage_backups");

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
