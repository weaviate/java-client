package io.weaviate.client6.v1.api.rbac;

import java.util.Arrays;
import java.util.List;

import com.google.gson.annotations.SerializedName;

public record ClusterPermission(
    @SerializedName("actions") List<Action> actions) implements Permission {

  public ClusterPermission(Action... actions) {
    this(Arrays.asList(actions));
  }

  @Override
  public Permission.Kind _kind() {
    return Permission.Kind.CLUSTER;
  }

  @Override
  public Object self() {
    return this;
  }

  public enum Action implements RbacAction<Action> {
    @SerializedName("read_cluster")
    READ("read_cluster");

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
