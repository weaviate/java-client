package io.weaviate.client6.v1.api.collections.data;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import io.weaviate.client6.v1.api.collections.ObjectMetadata;
import io.weaviate.client6.v1.api.collections.WeaviateObject;
import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.orm.CollectionDescriptor;
import io.weaviate.client6.v1.internal.rest.RestTransport;

public class WeaviateDataClientAsync<T> {
  private final RestTransport restTransport;
  private final CollectionDescriptor<T> collectionDescriptor;

  public WeaviateDataClientAsync(CollectionDescriptor<T> collectionDescriptor, RestTransport restTransport) {
    this.restTransport = restTransport;
    this.collectionDescriptor = collectionDescriptor;
  }

  public CompletableFuture<WeaviateObject<T, Object, ObjectMetadata>> insert(T properties) throws IOException {
    return insert(InsertObjectRequest.of(collectionDescriptor.name(), properties));
  }

  public CompletableFuture<WeaviateObject<T, Object, ObjectMetadata>> insert(T properties,
      Function<InsertObjectRequest.Builder<T>, ObjectBuilder<InsertObjectRequest<T>>> fn)
      throws IOException {
    return insert(InsertObjectRequest.of(collectionDescriptor.name(), properties, fn));
  }

  public CompletableFuture<WeaviateObject<T, Object, ObjectMetadata>> insert(InsertObjectRequest<T> request)
      throws IOException {
    return this.restTransport.performRequestAsync(request, InsertObjectRequest.endpoint(collectionDescriptor));
  }

  public CompletableFuture<Void> delete(String uuid) {
    return this.restTransport.performRequestAsync(new DeleteObjectRequest(collectionDescriptor.name(), uuid),
        DeleteObjectRequest._ENDPOINT);
  }

  public CompletableFuture<Void> referenceAdd(String fromUuid, String fromProperty, Reference reference) {
    return forEachAsync(reference.uuids(), uuid -> {
      var singleRef = new Reference(reference.collection(), (String) uuid);
      return this.restTransport.performRequestAsync(new ReferenceAddRequest(fromUuid, fromProperty, singleRef),
          ReferenceAddRequest.endpoint(collectionDescriptor));
    });
  }

  public CompletableFuture<Void> referenceDelete(String fromUuid, String fromProperty, Reference reference) {
    return forEachAsync(reference.uuids(), uuid -> {
      var singleRef = new Reference(reference.collection(), (String) uuid);
      return this.restTransport.performRequestAsync(new ReferenceDeleteRequest(fromUuid, fromProperty, singleRef),
          ReferenceDeleteRequest.endpoint(collectionDescriptor));
    });
  }

  public CompletableFuture<Void> referenceReplace(String fromUuid, String fromProperty, Reference reference) {
    return forEachAsync(reference.uuids(), uuid -> {
      var singleRef = new Reference(reference.collection(), (String) uuid);
      return this.restTransport.performRequestAsync(new ReferenceReplaceRequest(fromUuid, fromProperty, singleRef),
          ReferenceReplaceRequest.endpoint(collectionDescriptor));
    });
  }

  /**
   * Spawn execution {@code fn} for each of the {@code elements} and return a
   * flattened {@link CompletableFuture#allOf}.
   *
   * <p>
   * Usage:
   *
   * <pre>{@code
   *  // With elements immediately available
   *  forEachAsync(myElements, element -> doNetworkIo(element));
   *
   *  // Chain to another CompletableFuture
   *  fetch(request).thenCompose(elements -> forEachAsync(...));
   * }</pre>
   */
  private static <T> CompletableFuture<Void> forEachAsync(Collection<T> elements,
      Function<T, CompletableFuture<?>> fn) {
    var futures = elements.stream().map(el -> fn.apply(el))
        .toArray(CompletableFuture[]::new);
    return CompletableFuture.allOf(futures);
  }
}
