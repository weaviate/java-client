package io.weaviate.client.v1.grpc;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import com.google.protobuf.ByteString;

import io.weaviate.client.Config;
import io.weaviate.client.base.http.HttpClient;
import io.weaviate.client.v1.auth.provider.AccessTokenProvider;
import io.weaviate.client.v1.graphql.query.argument.NearVectorArgument;
import io.weaviate.client.v1.grpc.query.Raw;

public class GRPC {
  private Config config;
  private HttpClient httpClient;
  private AccessTokenProvider tokenProvider;

  public static class Arguments {
    public NearVectorArgument.NearVectorArgumentBuilder nearVectorArgBuilder() {
      return NearVectorArgument.builder();
    }
  }

  public GRPC(HttpClient httpClient, Config config, AccessTokenProvider tokenProvider) {
    this.config = config;
    this.httpClient = httpClient;
    this.tokenProvider = tokenProvider;
  }

  public Raw raw() {
    return new Raw(httpClient, config, tokenProvider);
  }

  public GRPC.Arguments arguments() {
    return new GRPC.Arguments();
  }

  public static ByteString toByteString(Float[] vector) {
    ByteBuffer buffer = ByteBuffer.allocate(vector.length * Float.BYTES).order(ByteOrder.LITTLE_ENDIAN);
    Arrays.stream(vector).forEach(buffer::putFloat);
    return ByteString.copyFrom(buffer.array());
  }

  public static ByteString toByteString(float[] vector) {
    ByteBuffer buffer = ByteBuffer.allocate(vector.length * Float.BYTES).order(ByteOrder.LITTLE_ENDIAN);
    for (float f : vector) {
      buffer.putFloat(f);
    }
    return ByteString.copyFrom(buffer.array());
  }
}
