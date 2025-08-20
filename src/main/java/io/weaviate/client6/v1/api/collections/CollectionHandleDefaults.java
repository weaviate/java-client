package io.weaviate.client6.v1.api.collections;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.google.common.util.concurrent.ListenableFuture;

import io.weaviate.client6.v1.api.collections.query.ConsistencyLevel;
import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.grpc.Rpc;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateGrpc.WeaviateBlockingStub;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateGrpc.WeaviateFutureStub;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoAggregate;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatchDelete;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoSearchGet;
import io.weaviate.client6.v1.internal.rest.Endpoint;
import io.weaviate.client6.v1.internal.rest.EndpointBase;
import io.weaviate.client6.v1.internal.rest.JsonEndpoint;

public record CollectionHandleDefaults(ConsistencyLevel consistencyLevel, String tenant) {
  private static final String CONSISTENCY_LEVEL = "consistency_level";
  private static final String TENANT = "tenant";

  /**
   * Set default values for query / aggregation requests.
   *
   * @return CollectionHandleDefaults derived from applying {@code fn} to
   *         {@link Builder}.
   */
  public static CollectionHandleDefaults of(Function<Builder, ObjectBuilder<CollectionHandleDefaults>> fn) {
    return fn.apply(new Builder()).build();
  }

  /**
   * Empty collection defaults.
   *
   * @return A tucked builder that does not leaves all defaults unset.
   */
  public static Function<Builder, ObjectBuilder<CollectionHandleDefaults>> none() {
    return ObjectBuilder.identity();
  }

  public CollectionHandleDefaults(Builder builder) {
    this(builder.consistencyLevel, builder.tenant);
  }

  public static final class Builder implements ObjectBuilder<CollectionHandleDefaults> {
    private ConsistencyLevel consistencyLevel;
    private String tenant;

    /** Set default consistency level for this collection handle. */
    public Builder consistencyLevel(ConsistencyLevel consistencyLevel) {
      this.consistencyLevel = consistencyLevel;
      return this;
    }

    /** Set default tenant for this collection handle. */
    public Builder tenant(String tenant) {
      this.tenant = tenant;
      return this;
    }

    @Override
    public CollectionHandleDefaults build() {
      return new CollectionHandleDefaults(this);
    }
  }

  public <RequestT, ResponseT> Endpoint<RequestT, ResponseT> endpoint(Endpoint<RequestT, ResponseT> ep,
      Function<EndpointBuilder<RequestT, ResponseT>, ObjectBuilder<Endpoint<RequestT, ResponseT>>> fn) {
    return fn.apply(new EndpointBuilder<>(ep)).build();
  }

  public <RequestT, RequestM, ResponseT, ReplyM> Rpc<RequestT, RequestM, ResponseT, ReplyM> rpc(
      Rpc<RequestT, RequestM, ResponseT, ReplyM> rpc) {
    return new ContextRpc<>(rpc);
  }

  /** Which part of the request a parameter should be added to. */
  public static enum Location {
    /** Query string. */
    QUERY,
    /**
     * Request body. {@code RequestT} must implement {@link WithDefaults} for the
     * changes to be applied.
     */
    BODY;
  }

  public static interface WithDefaults<SelfT extends WithDefaults<SelfT>> {
    ConsistencyLevel consistencyLevel();

    SelfT withConsistencyLevel(ConsistencyLevel consistencyLevel);

    String tenant();

    SelfT withTenant(String tenant);
  }

  private class ContextEndpoint<RequestT, ResponseT> extends EndpointBase<RequestT, ResponseT>
      implements JsonEndpoint<RequestT, ResponseT> {

    private final Location consistencyLevelLoc;
    private final Location tenantLoc;
    private final Endpoint<RequestT, ResponseT> endpoint;

    ContextEndpoint(EndpointBuilder<RequestT, ResponseT> builder) {
      super(builder.endpoint::method,
          builder.endpoint::requestUrl,
          builder.endpoint::queryParameters,
          builder.endpoint::body);
      this.consistencyLevelLoc = builder.consistencyLevelLoc;
      this.tenantLoc = builder.tenantLoc;
      this.endpoint = builder.endpoint;
    }

    /** Return consistencyLevel of the enclosing CollectionHandleDefaults object. */
    private ConsistencyLevel consistencyLevel() {
      return CollectionHandleDefaults.this.consistencyLevel;
    }

    @Override
    public Map<String, Object> queryParameters(RequestT request) {
      // Copy the map, as it's most likely unmodifiable.
      var query = new HashMap<>(super.queryParameters(request));
      if (consistencyLevel() != null && consistencyLevelLoc != null && consistencyLevelLoc == Location.QUERY) {
        query.putIfAbsent(CONSISTENCY_LEVEL, consistencyLevel());
      }
      if (tenant() != null && tenantLoc != null && tenantLoc == Location.QUERY) {
        query.putIfAbsent(TENANT, tenant());
      }
      return query;
    }

    @SuppressWarnings("unchecked")
    @Override
    public String body(RequestT request) {
      if (request instanceof WithDefaults wd) {
        if (wd.consistencyLevel() == null && consistencyLevel() != null) {
          wd = wd.withConsistencyLevel(consistencyLevel());
        }
        if (wd.tenant() == null && tenant() != null) {
          wd = wd.withTenant(tenant());
        }
        // This cast is safe as long as `wd` returns its own type,
        // which it does as per the interface contract.
        request = (RequestT) wd;
      }
      return super.body(request);
    }

    @Override
    public ResponseT deserializeResponse(int statusCode, String responseBody) {
      return EndpointBase.deserializeResponse(endpoint, statusCode, responseBody);
    }
  }

