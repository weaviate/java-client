package io.weaviate.client6.v1.api.collections;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.grpc.GrpcTransport;
import io.weaviate.client6.v1.internal.orm.CollectionDescriptor;
import io.weaviate.client6.v1.internal.rest.RestTransport;

public class WeaviateCollectionsClientAsync {
  private final RestTransport restTransport;
  private final GrpcTransport grpcTransport;

  public WeaviateCollectionsClientAsync(RestTransport restTransport, GrpcTransport grpcTransport) {
    this.restTransport = restTransport;
    this.grpcTransport = grpcTransport;
  }

  /**
   * Obtain a handle to send requests to a particular collection.
   * The returned object is thread-safe.
   *
   * @param cls Class that represents an object in the collection.
   * @return a handle for a collection with {@code PropertiesT} properties.
   */
  public <PropertiesT extends Record> CollectionHandleAsync<PropertiesT> use(Class<PropertiesT> cls) {
    return use(CollectionDescriptor.ofClass(cls), CollectionHandleDefaults.none());
  }

  /**
   * Obtain a handle to send requests to a particular collection.
   * The returned object is thread-safe.
   *
   * @param cls Class that represents an object in the collection.
   * @param fn  Lamda expression for optional parameters.
   * @return a handle for a collection with {@code PropertiesT} properties.
   */
  public <PropertiesT extends Record> CollectionHandleAsync<PropertiesT> use(
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
  public CollectionHandleAsync<Map<String, Object>> use(String collectionName) {
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
  public CollectionHandleAsync<Map<String, Object>> use(
      String collectionName,
      Function<CollectionHandleDefaults.Builder, ObjectBuilder<CollectionHandleDefaults>> fn) {
    return use(CollectionDescriptor.ofMap(collectionName), fn);
  }

  private <PropertiesT> CollectionHandleAsync<PropertiesT> use(CollectionDescriptor<PropertiesT> collection,
      Function<CollectionHandleDefaults.Builder, ObjectBuilder<CollectionHandleDefaults>> fn) {
    return new CollectionHandleAsync<>(restTransport, grpcTransport, collection, CollectionHandleDefaults.of(fn));
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
   * @see io.weaviate.client6.v1.api.collections.annotations.Collection
   * @see io.weaviate.client6.v1.api.collections.annotations.Property
   */
  public <PropertiesT extends Record> CompletableFuture<CollectionHandleAsync<PropertiesT>> create(
      Class<PropertiesT> cls) {
    var collection = CollectionDescriptor.ofClass(cls);
    return create(CollectionConfig.of(collection.collectionName(), collection.configFn()))
        .thenApply(__ -> use(cls));
  }

  /**
   * Create and configure a new Weaviate collection. See
   * {@link CollectionConfig.Builder} for available options.
   *
   * @param cls Class that represents an object in the collection.
   * @param fn  Lamda expression for optional parameters.
   * @see io.weaviate.client6.v1.api.collections.annotations.Collection
   * @see io.weaviate.client6.v1.api.collections.annotations.Property
   * @see WeaviateCollectionsClientAsync#create(Class)
   */
  public <PropertiesT extends Record> CompletableFuture<CollectionHandleAsync<PropertiesT>> create(
      Class<PropertiesT> cls,
      Function<CollectionConfig.Builder, ObjectBuilder<CollectionConfig>> fn) {
    var collection = CollectionDescriptor.ofClass(cls);
    var configFn = ObjectBuilder.partial(fn, collection.configFn());
    return create(CollectionConfig.of(collection.collectionName(), configFn))
        .thenApply(__ -> use(cls));
  }

  /**
   * Create a new Weaviate collection with default configuration.
   *
   * @param collectionName Collection name.
   */
  public CompletableFuture<CollectionHandleAsync<Map<String, Object>>> create(String collectionName) {
    return create(CollectionConfig.of(collectionName));
  }

  /**
   * Create and configure a new Weaviate collection. See
   * {@link CollectionConfig.Builder} for available options.
   *
   * @param collectionName Collection name.
   * @param fn             Lamda expression for optional parameters.
   */
  public CompletableFuture<CollectionHandleAsync<Map<String, Object>>> create(String collectionName,
      Function<CollectionConfig.Builder, ObjectBuilder<CollectionConfig>> fn) {
    return create(CollectionConfig.of(collectionName, fn));
  }

  /**
   * Create a new Weaviate collection with {@link CollectionConfig}.
   */
  public CompletableFuture<CollectionHandleAsync<Map<String, Object>>> create(CollectionConfig collection) {
    return this.restTransport.performRequestAsync(new CreateCollectionRequest(collection),
        CreateCollectionRequest._ENDPOINT).thenApply(__ -> use(collection.collectionName()));
  }

  /**
   * Fetch Weaviate collection configuration.
   *
   * @param collectionName Collection name.
   */
  public CompletableFuture<Optional<CollectionConfig>> getConfig(String collectionName) {
    return this.restTransport.performRequestAsync(new GetConfigRequest(collectionName), GetConfigRequest._ENDPOINT);
  }

  public CompletableFuture<List<CollectionConfig>> list() {
    return this.restTransport.performRequestAsync(new ListCollectionRequest(), ListCollectionRequest._ENDPOINT);
  }

  /**
   * Delete a Weaviate collection.
   *
   * @param collectionName Collection name.
   */
  public CompletableFuture<Void> delete(String collectionName) {
    return this.restTransport.performRequestAsync(new DeleteCollectionRequest(collectionName),
        DeleteCollectionRequest._ENDPOINT);
  }

  /**
   * Delete a Weaviate collection.
   *
   * @param cls Class that represents an object in the collection.
   */
  public CompletableFuture<Void> delete(Class<? extends Record> cls) {
    return delete(CollectionDescriptor.ofClass(cls).collectionName());
  }

  /**
   * Delete all collections in Weaviate.
   */
  public CompletableFuture<Void> deleteAll() throws IOException {
    return list().thenCompose(collections -> {
      var futures = collections.stream()
          .map(collection -> delete(collection.collectionName()))
          .toArray(CompletableFuture[]::new);
      return CompletableFuture.allOf(futures);
    });
  }

  /**
   * Check if a collection with this name exists.
   *
   * @param collectionName Collection name.
   */
  public CompletableFuture<Boolean> exists(String collectionName) {
    return getConfig(collectionName).thenApply(Optional::isPresent);
  }

  /**
   * Check if a collection with this name exists.
   *
   * @param cls Class that represents an object in the collection.
   */
  public CompletableFuture<Boolean> exists(Class<? extends Record> cls) {
    return exists(CollectionDescriptor.ofClass(cls).collectionName());
  }
}
