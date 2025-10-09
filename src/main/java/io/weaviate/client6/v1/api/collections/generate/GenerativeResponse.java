package io.weaviate.client6.v1.api.collections.generate;

import java.util.List;

import io.weaviate.client6.v1.api.collections.generative.DummyGenerative;
import io.weaviate.client6.v1.api.collections.query.QueryResponse;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoGenerative;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoSearchGet;
import io.weaviate.client6.v1.internal.orm.CollectionDescriptor;

public record GenerativeResponse<PropertiesT>(
    float took,
    List<GenerativeObject<PropertiesT>> objects,
    TaskOutput generated) {
  static <PropertiesT> GenerativeResponse<PropertiesT> unmarshal(
      WeaviateProtoSearchGet.SearchReply reply,
      CollectionDescriptor<PropertiesT> collection) {
    var objects = reply
        .getResultsList()
        .stream()
        .map(result -> {
          var object = QueryResponse.unmarshalResultObject(
              result.getProperties(), result.getMetadata(), collection);
          TaskOutput generative = null;
          if (result.hasGenerative()) {
            generative = GenerativeResponse.unmarshalTaskOutput(result.getGenerative());
          }
          return new GenerativeObject<>(
              object.properties(),
              object.metadata(),
              generative);
        })
        .toList();

    TaskOutput summary = null;
    if (reply.hasGenerativeGroupedResults()) {
      summary = GenerativeResponse.unmarshalTaskOutput(reply.getGenerativeGroupedResults());
    }
    return new GenerativeResponse<>(reply.getTook(), objects, summary);
  }

  static TaskOutput unmarshalTaskOutput(List<WeaviateProtoGenerative.GenerativeReply> values) {
    if (values.isEmpty()) {
      return null;
    }
    var generated = values.get(0);

    var metadata = generated.getMetadata();
    ProviderMetadata providerMetadata = null;
    if (metadata.hasDummy()) {
      providerMetadata = new DummyGenerative.Metadata();
    }

    GenerativeDebug debug = null;
    if (generated.getDebug() != null && generated.getDebug().getFullPrompt() != null) {
      debug = new GenerativeDebug(generated.getDebug().getFullPrompt());
    }
    return new TaskOutput(generated.getResult(), providerMetadata, debug);
  }

  static TaskOutput unmarshalTaskOutput(WeaviateProtoGenerative.GenerativeResult result) {
    return unmarshalTaskOutput(result.getValuesList());
  }
}
