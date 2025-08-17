package io.weaviate.client6.v1.internal.rest;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public interface RestTransport extends AutoCloseable {
  <RequestT, ResponseT, ExceptionT> ResponseT performRequest(RequestT request,
      Endpoint<RequestT, ResponseT> endpoint)
      throws IOException;

  <RequestT, ResponseT, ExceptionT> CompletableFuture<ResponseT> performRequestAsync(RequestT request,
      Endpoint<RequestT, ResponseT> endpoint);

  RestTransportOptions getTransportOptions();
}
