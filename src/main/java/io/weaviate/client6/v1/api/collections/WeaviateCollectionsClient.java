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

  /**
   * Obtain a handle to send requests to a particular collection.
   * The returned object is thread-safe.
   *
   * @param cls Class that represents an object in the collection.
   * @return a handle for a collection with {@code Class<PropertiesT>}
   *         properties.
   */
  public <PropertiesT extends Record> CollectionHandle<PropertiesT> use(Class<PropertiesT> cls) {
    return use(CollectionDescriptor.ofClass(cls), CollectionHandleDefaults.none());
  }

  /**
   * Obtain a handle to send requests to a particular collection.
   * The returned object is thread-safe.
   *
   * @param cls Class that represents an object in the collection.
   * @param fn  Lamda expression for optional parameters.
   * @return a handle for a collection with {@code Class<PropertiesT>}
   *         properties.
   */
  public <PropertiesT extends Record> CollectionHandle<PropertiesT> use(
      Class<PropertiesT> cls,
      Function<CollectionHandleDefaults.Builder, ObjectBuilder<CollectionHandleDefaults>> fn) {
    return use(CollectionDescriptor.ofClass(cls), fn);
  }

  /**
   * Obtain a handle to send requests to a particular collection.
   * The returned object is thread-safe.
   *
   * @param collectionName Name of the collection.
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
   * @param collectionName Name of the collection.
   * @param fn             Lamda expression for optional parameters.
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

  /**
   * Create a new Weaviate collection with default configuration.
   *
   * <pre>{@code
   * // Define a record class that represents an object in collection.
   * record Song(
   *  String title;
   *  int yearReleased;
   *  String[] genres;
   * ) {}
   *
   * client.collections.create(Song.class);
   * }</pre>
   *
   * @param cls Class that represents an object in the collection.
   * @return the configuration of the created collection.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @throws IOException          in case the request was not sent successfully
   *                              due to a malformed request, a networking error
   *                              or the server being unavailable.
   * @see io.weaviate.client6.v1.api.collections.annotations.Collection
   * @see io.weaviate.client6.v1.api.collections.annotations.Property
   */
  public <PropertiesT extends Record> CollectionConfig create(Class<PropertiesT> cls) throws IOException {
    var collection = CollectionDescriptor.ofClass(cls);
    return create(CollectionConfig.of(collection.collectionName(), collection.configFn()));
  }

  /**
   * Create and configure a new Weaviate collection. See
   * {@link CollectionConfig.Builder} for available options.
   *
   * @param cls Class that represents an object in the collection.
   * @param fn  Lamda expression for optional parameters.
   * @return the configuration of the created collection.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @throws IOException          in case the request was not sent successfully
   *                              due to a malformed request, a networking error
   *                              or the server being unavailable.
   * @see io.weaviate.client6.v1.api.collections.annotations.Collection
   * @see io.weaviate.client6.v1.api.collections.annotations.Property
   * @see WeaviateCollectionsClient#create(Class)
   */
  public <PropertiesT extends Record> CollectionConfig create(
      Class<PropertiesT> cls,
      Function<CollectionConfig.Builder, ObjectBuilder<CollectionConfig>> fn) throws IOException {
    var collection = CollectionDescriptor.ofClass(cls);
    var configFn = ObjectBuilder.partial(fn, collection.configFn());
    return create(CollectionConfig.of(collection.collectionName(), configFn));
  }

  /**
   * Create a new Weaviate collection with default configuration.
   *
   * @param collectionName Collection name.
   * @return the configuration of the created collection.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @throws IOException          in case the request was not sent successfully
   *                              due to a malformed request, a networking error
   *                              or the server being unavailable.
   */
  public CollectionConfig create(String collectionName) throws IOException {
    return create(CollectionConfig.of(collectionName));
  }

  /**
   * Create and configure a new Weaviate collection. See
   * {@link CollectionConfig.Builder} for available options.
   *
   * @param collectionName Collection name.
   * @param fn             Lamda expression for optional parameters.
   * @return the configuration of the created collection.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @throws IOException          in case the request was not sent successfully
   *                              due to a malformed request, a networking error
   *                              or the server being unavailable.
   */
  public CollectionConfig create(String collectionName,
      Function<CollectionConfig.Builder, ObjectBuilder<CollectionConfig>> fn) throws IOException {
    return create(CollectionConfig.of(collectionName, fn));
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
   * @param collectionName Collection name.
   * @return the collection configuration if one with this name exists.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @throws IOException          in case the request was not sent successfully
   *                              due to a malformed request, a networking error
   *                              or the server being unavailable.
   */
  public Optional<CollectionConfig> getConfig(String collectionName) throws IOException {
    return this.restTransport.performRequest(new GetConfigRequest(collectionName), GetConfigRequest._ENDPOINT);
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
   * @param collectionName Collection name.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @throws IOException          in case the request was not sent successfully
   *                              due to a malformed request, a networking error
   *                              or the server being unavailable.
   */
  public void delete(String collectionName) throws IOException {
    this.restTransport.performRequest(new DeleteCollectionRequest(collectionName), DeleteCollectionRequest._ENDPOINT);
  }

  /**
   * Delete a Weaviate collection.
   *
   * @param cls Class that represents an object in the collection.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @throws IOException          in case the request was not sent successfully
   *                              due to a malformed request, a networking error
   *                              or the server being unavailable.
   */
  public void delete(Class<? extends Record> cls) throws IOException {
    delete(CollectionDescriptor.ofClass(cls).collectionName());
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
   * @param collectionName Collection name.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @throws IOException          in case the request was not sent successfully
   *                              due to a malformed request, a networking error
   *                              or the server being unavailable.
   */
  public boolean exists(String collectionName) throws IOException {
    return getConfig(collectionName).isPresent();
  }

  /**
   * Check if a collection with this name exists.
   *
   * @param cls Class that represents an object in the collection.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @throws IOException          in case the request was not sent successfully
   *                              due to a malformed request, a networking error
   *                              or the server being unavailable.
   */
  public boolean exists(Class<? extends Record> cls) throws IOException {
    return exists(CollectionDescriptor.ofClass(cls).collectionName());
  }
}
