package io.weaviate.client6.v1.api.collections.data;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import io.weaviate.client6.v1.api.collections.CollectionHandleDefaults;
import io.weaviate.client6.v1.api.collections.ObjectMetadata;
import io.weaviate.client6.v1.api.collections.WeaviateObject;
import io.weaviate.client6.v1.api.collections.query.WeaviateQueryClientAsync;
import io.weaviate.client6.v1.api.collections.query.Filter;
import io.weaviate.client6.v1.api.collections.query.FilterOperand;
import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.grpc.GrpcTransport;
import io.weaviate.client6.v1.internal.orm.CollectionDescriptor;
import io.weaviate.client6.v1.internal.rest.RestTransport;

public class WeaviateDataClientAsync<PropertiesT> {
  private final RestTransport restTransport;
  private final GrpcTransport grpcTransport;
  private final CollectionDescriptor<PropertiesT> collection;

  private final WeaviateQueryClientAsync<PropertiesT> query;
  private final CollectionHandleDefaults defaults;

  public WeaviateDataClientAsync(
      CollectionDescriptor<PropertiesT> collectionDescriptor,
      RestTransport restTransport,
      GrpcTransport grpcTransport,
      CollectionHandleDefaults defaults) {
    this.restTransport = restTransport;
    this.grpcTransport = grpcTransport;
    this.collection = collectionDescriptor;
    this.query = new WeaviateQueryClientAsync<>(collectionDescriptor, grpcTransport, defaults);
    this.defaults = defaults;
  }

  /** Copy constructor that updates the {@link #query} to use new defaults. */
  public WeaviateDataClientAsync(WeaviateDataClientAsync<PropertiesT> c, CollectionHandleDefaults defaults) {
    this.restTransport = c.restTransport;
    this.grpcTransport = c.grpcTransport;
    this.collection = c.collection;
    this.query = new WeaviateQueryClientAsync<>(collection, grpcTransport, defaults);
    this.defaults = defaults;
  }

  public CompletableFuture<WeaviateObject<PropertiesT, Object, ObjectMetadata>> insert(PropertiesT properties) {
    return insert(InsertObjectRequest.of(collection.collectionName(), properties));
  }

  public CompletableFuture<WeaviateObject<PropertiesT, Object, ObjectMetadata>> insert(PropertiesT properties,
      Function<InsertObjectRequest.Builder<PropertiesT>, ObjectBuilder<InsertObjectRequest<PropertiesT>>> fn) {
    return insert(InsertObjectRequest.of(collection.collectionName(), properties, fn));
  }

  public CompletableFuture<WeaviateObject<PropertiesT, Object, ObjectMetadata>> insert(
      InsertObjectRequest<PropertiesT> request) {
    return this.restTransport.performRequestAsync(request, InsertObjectRequest.endpoint(collection, defaults));
  }

  @SafeVarargs
  public final CompletableFuture<InsertManyResponse> insertMany(PropertiesT... objects) {
    return insertMany(InsertManyRequest.of(objects));
  }

  public CompletableFuture<InsertManyResponse> insertMany(
      List<WeaviateObject<PropertiesT, Reference, ObjectMetadata>> objects) {
    return insertMany(new InsertManyRequest<>(objects));
  }

  public CompletableFuture<InsertManyResponse> insertMany(InsertManyRequest<PropertiesT> request) {
    return this.grpcTransport.performRequestAsync(request,
        InsertManyRequest.rpc(request.objects(), collection, defaults));
  }

  public CompletableFuture<Boolean> exists(String uuid) {
    return this.query.byId(uuid).thenApply(Optional::isPresent);
  }

  public CompletableFuture<Void> update(String uuid,
      Function<UpdateObjectRequest.Builder<PropertiesT>, ObjectBuilder<UpdateObjectRequest<PropertiesT>>> fn) {
    return this.restTransport.performRequestAsync(UpdateObjectRequest.of(collection.collectionName(), uuid, fn),
        UpdateObjectRequest.endpoint(collection, defaults));
  }

  public CompletableFuture<Void> replace(String uuid,
      Function<ReplaceObjectRequest.Builder<PropertiesT>, ObjectBuilder<ReplaceObjectRequest<PropertiesT>>> fn) {
    return this.restTransport.performRequestAsync(ReplaceObjectRequest.of(collection.collectionName(), uuid, fn),
        ReplaceObjectRequest.endpoint(collection, defaults));
  }

  public CompletableFuture<Void> delete(String uuid) {
    return this.restTransport.performRequestAsync(new DeleteObjectRequest(uuid),
        DeleteObjectRequest.endpoint(collection, defaults));
  }

  public CompletableFuture<DeleteManyResponse> deleteMany(String... uuids) {
    var either = Arrays.stream(uuids)
        .map(uuid -> (FilterOperand) Filter.uuid().eq(uuid))
        .toList();
    return deleteMany(DeleteManyRequest.of(Filter.or(either)));
  }

  public CompletableFuture<DeleteManyResponse> deleteMany(Filter filters) {
    return deleteMany(DeleteManyRequest.of(filters));
  }

  public CompletableFuture<DeleteManyResponse> deleteMany(Filter filters,
      Function<DeleteManyRequest.Builder, ObjectBuilder<DeleteManyRequest>> fn) {
    return deleteMany(DeleteManyRequest.of(filters, fn));
  }

  public CompletableFuture<DeleteManyResponse> deleteMany(DeleteManyRequest request) {
    return this.grpcTransport.performRequestAsync(request, DeleteManyRequest.rpc(collection, defaults));
  }

  public CompletableFuture<Void> referenceAdd(String fromUuid, String fromProperty, Reference reference) {
    return forEachAsync(reference.uuids(), uuid -> {
      var singleRef = new Reference(reference.collection(), (String) uuid);
      return this.restTransport.performRequestAsync(new ReferenceAddRequest(fromUuid, fromProperty, singleRef),
          ReferenceAddRequest.endpoint(collection, defaults));
    });
  }

  public CompletableFuture<ReferenceAddManyResponse> referenceAddMany(BatchReference... references) {
    return referenceAddMany(Arrays.asList(references));
  }

  public CompletableFuture<ReferenceAddManyResponse> referenceAddMany(List<BatchReference> references) {
    return this.restTransport.performRequestAsync(new ReferenceAddManyRequest(references),
        ReferenceAddManyRequest.endpoint(references, defaults));
  }

  public CompletableFuture<Void> referenceDelete(String fromUuid, String fromProperty, Reference reference) {
    return forEachAsync(reference.uuids(), uuid -> {
      var singleRef = new Reference(reference.collection(), (String) uuid);
      return this.restTransport.performRequestAsync(new ReferenceDeleteRequest(fromUuid, fromProperty, singleRef),
          ReferenceDeleteRequest.endpoint(collection, defaults));
    });
  }

  public CompletableFuture<Void> referenceReplace(String fromUuid, String fromProperty, Reference reference) {
    return forEachAsync(reference.uuids(), uuid -> {
      var singleRef = new Reference(reference.collection(), (String) uuid);
      return this.restTransport.performRequestAsync(new ReferenceReplaceRequest(fromUuid, fromProperty, singleRef),
          ReferenceReplaceRequest.endpoint(collection, defaults));
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
