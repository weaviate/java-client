package io.weaviate.client6.internal.codec.grpc;

public interface GrpcMarshaler<R> {
  R marshal();
}