  /**
   * EndpointBuilder configures how CollectionHandleDefautls
   * are added to a REST request.
   */
  public class EndpointBuilder<RequestT, ResponseT> implements ObjectBuilder<Endpoint<RequestT, ResponseT>> {
    private final Endpoint<RequestT, ResponseT> endpoint;

    private Location consistencyLevelLoc;
    private Location tenantLoc;

    EndpointBuilder(Endpoint<RequestT, ResponseT> ep) {
      this.endpoint = ep;
    }

    /** Control which part of the request to add default consistency level to. */
    public EndpointBuilder<RequestT, ResponseT> consistencyLevel(Location loc) {
      this.consistencyLevelLoc = loc;
      return this;
    }

    /** Control which part of the request to add default consistency level to. */
    public EndpointBuilder<RequestT, ResponseT> tenant(Location loc) {
      this.tenantLoc = loc;
      return this;
    }

    @Override
    public Endpoint<RequestT, ResponseT> build() {
      return new ContextEndpoint<>(this);
    }
  }

  private class ContextRpc<RequestT, RequestM, ResponseT, ReplyM>
      implements Rpc<RequestT, RequestM, ResponseT, ReplyM> {

    private final Rpc<RequestT, RequestM, ResponseT, ReplyM> rpc;

    ContextRpc(Rpc<RequestT, RequestM, ResponseT, ReplyM> rpc) {
      this.rpc = rpc;
    }

    /** Return consistencyLevel of the enclosing CollectionHandleDefaults object. */
    private ConsistencyLevel consistencyLevel() {
      return CollectionHandleDefaults.this.consistencyLevel;
    }

    /** Return tenant of the enclosing CollectionHandleDefaults object. */
    private String tenant() {
      return CollectionHandleDefaults.this.tenant;
    }

    @SuppressWarnings("unchecked")
    @Override
    public RequestM marshal(RequestT request) {
      var message = rpc.marshal(request);
      if (message instanceof WeaviateProtoBatch.BatchObjectsRequest msg) {
        var b = msg.toBuilder();
        if (!msg.hasConsistencyLevel() && consistencyLevel() != null) {
          consistencyLevel().appendTo(b);
        }

        // Tenant must be applied to each batch object individually.
        msg.getObjectsList().stream()
            .map(obj -> {
              var objBuilder = obj.toBuilder();
              if (obj.getTenant().isEmpty() && tenant() != null) {
                objBuilder.setTenant(tenant());
              }
              return objBuilder.build();
            })
            .forEach(b::addObjects);
        return (RequestM) b.build();

      } else if (message instanceof WeaviateProtoBatchDelete.BatchDeleteRequest msg) {
        var b = msg.toBuilder();
        if (!msg.hasConsistencyLevel() && consistencyLevel() != null) {
          consistencyLevel().appendTo(b);
        }
        if (msg.getTenant().isEmpty() && tenant() != null) {
          b.setTenant(tenant());
        }
        return (RequestM) b.build();

      } else if (message instanceof WeaviateProtoSearchGet.SearchRequest msg) {
        var b = msg.toBuilder();
        if (!msg.hasConsistencyLevel() && consistencyLevel() != null) {
          consistencyLevel().appendTo(b);
        }
        if (msg.getTenant().isEmpty() && tenant() != null) {
          b.setTenant(tenant());
        }
        return (RequestM) b.build();

      } else if (message instanceof WeaviateProtoAggregate.AggregateRequest msg) {
        var b = msg.toBuilder();
        if (msg.getTenant().isEmpty() && tenant() != null) {
          b.setTenant(tenant());
        }
        return (RequestM) b.build();
      }

      return message;
    }

    @Override
    public ResponseT unmarshal(ReplyM reply) {
      return rpc.unmarshal(reply);
    }

    @Override
    public BiFunction<WeaviateBlockingStub, RequestM, ReplyM> method() {
      return rpc.method();
    }

    @Override
    public BiFunction<WeaviateFutureStub, RequestM, ListenableFuture<ReplyM>> methodAsync() {
      return rpc.methodAsync();
    }
  }
}
