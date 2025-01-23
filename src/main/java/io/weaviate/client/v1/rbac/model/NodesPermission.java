package io.weaviate.client.v1.rbac.model;

import com.google.gson.annotations.SerializedName;

import io.weaviate.client.v1.rbac.api.WeaviatePermission;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = true)
public class NodesPermission extends Permission<NodesPermission> {
  final String collection;
  final Verbosity verbosity;

  public NodesPermission(Action action, Verbosity verbosity) {
    this(action, verbosity, "*");
  }

  NodesPermission(String action, Verbosity verbosity) {
    this(CustomAction.fromString(Action.class, action), verbosity);
  }

  NodesPermission(String action, Verbosity verbosity, String collection) {
    this(CustomAction.fromString(Action.class, action), verbosity, collection);
  }

  public NodesPermission(Action action, Verbosity verbosity, String collection) {
    super(action);
    this.collection = collection;
    this.verbosity = verbosity;
  }

  @Override
  public WeaviatePermission toWeaviate() {
    return new WeaviatePermission(this.action, this);
  }

  @AllArgsConstructor
  public enum Action implements CustomAction {
    READ("read_nodes");

    @Getter
    private final String value;
  }

  @AllArgsConstructor
  public enum Verbosity {
    @SerializedName("minimal")
    MINIMAL,
    @SerializedName("verbose")
    VERBOSE;
  }

}
