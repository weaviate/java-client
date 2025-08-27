package io.weaviate.client6.v1.api.alias;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.rest.RestTransport;

/** Async client for {@code /aliases} endpoints. */
public class WeaviateAliasClientAsync {
  private final RestTransport restTransport;

  public WeaviateAliasClientAsync(RestTransport restTransport) {
    this.restTransport = restTransport;
  }

  /**
   * Create a new collection alias.
   *
   * @param collection Original collection name.
   * @param alias      Collection alias.
   *
   * @return A future holding the server's response.
   */
  public CompletableFuture<Void> create(String collection, String alias) {
    return create(new Alias(collection, alias));
  }

  /**
   * Create a new collection alias.
   *
   * @param alias Alias object.
   *
   * @return A future holding the server's response.
   */
  public CompletableFuture<Void> create(Alias alias) {
    return this.restTransport.performRequestAsync(new CreateAliasRequest(alias), CreateAliasRequest._ENDPOINT);
  }

  /**
   * List all collection aliases defined in the cluster.
   *
   * @return A future holding the server's response.
   */
  public CompletableFuture<List<Alias>> list() {
    return list(ListAliasRequest.of());
  }

  /**
   * List all collection aliases defined in the cluster.
   *
   * @param fn Lambda expression for optional parameters.
   *
   * @return A future holding the server's response.
   */
  public CompletableFuture<List<Alias>> list(Function<ListAliasRequest.Builder, ObjectBuilder<ListAliasRequest>> fn) {
    return list(ListAliasRequest.of(fn));
  }

  private CompletableFuture<List<Alias>> list(ListAliasRequest request) {
    return this.restTransport.performRequestAsync(request, ListAliasRequest._ENDPOINT);
  }

  /**
   * Get alias by name.
   *
   * @param alias Collection alias.
   *
   * @return A future holding the server's response.
   */
  public CompletableFuture<Optional<Alias>> get(String alias) {
    return this.restTransport.performRequestAsync(new GetAliasRequest(alias), GetAliasRequest._ENDPOINT);
  }

  /**
   * Change which collection this alias references.
   *
   * @param alias               Collection alias.
   * @param newTargetCollection Collection name.
   *
   * @return A future holding the server's response.
   */
  public CompletableFuture<Void> update(String alias, String newTargetCollection) {
    return this.restTransport.performRequestAsync(new UpdateAliasRequest(alias, newTargetCollection),
        UpdateAliasRequest._ENDPOINT);
  }

  /**
   * Delete an alias. The previously aliased collection is not affected.
   *
   * @param alias Collection alias.
   *
   * @return A future holding the server's response.
   */
  public CompletableFuture<Void> delete(String alias) {
    return this.restTransport.performRequestAsync(new DeleteAliasRequest(alias), DeleteAliasRequest._ENDPOINT);
  }
}
