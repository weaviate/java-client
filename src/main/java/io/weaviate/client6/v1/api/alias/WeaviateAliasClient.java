package io.weaviate.client6.v1.api.alias;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.rest.RestTransport;

public class WeaviateAliasClient {
  private final RestTransport restTransport;

  public WeaviateAliasClient(RestTransport restTransport) {
    this.restTransport = restTransport;
  }

  public void create(String collection, String alias) throws IOException {
    create(new Alias(collection, alias));
  }

  public void create(Alias alias) throws IOException {
    this.restTransport.performRequest(new CreateAliasRequest(alias), CreateAliasRequest._ENDPOINT);
  }

  public List<Alias> list() throws IOException {
    return list(ListAliasRequest.of());
  }

  public List<Alias> list(Function<ListAliasRequest.Builder, ObjectBuilder<ListAliasRequest>> fn) throws IOException {
    return list(ListAliasRequest.of(fn));
  }

  private List<Alias> list(ListAliasRequest request) throws IOException {
    return this.restTransport.performRequest(request, ListAliasRequest._ENDPOINT);
  }

  public Optional<Alias> get(String alias) throws IOException {
    return this.restTransport.performRequest(new GetAliasRequest(alias), GetAliasRequest._ENDPOINT);
  }

  public void update(String alias, String newTargetCollection) throws IOException {
    this.restTransport.performRequest(new UpdateAliasRequest(alias, newTargetCollection),
        UpdateAliasRequest._ENDPOINT);
  }

  public void delete(String alias) throws IOException {
    this.restTransport.performRequest(new DeleteAliasRequest(alias), DeleteAliasRequest._ENDPOINT);
  }
}
