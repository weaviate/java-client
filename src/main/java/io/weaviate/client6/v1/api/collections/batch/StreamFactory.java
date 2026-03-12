package io.weaviate.client6.v1.api.collections.batch;

import io.grpc.stub.StreamObserver;

/**
 * @param <SendT> the type of the object sent down the stream.
 * @param <RecvT> the type of the object received from the stream.
 */
@FunctionalInterface
interface StreamFactory<SendT, RecvT> {
  /** Create a new stream for the send-recv observer pair. */
  StreamObserver<SendT> createStream(StreamObserver<RecvT> recv);
}
