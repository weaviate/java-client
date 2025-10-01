package io.weaviate.client6.v1.api.rbac;

import java.io.IOException;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.internal.Streams;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import io.weaviate.client6.v1.api.rbac.NodesPermission.Verbosity;
import io.weaviate.client6.v1.api.rbac.RolesPermission.Scope;
import io.weaviate.client6.v1.internal.json.JsonEnum;

public interface Permission {
  List<? extends RbacAction<?>> actions();

  enum Kind implements JsonEnum<Kind> {
    ALIASES("aliases"),
    BACKUPS("backups"),
    COLLECTIONS("collections"),
    DATA("data"),
    GROUPS("groups"),
    ROLES("roles"),
    NODES("nodes"),
    TENANTS("tenants"),
    REPLICATE("replicate"),
    USERS("users"),

    // Fake permission kinds: Weaviate does not use those.
    CLUSTER("cluster");

    private static final Map<String, Kind> jsonValueMap = JsonEnum.collectNames(Kind.values());
    private final String jsonValue;

    private Kind(String jsonValue) {
      this.jsonValue = jsonValue;
    }

    @Override
    public String jsonValue() {
      return jsonValue;
    }

    public static Kind valueOfJson(String jsonValue) {
      return JsonEnum.valueOfJson(jsonValue, jsonValueMap, Kind.class);
    }
  }

  Permission.Kind _kind();

  Object self();

  /**
   * Create {@link AliasPermission} for an alias.
   */
  public static AliasPermission alias(String alias, String collection, AliasPermission.Action... actions) {
    checkDeprecation(actions);
    return new AliasPermission(alias, collection, actions);
  }

  /**
   * Create {@link BackupsPermission} for a collection.
   */
  public static BackupsPermission backups(String collection, BackupsPermission.Action... actions) {
    checkDeprecation(actions);
    return new BackupsPermission(collection, actions);
  }

  /**
   * Create {@link ClusterPermission} permission.
   */
  public static ClusterPermission cluster(ClusterPermission.Action... actions) {
    checkDeprecation(actions);
    return new ClusterPermission(actions);
  }

  /**
   * Create permission for collection's configuration.
   */
  public static CollectionsPermission collections(String collection, CollectionsPermission.Action... actions) {
    checkDeprecation(actions);
    return new CollectionsPermission(collection, actions);
  }

  /**
   * Create permissions for managing collection's data.
   */
  public static DataPermission data(String collection, DataPermission.Action... actions) {
    checkDeprecation(actions);
    return new DataPermission(collection, actions);
  }

  /**
   * Create permissions for managing RBAC groups.
   */
  public static GroupsPermission groups(String groupId, String groupType, GroupsPermission.Action... actions) {
    checkDeprecation(actions);
    return new GroupsPermission(groupId, groupType, actions);
  }

  /**
   * Create {@link NodesPermission} scoped to all collections.
   */
  public static NodesPermission nodes(NodesPermission.Verbosity verbosity, NodesPermission.Action... actions) {
    checkDeprecation(actions);
    return new NodesPermission("*", verbosity, actions);
  }

  /**
   * Create {@link NodesPermission} scoped to a specific collection. Verbosity is
   * set to {@link Verbosity#VERBOSE} by default.
   */
  public static NodesPermission nodes(String collection, NodesPermission.Action... actions) {
    checkDeprecation(actions);
    return new NodesPermission(collection, Verbosity.VERBOSE, actions);
  }

  /**
   * Create {@link RolesPermission} for multiple actions.
   */
  public static RolesPermission roles(String roleName, Scope scope, RolesPermission.Action... actions) {
    checkDeprecation(actions);
    return new RolesPermission(roleName, scope, actions);
  }

  /**
   * Create {@link TenantsPermission} for a tenant.
   */
  public static TenantsPermission tenants(String collection, String tenant, TenantsPermission.Action... actions) {
    checkDeprecation(actions);
    return new TenantsPermission(collection, tenant, actions);
  }

  /**
   * Create {@link UsersPermission}.
   */
  public static UsersPermission users(String user, UsersPermission.Action... actions) {
    checkDeprecation(actions);
    return new UsersPermission(user, actions);
  }

  /**
   * Create {@link ReplicatePermission}.
   *
   * <p>
   * Example:
   * {@code Permissions.replicate("Pizza", "shard-123", ReplicatePermission.Action.CREATE)}
   */
  public static ReplicatePermission replicate(String collection, String shard, ReplicatePermission.Action... actions) {
    checkDeprecation(actions);
    return new ReplicatePermission(collection, shard, actions);
  }

