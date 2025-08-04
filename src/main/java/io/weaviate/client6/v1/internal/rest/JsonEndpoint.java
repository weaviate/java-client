package io.weaviate.client6.v1.internal.rest;

/** An Endpoint which expects a JSON response body. */
public interface JsonEndpoint<RequestT, ResponseT>
    extends Endpoint<RequestT, ResponseT> {
  ResponseT deserializeResponse(int statusCode, String responseBody);
}
