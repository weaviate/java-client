package io.weaviate.testutil.transport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import io.weaviate.client6.v1.internal.rest.BooleanEndpoint;
import io.weaviate.client6.v1.internal.rest.Endpoint;
import io.weaviate.client6.v1.internal.rest.RestTransport;

public class MockRestTransport implements RestTransport {

  private record Request<RequestT>(String method, String requestUrl, String body,
      Map<String, Object> queryParameters) {

    Request(RequestT req, Endpoint<RequestT, ?> ep) {
      this(ep.method(req), ep.requestUrl(req), ep.body(req), ep.queryParameters(req));
    }
  }

  @FunctionalInterface
  public interface AssertFunction {
    void apply(String method, String requestUrl, String body, Map<String, Object> queryParameters);
  }

  private List<Request<?>> requests = new ArrayList<>();

  public void assertNext(AssertFunction... assertions) {
    var assertN = Math.min(assertions.length, requests.size());
    try {
      for (var i = 0; i < assertN; i++) {
        var req = requests.get(i);
        assertions[i].apply(req.method, req.requestUrl, req.body, req.queryParameters);
      }
    } finally {
      requests.clear();
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public <RequestT, ResponseT, ExceptionT> ResponseT performRequest(RequestT request,
      Endpoint<RequestT, ResponseT> endpoint) throws IOException {
    requests.add(new Request<>(request, endpoint));
    if (endpoint instanceof BooleanEndpoint) {
      return (ResponseT) Boolean.TRUE;
    }
    return null;
  }

  @Override
  public <RequestT, ResponseT, ExceptionT> CompletableFuture<ResponseT> performRequestAsync(RequestT request,
      Endpoint<RequestT, ResponseT> endpoint) {
    requests.add(new Request<>(request, endpoint));
    return null;
  }

  @Override
  public void close() throws IOException {
  }
}
