package io.weaviate.client.v1.rbac.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import io.weaviate.client.v1.rbac.api.WeaviatePermission;
import io.weaviate.client.v1.rbac.model.NodesPermission.Verbosity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public abstract class Permission<P extends Permission<P>> {
  @Getter
  final transient List<String> actions = new ArrayList<>();

  Permission(RbacAction... actions) {
    this.actions.addAll(
        Arrays.stream(actions)
            .map(RbacAction::getValue)
            .collect(Collectors.toList()));
  }

  /** Convert the permission to a list of {@link WeaviatePermission}. */
  public WeaviatePermission firstToWeaviate() {
    if (actions.isEmpty()) {
      return null;
    }
    return this.toWeaviate(actions.get(0));
  };

  public List<WeaviatePermission> toWeaviate() {
    return this.actions.stream().map(this::toWeaviate).collect(Collectors.toList());
  }

  private WeaviatePermission toWeaviate(String action) {
    return new WeaviatePermission(action, this);
  }

  /**
   * Convert {@link WeaviatePermission} to concrete {@link Permission}.
   */
  public static Permission<?> fromWeaviate(WeaviatePermission perm) {
    String action = perm.getAction();
    if (perm.getBackups() != null) {
      return new BackupsPermission(perm.getBackups().getCollection(), action);
    } else if (perm.getCollections() != null) {
      return new CollectionsPermission(perm.getCollections().getCollection(), action);
    } else if (perm.getData() != null) {
      return new DataPermission(perm.getData().getCollection(), action);
    } else if (perm.getNodes() != null) {
      NodesPermission nodes = perm.getNodes();
      if (nodes.getCollection() != null) {
        return new NodesPermission(nodes.getCollection(), nodes.getVerbosity(), action);
      }
      return new NodesPermission(nodes.getVerbosity(), action);
    } else if (perm.getRoles() != null) {
      RolesPermission roles = perm.getRoles();
      return new RolesPermission(roles.getRole(), roles.getScope(), action);
    } else if (perm.getTenants() != null) {
      return new TenantsPermission(action);
    } else if (RbacAction.isValid(ClusterPermission.Action.class, action)) {
      return new ClusterPermission(action);
    } else if (RbacAction.isValid(UsersPermission.Action.class, action)) {
      return new UsersPermission(action);
    }
    return null;
  }

  public static final List<Permission<?>> merge(List<Permission<?>> permissions) {
    @RequiredArgsConstructor
    @EqualsAndHashCode
    class Key {
      final int hash;
      final Class<?> cls;
    }

    Map<Key, Permission<?>> result = new LinkedHashMap<>(); // preserve insertion order
    for (Permission<?> perm : permissions) {
      int hash = HashCodeBuilder.reflectionHashCode(perm, "actions");
      Key key = new Key(hash, perm.getClass());
      Permission<?> stored = result.putIfAbsent(key, perm);
      if (stored != null) {
        stored.actions.addAll(perm.actions);
      }
    }
    return result.values().stream().<Permission<?>>map(perm -> {
      Set<String> actions = new HashSet<>(perm.actions);
      perm.actions.clear();
      perm.actions.addAll(actions);
      return perm;
    }).collect(Collectors.toList());
  }

  /**
   * Create {@link BackupsPermission} for a collection.
   * <p>
   * Example:
   * {@code Permission.backups(BackupsPermission.Action.MANAGE, "Pizza") }
   */
  public static BackupsPermission backups(String collection, BackupsPermission.Action... actions) {
    checkDeprecation(actions);
    return new BackupsPermission(collection, actions);
  }

  /**
   * Create {@link ClusterPermission} permission.
   * <p>
   * Example: {@code Permission.cluster(ClusterPermission.Action.READ, "Pizza") }
   */
  public static ClusterPermission cluster(ClusterPermission.Action... actions) {
    checkDeprecation(actions);
    return new ClusterPermission(actions);
  }

  /**
   * Create permission for collection's configuration.
   * <p>
   * Example:
   * {@code Permission.collections("Pizza", CollectionsPermission.Action.READ, CollectionsPermission.Action.UPDATE) }
   */
  public static CollectionsPermission collections(String collection, CollectionsPermission.Action... actions) {
    checkDeprecation(actions);
    return new CollectionsPermission(collection, actions);
  }

  /**
   * Create permissions for multiple actions for managing collection's
   * data.
   * <p>
   * Example:
   * {@code Permission.data("Pizza", DataPermission.Action.READ, DataPermission.Action.UPDATE) }
   */
  public static DataPermission data(String collection, DataPermission.Action... actions) {
    checkDeprecation(actions);
    return new DataPermission(collection, actions);
  }

  /**
   * Create {@link NodesPermission} scoped to all collections.
   * <p>
   * Example:
   * {@code Permission.nodes(NodesPermission.Verbosity.MINIMAL, NodesPermission.Action.READ) }
   */
  public static NodesPermission nodes(NodesPermission.Verbosity verbosity, NodesPermission.Action... actions) {
    checkDeprecation(actions);
    return new NodesPermission(verbosity, actions);
  }

  /**
   * Create {@link NodesPermission} scoped to a specific collection. Verbosity is
   * set to {@link Verbosity#VERBOSE} by default.
   * <p>
   * Example:
   * {@code Permission.nodes("Pizza", NodesPermission.Action.READ) }
   */
  public static NodesPermission nodes(String collection, NodesPermission.Action... actions) {
    checkDeprecation(actions);
    return new NodesPermission(collection, actions);
  }

  /**
   * Create {@link RolesPermission} for multiple actions.
   * <p>
   * Example:
   * {@code Permission.roles("MyRole", RolesPermission.Action.READ, RolesPermission.Action.UPDATE) }
   */
  public static RolesPermission roles(String role, RolesPermission.Action... actions) {
    checkDeprecation(actions);
    return new RolesPermission(role, actions);
  }

  /**
   * Create {@link TenantsPermission} for a tenant.
   * <p>
   * Example:
   * {@code Permission.tenants(TenantsPermission.Action.READ) }
   */
  public static TenantsPermission tenants(TenantsPermission.Action... actions) {
    checkDeprecation(actions);
    return new TenantsPermission(actions);
  }

  /**
   * Create {@link UsersPermission}.
   * <p>
   * Example:
   * {@code Permission.users(UsersPermission.Action.READ) }
   */
  public static UsersPermission users(UsersPermission.Action... actions) {
    checkDeprecation(actions);
    return new UsersPermission(actions);
  }

  private static void checkDeprecation(RbacAction... actions) throws IllegalArgumentException {
    for (RbacAction action : actions) {
      if (action.isDeprecated()) {
        throw new IllegalArgumentException(action.getValue()
            + " is hard-deprecated and should only be used to read legacy permissions created in v1.28");
      }
    }
  }
}
