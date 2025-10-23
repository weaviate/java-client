package io.weaviate.client6.v1.api.rbac;

import java.util.Arrays;
import java.util.List;

import com.google.gson.annotations.SerializedName;

import io.weaviate.client6.v1.api.cluster.NodeVerbosity;

public record NodesPermission(
    @SerializedName("collection") String collection,
    @SerializedName("verbosity") NodeVerbosity verbosity,
    @SerializedName("actions") List<Action> actions) implements Permission {

  public NodesPermission(String collection, NodeVerbosity verbosity, Action... actions) {
    this(collection, verbosity, Arrays.asList(actions));
  }

  @Override
  public Permission.Kind _kind() {
    return Permission.Kind.NODES;
  }

  @Override
  public Object self() {
    return this;
  }

  public enum Action implements RbacAction<Action> {
    @SerializedName("read_nodes")
    READ("read_nodes");

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
