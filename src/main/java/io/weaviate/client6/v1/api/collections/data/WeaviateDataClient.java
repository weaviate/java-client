package io.weaviate.client6.v1.api.collections.data;

import java.io.IOException;
import java.util.function.Function;

import io.weaviate.client6.v1.api.collections.ObjectMetadata;
import io.weaviate.client6.v1.api.collections.WeaviateObject;
import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.orm.CollectionDescriptor;
import io.weaviate.client6.v1.internal.rest.RestTransport;

public class WeaviateDataClient<T> {
  private final RestTransport restTransport;
  private final CollectionDescriptor<T> collectionDescriptor;

  public WeaviateDataClient(CollectionDescriptor<T> collectionDescriptor, RestTransport restTransport) {
    this.restTransport = restTransport;
    this.collectionDescriptor = collectionDescriptor;
  }

  public WeaviateObject<T, Object, ObjectMetadata> insert(T properties) throws IOException {
    return insert(InsertObjectRequest.of(collectionDescriptor.name(), properties));
  }

  public WeaviateObject<T, Object, ObjectMetadata> insert(T properties,
      Function<InsertObjectRequest.Builder<T>, ObjectBuilder<InsertObjectRequest<T>>> fn)
      throws IOException {
    return insert(InsertObjectRequest.of(collectionDescriptor.name(), properties, fn));
  }

  public WeaviateObject<T, Object, ObjectMetadata> insert(InsertObjectRequest<T> request) throws IOException {
    return this.restTransport.performRequest(request, InsertObjectRequest.endpoint(collectionDescriptor));
  }

  public void delete(String uuid) throws IOException {
    this.restTransport.performRequest(new DeleteObjectRequest(collectionDescriptor.name(), uuid),
        DeleteObjectRequest._ENDPOINT);
  }

  public void referenceAdd(String fromUuid, String fromProperty, Reference reference) throws IOException {
    for (var uuid : reference.uuids()) {
      var singleRef = new Reference(reference.collection(), uuid);
      this.restTransport.performRequest(new ReferenceAddRequest(fromUuid, fromProperty, singleRef),
          ReferenceAddRequest.endpoint(collectionDescriptor));
    }
  }

  public void referenceDelete(String fromUuid, String fromProperty, Reference reference) throws IOException {
    for (var uuid : reference.uuids()) {
      var singleRef = new Reference(reference.collection(), uuid);
      this.restTransport.performRequest(new ReferenceDeleteRequest(fromUuid, fromProperty, singleRef),
          ReferenceDeleteRequest.endpoint(collectionDescriptor));
    }
  }

  public void referenceReplace(String fromUuid, String fromProperty, Reference reference) throws IOException {
    for (var uuid : reference.uuids()) {
      var singleRef = new Reference(reference.collection(), uuid);
      this.restTransport.performRequest(new ReferenceReplaceRequest(fromUuid, fromProperty, singleRef),
          ReferenceReplaceRequest.endpoint(collectionDescriptor));
    }
  }
}
