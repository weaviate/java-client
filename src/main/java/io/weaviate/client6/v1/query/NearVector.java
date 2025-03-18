package io.weaviate.client6.v1.query;

import java.util.function.Consumer;

import io.weaviate.client6.grpc.protocol.v1.WeaviateProtoBaseSearch;
import io.weaviate.client6.grpc.protocol.v1.WeaviateProtoSearchGet.SearchRequest;
import io.weaviate.client6.internal.GRPC;

public class NearVector {
  private final Float[] vector;
  private final Options options;

  void appendTo(SearchRequest.Builder search) {
    var nearVector = WeaviateProtoBaseSearch.NearVector.newBuilder();

    // TODO: we should only add (named) Vectors.
    // Since we do not force the users to supply a name when defining an index,
    // we also need a way to "get default vector name" from the collection.
    // For Map<String, Object> (untyped query handle) we always require the name.
    nearVector.setVectorBytes(GRPC.toByteString(vector));
    options.append(search, nearVector);
    search.setNearVector(nearVector.build());
  }

  public NearVector(Float[] vector, Consumer<Options> options) {
    this.options = new Options();
    this.vector = vector;
    options.accept(this.options);
  }

  public static class Options extends QueryOptions<Options> {
    private Float distance;
    private Float certainty;

    public Options distance(float distance) {
      this.distance = distance;
      return this;
    }

    public Options certainty(float certainty) {
      this.certainty = certainty;
      return this;
    }

    void append(SearchRequest.Builder search, WeaviateProtoBaseSearch.NearVector.Builder nearVector) {
      if (certainty != null) {
        nearVector.setCertainty(certainty);
      } else if (distance != null) {
        nearVector.setDistance(distance);
      }
      super.appendTo(search);
    }
  }
}
