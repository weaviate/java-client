package io.weaviate.client6.v1.api.collections.batch;

import io.grpc.stub.StreamObserver;

interface StreamFactory<SendT, RecvT> {
  StreamObserver<SendT> createStream(StreamObserver<RecvT> out);
}
