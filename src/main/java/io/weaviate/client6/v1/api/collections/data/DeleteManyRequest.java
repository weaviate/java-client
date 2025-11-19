package io.weaviate.client6.v1.api.collections.data;

import java.util.function.Function;

import io.weaviate.client6.v1.api.collections.CollectionHandleDefaults;
import io.weaviate.client6.v1.api.collections.query.Filter;
import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.grpc.ByteStringUtil;
import io.weaviate.client6.v1.internal.grpc.Rpc;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateGrpc.WeaviateBlockingStub;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateGrpc.WeaviateFutureStub;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBase;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatchDelete;
import io.weaviate.client6.v1.internal.orm.CollectionDescriptor;

public record DeleteManyRequest(Filter filters, Boolean verbose, Boolean dryRun) {

  public static Rpc<DeleteManyRequest, WeaviateProtoBatchDelete.BatchDeleteRequest, DeleteManyResponse, WeaviateProtoBatchDelete.BatchDeleteReply> rpc(
      CollectionDescriptor<?> collection,
      CollectionHandleDefaults defaults) {
    return Rpc.of(
        request -> {
          var message = WeaviateProtoBatchDelete.BatchDeleteRequest.newBuilder();
          message.setCollection(collection.collectionName());

          if (request.verbose != null) {
            message.setVerbose(request.verbose);
          }
          if (request.dryRun != null) {
            message.setDryRun(request.dryRun);
          }
          if (defaults.tenant() != null) {
            message.setTenant(defaults.tenant());
          }
          if (defaults.consistencyLevel() != null) {
            defaults.consistencyLevel().appendTo(message);
          }

          var filters = WeaviateProtoBase.Filters.newBuilder();
          request.filters.appendTo(filters);
          message.setFilters(filters);

          return message.build();
        },
        reply -> {
          var objects = reply.getObjectsList()
              .stream()
              .map(obj -> new DeleteManyResponse.DeletedObject(
                  ByteStringUtil.decodeUuid(obj.getUuid()).toString(),
                  obj.getSuccessful(),
                  obj.getError()))
              .toList();

          return new DeleteManyResponse(
              reply.getTook(),
              reply.getFailed(),
              reply.getMatches(),
              reply.getSuccessful(),
              objects);
        },
        () -> WeaviateBlockingStub::batchDelete,
        () -> WeaviateFutureStub::batchDelete);
  }

  public static DeleteManyRequest of(Filter filters) {
    return of(filters, ObjectBuilder.identity());
  }

  public DeleteManyRequest(Builder builder) {
    this(
        builder.filters,
        builder.verbose,
        builder.dryRun);
  }

  public static DeleteManyRequest of(Filter filters, Function<Builder, ObjectBuilder<DeleteManyRequest>> fn) {
    return fn.apply(new Builder(filters)).build();
  }

  public static class Builder implements ObjectBuilder<DeleteManyRequest> {
    // Required request parameters;
    private final Filter filters;

    private Boolean verbose;
    private Boolean dryRun;

    public Builder(Filter filters) {
      this.filters = filters;
    }

    public Builder verbose(boolean verbose) {
      this.verbose = verbose;
      return this;
    }

    public Builder dryRun(boolean dryRun) {
      this.dryRun = dryRun;
      return this;
    }

    @Override
    public DeleteManyRequest build() {
      return new DeleteManyRequest(this);
    }
  }
}
