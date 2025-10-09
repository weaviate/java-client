package io.weaviate.client6.v1.api.rbac;

import java.util.Arrays;
import java.util.List;

import com.google.gson.annotations.SerializedName;

public record NodesPermission(
    @SerializedName("collection") String collection,
    @SerializedName("verbosity") Verbosity verbosity,
    @SerializedName("actions") List<Action> actions) implements Permission {

  public NodesPermission(String collection, Verbosity verbosity, Action... actions) {
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

  public enum Verbosity {
    @SerializedName("minimal")
    MINIMAL,
    @SerializedName("verbose")
    VERBOSE;
  }
}
