package io.weaviate.client6.v1.api.collections.data;

import java.io.IOException;
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
}
