package io.weaviate.client6.v1.api.collections;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import io.weaviate.client6.v1.api.WeaviateApiException;
import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.grpc.GrpcTransport;
import io.weaviate.client6.v1.internal.orm.CollectionDescriptor;
import io.weaviate.client6.v1.internal.rest.RestTransport;

public class WeaviateCollectionsClient {
  private final RestTransport restTransport;
  private final GrpcTransport grpcTransport;

  public WeaviateCollectionsClient(RestTransport restTransport, GrpcTransport grpcTransport) {
    this.restTransport = restTransport;
    this.grpcTransport = grpcTransport;
  }

  public <PropertiesT> CollectionHandle<PropertiesT> use(Class<PropertiesT> cls) {
    return use(CollectionDescriptor.ofClass(cls), CollectionHandleDefaults.none());
  }

  /**
   * Obtain a handle to send requests to a particular collection.
   * The returned object is thread-safe.
   *
   * @return a handle for a collection with {@code Map<String, Object>}
   *         properties.
   */
  public CollectionHandle<Map<String, Object>> use(String collectionName) {
    return use(collectionName, CollectionHandleDefaults.none());
  }

  /**
   * Obtain a handle to send requests to a particular collection.
   * The returned object is thread-safe.
   *
   * @return a handle for a collection with {@code Map<String, Object>}
   *         properties.
   */
  public CollectionHandle<Map<String, Object>> use(
      String collectionName,
      Function<CollectionHandleDefaults.Builder, ObjectBuilder<CollectionHandleDefaults>> fn) {
    return use(CollectionDescriptor.ofMap(collectionName), fn);
  }

  private <PropertiesT> CollectionHandle<PropertiesT> use(CollectionDescriptor<PropertiesT> collection,
      Function<CollectionHandleDefaults.Builder, ObjectBuilder<CollectionHandleDefaults>> fn) {
    return new CollectionHandle<>(restTransport, grpcTransport, collection, CollectionHandleDefaults.of(fn));
  }

  public <PropertiesT> CollectionConfig create(Class<PropertiesT> cls) throws IOException {
    var collection = CollectionDescriptor.ofClass(cls);
    return create(CollectionConfig.of(collection.name(), collection.configFn()));
  }

  /**
   * Create a new Weaviate collection with default configuration.
   *
   * @return the configuration of the created collection.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @throws IOException          in case the request was not sent successfully
   *                              due to a malformed request, a networking error
   *                              or the server being unavailable.
   */
  public CollectionConfig create(String name) throws IOException {
    return create(CollectionConfig.of(name));
  }

  /**
   * Create and configure a new Weaviate collection. See
   * {@link CollectionConfig.Builder} for available options.
   *
   * @return the configuration of the created collection.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @throws IOException          in case the request was not sent successfully
   *                              due to a malformed request, a networking error
   *                              or the server being unavailable.
   */
  public CollectionConfig create(String name,
      Function<CollectionConfig.Builder, ObjectBuilder<CollectionConfig>> fn) throws IOException {
    return create(CollectionConfig.of(name, fn));
  }

  /**
   * Create a new Weaviate collection with {@link CollectionConfig}.
   *
   * @return the configuration of the created collection.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @throws IOException          in case the request was not sent successfully
   *                              due to a malformed request, a networking error
   *                              or the server being unavailable.
   */
  public CollectionConfig create(CollectionConfig collection) throws IOException {
    return this.restTransport.performRequest(new CreateCollectionRequest(collection),
        CreateCollectionRequest._ENDPOINT);
  }

  /**
   * Fetch Weaviate collection configuration.
   *
   * @return the collection configuration if one with this name exists.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @throws IOException          in case the request was not sent successfully
   *                              due to a malformed request, a networking error
   *                              or the server being unavailable.
   */
  public Optional<CollectionConfig> getConfig(String name) throws IOException {
    return this.restTransport.performRequest(new GetConfigRequest(name), GetConfigRequest._ENDPOINT);
  }

  /**
   * Fetch configurations for all collections in Weaviate.
   *
   * @return a list of collection configurations.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @throws IOException          in case the request was not sent successfully
   *                              due to a malformed request, a networking error
   *                              or the server being unavailable.
   */
  public List<CollectionConfig> list() throws IOException {
    return this.restTransport.performRequest(new ListCollectionRequest(), ListCollectionRequest._ENDPOINT);
  }

  /**
   * Delete a Weaviate collection.
   *
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @throws IOException          in case the request was not sent successfully
   *                              due to a malformed request, a networking error
   *                              or the server being unavailable.
   */
  public void delete(String name) throws IOException {
    this.restTransport.performRequest(new DeleteCollectionRequest(name), DeleteCollectionRequest._ENDPOINT);
  }

  /**
   * Delete all collections in Weaviate.
   *
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @throws IOException          in case the request was not sent successfully
   *                              due to a malformed request, a networking error
   *                              or the server being unavailable.
   */
  public void deleteAll() throws IOException {
    for (var collection : list()) {
      delete(collection.collectionName());
    }
  }

  /**
   * Check if a collection with this name exists.
   *
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @throws IOException          in case the request was not sent successfully
   *                              due to a malformed request, a networking error
   *                              or the server being unavailable.
   */
  public boolean exists(String name) throws IOException {
    return getConfig(name).isPresent();
  }
}
