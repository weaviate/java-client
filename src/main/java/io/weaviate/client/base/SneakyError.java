package io.weaviate.client.base;

import java.util.List;

/**
 * Describes a response body that might contain error messages even if returned with a "good" status code.
 *
 * <p>
 * Notably, GraphQL APIs conventionally return HTTP 200 for all requests which they were able to parse,
 * even if the request itself is a "bad request". Other classes used for deserializing such response bodies
 * may implement this interface to help {@link BaseClient} extract an error message from a "custom" location
 * in the response.
 */
public interface SneakyError {
  List<WeaviateErrorMessage> errorMessages();
}
