package io.weaviate.client.v1.rbac.model;

import com.google.gson.annotations.SerializedName;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = true)
public class NodesPermission extends Permission<NodesPermission> {
  final String collection;
  final Verbosity verbosity;

  /** Create permission scoped to all collections. */
  public NodesPermission(Verbosity verbosity, Action action) {
    this("*", verbosity, action);
  }

  /**
   * Permission scoped to a collection with {@link Verbosity#VERBOSE}.
   */
  public NodesPermission(String collection, Action action) {
    this(collection, Verbosity.VERBOSE, action);
  }

  NodesPermission(Verbosity verbosity, String action) {
    this(verbosity, RbacAction.fromString(Action.class, action));
  }

  NodesPermission(String collection, Verbosity verbosity, String action) {
    this(collection, verbosity, RbacAction.fromString(Action.class, action));
  }

  NodesPermission(String collection, Verbosity verbosity, Action action) {
    super(action);
    this.collection = collection;
    this.verbosity = verbosity;
  }

  @AllArgsConstructor
  public enum Action implements RbacAction {
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
