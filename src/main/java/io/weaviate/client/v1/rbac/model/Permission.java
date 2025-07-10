package io.weaviate.client.v1.rbac.model;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import io.weaviate.client.v1.async.rbac.api.PermissionChecker;
import io.weaviate.client.v1.rbac.api.WeaviatePermission;
import io.weaviate.client.v1.rbac.model.NodesPermission.Verbosity;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public abstract class Permission<P extends Permission<P>> {
  /**
   * Actions allowed by this permission. Transience allows easily
   * serializing "action" separately from other attributes in the
   * extending permission types.
   *
   * LinkedHashSet preserves insertion order for predictability.
   */
  final transient Set<String> actions = new LinkedHashSet<>();

  public List<String> getActions() {
    return actions.stream().collect(Collectors.toList());
  }

  Permission(RbacAction... actions) {
    this.actions.addAll(
        Arrays.stream(actions)
            .map(RbacAction::getValue)
            .collect(Collectors.toList()));
  }

  /**
   * Create {@link WeaviatePermission} with the first action in the actions list.
   *
   * This is meant to be used with {@link PermissionChecker}, which can only
   * include a permission with a single action in the request.
   */
  public WeaviatePermission firstToWeaviate() {
    if (actions.isEmpty()) {
      return null;
    }
    return this.toWeaviate(actions.iterator().next());
  };

  /** Convert the permission to a list of {@link WeaviatePermission}. */
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
    if (perm.getAliases() != null) {
      return new AliasesPermission(perm.getAliases().getAlias(), action);
    } else if (perm.getBackups() != null) {
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

  /**
   * Merge permissions by their type and targeted resource. Weaviate server
   * returns separate entries for each action, but working with a
   * permission-per-resource model is more convenient.
   *
   * <p>
   * Example: convert Data[read_data, MyCollection], Data[delete_data,
   * MyCollection] to Data[[read_data, delete_data], MyCollection].
   */
  public static final List<Permission<?>> merge(List<Permission<?>> permissions) {
    @EqualsAndHashCode
    class Key {
      // hash is computed on all permission fields apart from "actions" which
      // is what we need to aggregate.
      final int hash;
      // Permission types which do not have any filters differentiate by their class.
      final Class<?> cls;

      private Key(Object object) {
        this.hash = HashCodeBuilder.reflectionHashCode(object, "actions");
        this.cls = object.getClass();
      }
    }

    Map<Key, Permission<?>> result = new LinkedHashMap<>(); // preserve insertion order
    for (Permission<?> perm : permissions) {
      Key key = new Key(perm);
      Permission<?> stored = result.putIfAbsent(key, perm);
      if (stored != null) { // A permission for this key already exists, add all actions.
        stored.actions.addAll(perm.actions);
      }
    }
    return result.values().stream().collect(Collectors.toList());
  }

  /**
   * Create {@link AliasesPermission} for a alias.
   * <p>
   * Example:
   * {@code Permission.aliases("PizzaAlias", AliasPermission.Action.CREATE) }
   */
  public static AliasesPermission aliases(String alias, AliasesPermission.Action... actions) {
    checkDeprecation(actions);
    return new AliasesPermission(alias, actions);
  }

  /**
   * Create {@link BackupsPermission} for a collection.
   * <p>
   * Example:
   * {@code Permission.backups("Pizza", BackupsPermission.Action.MANAGE) }
   */
  public static BackupsPermission backups(String collection, BackupsPermission.Action... actions) {
    checkDeprecation(actions);
    return new BackupsPermission(collection, actions);
  }

  /**
   * Create {@link ClusterPermission} permission.
   * <p>
   * Example: {@code Permission.cluster(ClusterPermission.Action.READ) }
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
