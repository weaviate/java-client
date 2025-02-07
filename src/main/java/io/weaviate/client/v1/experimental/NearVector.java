package io.weaviate.client.v1.experimental;

import java.util.function.Consumer;

import io.weaviate.client.grpc.protocol.v1.WeaviateProtoSearchGet;
import io.weaviate.client.grpc.protocol.v1.WeaviateProtoSearchGet.SearchRequest;
import io.weaviate.client.v1.grpc.GRPC;

public class NearVector {
  private final float[] vector;
  private final Options opt;

  void append(SearchRequest.Builder search) {
    WeaviateProtoSearchGet.NearVector.Builder nearVector = WeaviateProtoSearchGet.NearVector
        .newBuilder();
    nearVector.setVectorBytes(GRPC.toByteString(vector));
    opt.append(search, nearVector);
    search.setNearVector(nearVector.build());
  }

  public NearVector(float[] vector, Consumer<Options> options) {
    this.opt = new Options();
    this.vector = vector;
    options.accept(this.opt);
  }

  public static class Options extends SearchOptions<Options> {
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

    void append(SearchRequest.Builder search, WeaviateProtoSearchGet.NearVector.Builder nearVector) {
      if (certainty != null) {
        nearVector.setCertainty(certainty);
      } else if (distance != null) {
        nearVector.setDistance(distance);
      }
      super.append(search);
    }
  }
}
