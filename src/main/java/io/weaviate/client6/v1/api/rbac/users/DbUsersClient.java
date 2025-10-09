package io.weaviate.client6.v1.api.rbac.users;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import io.weaviate.client6.v1.api.WeaviateApiException;
import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.rest.RestTransport;

public class DbUsersClient extends NamespacedUsersClient {

  public DbUsersClient(RestTransport restTransport) {
    super(restTransport, UserType.DB_USER);
  }

  /**
   * Create a new "db" user.
   *
   * @param userId User ID.
   * @return API key for the created user.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @throws IOException          in case the request was not sent successfully
   *                              due to a malformed request, a networking error
   *                              or the server being unavailable.
   */
  public String create(String userId) throws IOException {
    return this.restTransport.performRequest(new CreateDbUserRequest(userId), CreateDbUserRequest._ENDPOINT);
  }

  /**
   * Delete a "db" user.
   *
   * @param userId User ID.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @throws IOException          in case the request was not sent successfully
   *                              due to a malformed request, a networking error
   *                              or the server being unavailable.
   */
  public void delete(String userId) throws IOException {
    this.restTransport.performRequest(new DeleteDbUserRequest(userId), DeleteDbUserRequest._ENDPOINT);
  }

  /**
   * Activate a "db" user.
   *
   * @param userId User ID.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @throws IOException          in case the request was not sent successfully
   *                              due to a malformed request, a networking error
   *                              or the server being unavailable.
   */
  public void activate(String userId) throws IOException {
    this.restTransport.performRequest(new ActivateDbUserRequest(userId), ActivateDbUserRequest._ENDPOINT);
  }

  /**
   * Deactivate a "db" user.
   *
   * @param userId User ID.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @throws IOException          in case the request was not sent successfully
   *                              due to a malformed request, a networking error
   *                              or the server being unavailable.
   */
  public void deactivate(String userId) throws IOException {
    this.restTransport.performRequest(new DeactivateDbUserRequest(userId), DeactivateDbUserRequest._ENDPOINT);
  }

  /**
   * Rotate API key of the "db" user.
   *
   * @param userId User ID.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @throws IOException          in case the request was not sent successfully
   *                              due to a malformed request, a networking error
   *                              or the server being unavailable.
   */
  public String rotateKey(String userId) throws IOException {
    return this.restTransport.performRequest(new RotateDbUserKeyRequest(userId), RotateDbUserKeyRequest._ENDPOINT);
  }

  /**
   * Fetch "db" user info.
   *
   * @param userId User ID.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @throws IOException          in case the request was not sent successfully
   *                              due to a malformed request, a networking error
   *                              or the server being unavailable.
   */
  public Optional<DbUser> byName(String userId) throws IOException {
    return this.restTransport.performRequest(GetDbUserRequest.of(userId), GetDbUserRequest._ENDPOINT);
  }

  /**
   * Fetch "db" user info.
   *
   * @param userId User ID.
   * @param fn     Lambda expression for optional parameters.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @throws IOException          in case the request was not sent successfully
   *                              due to a malformed request, a networking error
   *                              or the server being unavailable.
   */
  public Optional<DbUser> byName(String userId, Function<GetDbUserRequest.Builder, ObjectBuilder<GetDbUserRequest>> fn)
      throws IOException {
    return this.restTransport.performRequest(GetDbUserRequest.of(userId, fn), GetDbUserRequest._ENDPOINT);
  }

  /**
   * List all "db" users.
   *
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @throws IOException          in case the request was not sent successfully
   *                              due to a malformed request, a networking error
   *                              or the server being unavailable.
   */
  public List<DbUser> list()
      throws IOException {
    return this.restTransport.performRequest(ListDbUsersRequest.of(), ListDbUsersRequest._ENDPOINT);
  }

  /**
   * List all "db" users.
   *
   * @param fn Lambda expression for optional parameters.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @throws IOException          in case the request was not sent successfully
   *                              due to a malformed request, a networking error
   *                              or the server being unavailable.
   */
  public List<DbUser> list(Function<ListDbUsersRequest.Builder, ObjectBuilder<ListDbUsersRequest>> fn)
      throws IOException {
    return this.restTransport.performRequest(ListDbUsersRequest.of(fn), ListDbUsersRequest._ENDPOINT);
  }
}