  private static void checkDeprecation(RbacAction<?>... actions) throws IllegalArgumentException {
    for (var action : actions) {
      if (action.isDeprecated()) {
        throw new IllegalArgumentException(action.jsonValue()
            + " is hard-deprecated and should only be used to read legacy permissions created in v1.28");
      }
    }
  }

  @SuppressWarnings("unchecked")
  static List<Permission> merge(List<Permission> permissions) {
    record Key(
        /**
         * hash is computed on all permission fields apart from "actions"
         * which is what we need to aggregate.
         */
        int hash,
        /**
         * Permission types which do not have any filters differentiate by their class.
         */
        Class<?> cls) {
      private Key(Object object) {
        this(HashCodeBuilder.reflectionHashCode(object, "actions"), object.getClass());
      }
    }

    var result = new LinkedHashMap<Key, Permission>(); // preserve insertion order
    for (Permission perm : permissions) {
      var key = new Key(perm);
      var stored = result.putIfAbsent(key, perm);
      if (stored != null) { // A permission for this key already exists, add all actions.
        assert stored.actions() != null : "actions == null for " + stored.getClass();
        ((List<? super RbacAction<?>>) stored.actions()).addAll(perm.actions());
      }
    }
    return result.values().stream().collect(Collectors.toList());
  }

  public static enum CustomTypeAdapterFactory implements TypeAdapterFactory {
    INSTANCE;

    private static final EnumMap<Permission.Kind, TypeAdapter<? extends Permission>> readAdapters = new EnumMap<>(
        Permission.Kind.class);

    private final void addAdapter(Gson gson, Permission.Kind kind, Class<? extends Permission> cls) {
      readAdapters.put(kind, (TypeAdapter<? extends Permission>) gson.getDelegateAdapter(this, TypeToken.get(cls)));
    }

    private final void init(Gson gson) {
      addAdapter(gson, Permission.Kind.ALIASES, AliasPermission.class);
      addAdapter(gson, Permission.Kind.BACKUPS, BackupsPermission.class);
      addAdapter(gson, Permission.Kind.COLLECTIONS, CollectionsPermission.class);
      addAdapter(gson, Permission.Kind.DATA, DataPermission.class);
      addAdapter(gson, Permission.Kind.GROUPS, GroupsPermission.class);
      addAdapter(gson, Permission.Kind.ROLES, RolesPermission.class);
      addAdapter(gson, Permission.Kind.NODES, NodesPermission.class);
      addAdapter(gson, Permission.Kind.TENANTS, TenantsPermission.class);
      addAdapter(gson, Permission.Kind.REPLICATE, ReplicatePermission.class);
      addAdapter(gson, Permission.Kind.USERS, UsersPermission.class);
      addAdapter(gson, Permission.Kind.CLUSTER, ClusterPermission.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
      var rawType = type.getRawType();
      if (!Permission.class.isAssignableFrom(rawType)) {
        return null;
      }

      if (readAdapters.isEmpty()) {
        init(gson);
      }

      final var writeAdapter = gson.getDelegateAdapter(this, TypeToken.get(rawType));
      return (TypeAdapter<T>) new TypeAdapter<Permission>() {

        @Override
        public void write(JsonWriter out, Permission value) throws IOException {
          out.beginObject();

          if (!value.actions().isEmpty()) {
            // User might not have provided any actions by mistake
            var action = (RbacAction<?>) value.actions().get(0);
            out.name("action");
            out.value(action.jsonValue());
          }

          if (value.self() != null) {
            var permission = writeAdapter.toJsonTree((T) value.self());
            // Some permission types do not have a body
            permission.getAsJsonObject().remove("actions");
            out.name(value._kind().jsonValue());
            Streams.write(permission, out);
          }

          out.endObject();
        }

        @Override
        public Permission read(JsonReader in) throws IOException {
          var jsonObject = JsonParser.parseReader(in).getAsJsonObject();

          var actions = new JsonArray(1);
          var permission = new JsonObject();

          var action = jsonObject.remove("action");
          actions.add(action);

          Permission.Kind kind;
          if (!jsonObject.keySet().isEmpty()) {
            var kindString = jsonObject.keySet().iterator().next();
            kind = Permission.Kind.valueOfJson(kindString);
            permission = jsonObject.get(kindString).getAsJsonObject();
          } else {
            var actionString = action.getAsString();
            if (actionString.endsWith("_cluster")) {
              kind = Permission.Kind.CLUSTER;
            } else {
              throw new IllegalArgumentException("unknown RBAC action " + actionString);
            }
          }

          var readAdapter = readAdapters.get(kind);
          if (readAdapter == null) {
            return null;
          }

          permission.add("actions", actions);
          return readAdapter.fromJsonTree(permission);
        }
      }.nullSafe();
    }
  }
}
