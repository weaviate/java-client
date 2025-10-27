package io.weaviate.client6.v1.api.collections.generate;

import io.weaviate.client6.v1.api.collections.CollectionHandleDefaults;
import io.weaviate.client6.v1.api.collections.query.GroupBy;
import io.weaviate.client6.v1.api.collections.query.QueryOperator;
import io.weaviate.client6.v1.api.collections.query.QueryRequest;
import io.weaviate.client6.v1.internal.grpc.Rpc;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateGrpc.WeaviateBlockingStub;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateGrpc.WeaviateFutureStub;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoGenerative;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoSearchGet;
import io.weaviate.client6.v1.internal.orm.CollectionDescriptor;

public record GenerativeRequest(QueryOperator operator, GenerativeTask generate, GroupBy groupBy) {
  static <PropertiesT> Rpc<GenerativeRequest, WeaviateProtoSearchGet.SearchRequest, GenerativeResponse<PropertiesT>, WeaviateProtoSearchGet.SearchReply> rpc(
      CollectionDescriptor<PropertiesT> collection,
      CollectionHandleDefaults defaults) {
    return Rpc.of(
        request -> {
          var query = QueryRequest.marshal(
              new QueryRequest(request.operator, request.groupBy),
              collection, defaults);
          var generative = WeaviateProtoGenerative.GenerativeSearch.newBuilder();
          request.generate.appendTo(generative);
          var builder = query.toBuilder();
          builder.setGenerative(generative);
          return builder.build();
        },
        reply -> GenerativeResponse.unmarshal(reply, collection),
        () -> WeaviateBlockingStub::search,
        () -> WeaviateFutureStub::search);
  }

  static <PropertiesT> Rpc<GenerativeRequest, WeaviateProtoSearchGet.SearchRequest, GenerativeResponseGrouped<PropertiesT>, WeaviateProtoSearchGet.SearchReply> grouped(
      CollectionDescriptor<PropertiesT> collection,
      CollectionHandleDefaults defaults) {
    var rpc = rpc(collection, defaults);
    return Rpc.of(
        request -> rpc.marshal(request),
        reply -> GenerativeResponseGrouped.unmarshal(reply, collection, defaults),
        () -> rpc.method(), () -> rpc.methodAsync());
  }
}
