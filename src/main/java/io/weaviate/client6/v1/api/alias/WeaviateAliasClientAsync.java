package io.weaviate.client6.v1.api.alias;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.rest.RestTransport;

public class WeaviateAliasClientAsync {
  private final RestTransport restTransport;

  public WeaviateAliasClientAsync(RestTransport restTransport) {
    this.restTransport = restTransport;
  }

  public CompletableFuture<Void> create(String collection, String alias) {
    return create(new Alias(collection, alias));
  }

  public CompletableFuture<Void> create(Alias alias) {
    return this.restTransport.performRequestAsync(new CreateAliasRequest(alias), CreateAliasRequest._ENDPOINT);
  }

  public CompletableFuture<List<Alias>> list() {
    return list(ListAliasRequest.of());
  }

  public CompletableFuture<List<Alias>> list(Function<ListAliasRequest.Builder, ObjectBuilder<ListAliasRequest>> fn) {
    return list(ListAliasRequest.of(fn));
  }

  private CompletableFuture<List<Alias>> list(ListAliasRequest request) {
    return this.restTransport.performRequestAsync(request, ListAliasRequest._ENDPOINT);
  }

  public CompletableFuture<Optional<Alias>> get(String alias) {
    return this.restTransport.performRequestAsync(new GetAliasRequest(alias), GetAliasRequest._ENDPOINT);
  }

  public CompletableFuture<Void> update(String alias, String newTargetCollection) {
    return this.restTransport.performRequestAsync(new UpdateAliasRequest(alias, newTargetCollection),
        UpdateAliasRequest._ENDPOINT);
  }

  public CompletableFuture<Void> delete(String alias) {
    return this.restTransport.performRequestAsync(new DeleteAliasRequest(alias), DeleteAliasRequest._ENDPOINT);
  }
}
