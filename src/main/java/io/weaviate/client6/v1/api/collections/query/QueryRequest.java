package io.weaviate.client6.v1.api.collections.query;

import io.weaviate.client6.v1.api.collections.CollectionHandleDefaults;
import io.weaviate.client6.v1.internal.grpc.Rpc;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateGrpc.WeaviateBlockingStub;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateGrpc.WeaviateFutureStub;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoSearchGet;
import io.weaviate.client6.v1.internal.orm.CollectionDescriptor;

public record QueryRequest(QueryOperator operator, GroupBy groupBy) {

  static <PropertiesT> Rpc<QueryRequest, WeaviateProtoSearchGet.SearchRequest, QueryResponse<PropertiesT>, WeaviateProtoSearchGet.SearchReply> rpc(
      CollectionDescriptor<PropertiesT> collection,
      CollectionHandleDefaults defaults) {
    return Rpc.of(
        request -> QueryRequest.marshal(request, collection, defaults),
        reply -> QueryResponse.unmarshal(reply, collection),
        () -> WeaviateBlockingStub::search,
        () -> WeaviateFutureStub::search);
  }

  public static <PropertiesT> WeaviateProtoSearchGet.SearchRequest marshal(
      QueryRequest request,
      CollectionDescriptor<PropertiesT> collection,
      CollectionHandleDefaults defaults) {
    var message = WeaviateProtoSearchGet.SearchRequest.newBuilder();
    message.setUses127Api(true);
    message.setUses125Api(true);
    message.setUses123Api(true);
    message.setCollection(collection.collectionName());

    if (request.operator.common() != null) {
      request.operator.common().appendTo(message);
    }
    if (request.operator.rerank() != null) {
      request.operator.rerank().appendTo(message);
    }
    request.operator.appendTo(message);

    if (defaults.tenant() != null) {
      message.setTenant(defaults.tenant());
    }
    if (defaults.consistencyLevel() != null) {
      defaults.consistencyLevel().appendTo(message);
    }

    if (request.groupBy != null) {
      request.groupBy.appendTo(message);
    }

    return message.build();
  }

  static <PropertiesT> Rpc<QueryRequest, WeaviateProtoSearchGet.SearchRequest, QueryResponseGrouped<PropertiesT>, WeaviateProtoSearchGet.SearchReply> grouped(
      CollectionDescriptor<PropertiesT> collection,
      CollectionHandleDefaults defaults) {
    var rpc = rpc(collection, defaults);
    return Rpc.of(
        request -> rpc.marshal(request),
        reply -> QueryResponseGrouped.unmarshal(reply, collection, defaults),
        () -> rpc.method(), () -> rpc.methodAsync());
  }

}
