package io.weaviate.testutil.transport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;
import java.util.concurrent.CompletableFuture;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageOrBuilder;
import com.google.protobuf.util.JsonFormat;

import io.grpc.stub.StreamObserver;
import io.weaviate.client6.v1.internal.grpc.GrpcTransport;
import io.weaviate.client6.v1.internal.grpc.Rpc;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch.BatchStreamReply;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch.BatchStreamRequest;

public class MockGrpcTransport implements GrpcTransport {
  private final String host = "example.com";

  @FunctionalInterface
  public interface AssertFunction {
    void apply(String json);
  }

  private List<MessageOrBuilder> requests = new ArrayList<>();

  public void assertNext(AssertFunction... assertions) {
    var assertN = Math.min(assertions.length, requests.size());
    try {
      for (var i = 0; i < assertN; i++) {
        var req = requests.get(i);
        String json;
        try {
          json = JsonFormat.printer().print(req);
        } catch (InvalidProtocolBufferException e) {
          throw new RuntimeException(e);
        }
        assertions[i].apply(json);
      }
    } finally {
      requests.clear();
    }
  }

  @Override
  public <RequestT, RequestM, ReplyM, ResponseT> ResponseT performRequest(RequestT request,
      Rpc<RequestT, RequestM, ResponseT, ReplyM> rpc) {
    var r = rpc.marshal(request);
    requests.add((MessageOrBuilder) r);
    return null;
  }

  @Override
  public <RequestT, RequestM, ReplyM, ResponseT> CompletableFuture<ResponseT> performRequestAsync(RequestT request,
      Rpc<RequestT, RequestM, ResponseT, ReplyM> rpc) {
    requests.add((MessageOrBuilder) rpc.marshal(request));
    return null;
  }

  @Override
  public void close() throws IOException {
  }

  @Override
  public StreamObserver<BatchStreamRequest> createStream(StreamObserver<BatchStreamReply> recv) {
    // TODO(dyma): implement for tests
    throw new UnsupportedOperationException("Unimplemented method 'createStream'");
  }

  @Override
  public OptionalInt maxMessageSizeBytes() {
    // TODO(dyma): implement for tests
    throw new UnsupportedOperationException("Unimplemented method 'maxMessageSizeBytes'");
  }

  @Override
  public String host() {
    return host;
  }
}
