package io.weaviate.testutil.transport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageOrBuilder;
import com.google.protobuf.util.JsonFormat;

import io.weaviate.client6.v1.internal.grpc.GrpcTransport;
import io.weaviate.client6.v1.internal.grpc.Rpc;

public class MockGrpcTransport implements GrpcTransport {

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
}
