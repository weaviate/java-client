package io.weaviate.client.v1.rbac.model;

import io.weaviate.client.v1.rbac.api.WeaviatePermission;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
public class NodesPermission implements Permission<NodesPermission> {
  final transient String action;
  final String collection;
  final Verbosity verbosity;

  public NodesPermission(Action action, Verbosity verbosity) {
    this(action, verbosity, "*");
  }

  public NodesPermission(Action action, Verbosity verbosity, String collection) {
    this.action = action.getValue();
    this.collection = collection;
    this.verbosity = verbosity;
  }

  @Override
  public WeaviatePermission toWeaviate() {
    return new WeaviatePermission(this.action, this);
  }

  @Override
  public NodesPermission fromWeaviate(WeaviatePermission perm) {
    return null;
  }

  @AllArgsConstructor
  public enum Action {
    READ("read_nodes");

    @Getter
    private final String value;
  }

  @AllArgsConstructor
  public enum Verbosity {
    MINIMAL("minimal"),
    VERBOSE("verbose");

    @Getter
    private final String value;
  }

}
