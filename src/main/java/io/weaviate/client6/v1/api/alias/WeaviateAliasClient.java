package io.weaviate.client6.v1.api.alias;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import io.weaviate.client6.v1.api.WeaviateApiException;
import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.rest.RestTransport;

public class WeaviateAliasClient {
  private final RestTransport restTransport;

  public WeaviateAliasClient(RestTransport restTransport) {
    this.restTransport = restTransport;
  }

  /**
   * Create a new collection alias.
   *
   * @param collection Original collection name.
   * @param alias      Collection alias.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @throws IOException          in case the request was not sent successfully
   *                              due to a malformed request, a networking error
   *                              or the server being unavailable.
   */
  public void create(String collection, String alias) throws IOException {
    create(new Alias(collection, alias));
  }

  /**
   * Create a new collection alias.
   *
   * @param alias Alias object.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @throws IOException          in case the request was not sent successfully
   *                              due to a malformed request, a networking error
   *                              or the server being unavailable.
   */
  public void create(Alias alias) throws IOException {
    this.restTransport.performRequest(new CreateAliasRequest(alias), CreateAliasRequest._ENDPOINT);
  }

  /**
   * List all collection aliases defined in the cluster.
   *
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @throws IOException          in case the request was not sent successfully
   *                              due to a malformed request, a networking error
   *                              or the server being unavailable.
   */
  public List<Alias> list() throws IOException {
    return list(ListAliasRequest.of());
  }

  /**
   * List all collection aliases defined in the cluster.
   *
   * @param fn Lambda expression for optional parameters.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @throws IOException          in case the request was not sent successfully
   *                              due to a malformed request, a networking error
   *                              or the server being unavailable.
   * @return A list of aliases.
   */
  public List<Alias> list(Function<ListAliasRequest.Builder, ObjectBuilder<ListAliasRequest>> fn) throws IOException {
    return list(ListAliasRequest.of(fn));
  }

  private List<Alias> list(ListAliasRequest request) throws IOException {
    return this.restTransport.performRequest(request, ListAliasRequest._ENDPOINT);
  }

  /**
   * Get alias by name.
   *
   * @param alias Collection alias.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @throws IOException          in case the request was not sent successfully
   *                              due to a malformed request, a networking error
   *                              or the server being unavailable.
   * @return Alias if one exists and empty {@code Optional} otherwise.
   */
  public Optional<Alias> get(String alias) throws IOException {
    return this.restTransport.performRequest(new GetAliasRequest(alias), GetAliasRequest._ENDPOINT);
  }

  /**
   * Change which collection this alias references.
   *
   * @param alias               Collection alias.
   * @param newTargetCollection Collection name.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @throws IOException          in case the request was not sent successfully
   *                              due to a malformed request, a networking error
   *                              or the server being unavailable.
   */
  public void update(String alias, String newTargetCollection) throws IOException {
    this.restTransport.performRequest(new UpdateAliasRequest(alias, newTargetCollection),
        UpdateAliasRequest._ENDPOINT);
  }

  /**
   * Delete an alias. The previously aliased collection is not affected.
   *
   * @param alias Collection alias.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @throws IOException          in case the request was not sent successfully
   *                              due to a malformed request, a networking error
   *                              or the server being unavailable.
   * 
   * @return {@code true} if the alias was deleted, {@code false} if there was no
   *         alias to delete.
   */
  public boolean delete(String alias) throws IOException {
    return this.restTransport.performRequest(new DeleteAliasRequest(alias), DeleteAliasRequest._ENDPOINT);
  }
}
