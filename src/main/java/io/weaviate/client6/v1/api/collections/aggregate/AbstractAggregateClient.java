package io.weaviate.client6.v1.api.collections.aggregate;

import java.util.List;
import java.util.function.Function;

import io.weaviate.client6.v1.api.collections.query.NearImage;
import io.weaviate.client6.v1.api.collections.query.NearText;
import io.weaviate.client6.v1.api.collections.query.NearVector;
import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.grpc.GrpcTransport;
import io.weaviate.client6.v1.internal.orm.CollectionDescriptor;

abstract class AbstractAggregateClient<ResponseT, GroupedResponseT> {
  protected final CollectionDescriptor<?> collection;
  protected final GrpcTransport transport;

  AbstractAggregateClient(CollectionDescriptor<?> collection, GrpcTransport transport) {
    this.transport = transport;
    this.collection = collection;
  }

  protected abstract ResponseT performRequest(Aggregation aggregation);

  protected abstract GroupedResponseT performRequest(Aggregation aggregation, GroupBy groupBy);

  // OverAll ------------------------------------------------------------------

  public ResponseT overAll(Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn) {
    return performRequest(Aggregation.of(fn));
  }

  public GroupedResponseT overAll(Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn, GroupBy groupBy) {
    return performRequest(Aggregation.of(fn), groupBy);
  }

  // NearVector ---------------------------------------------------------------

  public ResponseT nearVector(Float[] vector, Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn) {
    return nearVector(NearVector.of(vector), fn);
  }

  public ResponseT nearVector(Float[] vector, Function<NearVector.Builder, ObjectBuilder<NearVector>> nv,
      Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn) {
    return nearVector(NearVector.of(vector, nv), fn);
  }

  public ResponseT nearVector(NearVector filter, Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn) {
    return performRequest(Aggregation.of(filter, fn));
  }

  public GroupedResponseT nearVector(Float[] vector, Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn,
      GroupBy groupBy) {
    return nearVector(NearVector.of(vector), fn, groupBy);
  }

  public GroupedResponseT nearVector(Float[] vector, Function<NearVector.Builder, ObjectBuilder<NearVector>> nv,
      Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn, GroupBy groupBy) {
    return nearVector(NearVector.of(vector, nv), fn, groupBy);
  }

  public GroupedResponseT nearVector(NearVector filter, Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn,
      GroupBy groupBy) {
    return performRequest(Aggregation.of(filter, fn), groupBy);
  }

  // NearText -----------------------------------------------------------------

  public ResponseT nearText(String text, Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn) {
    return nearText(NearText.of(text), fn);
  }

  public ResponseT nearText(List<String> concepts, Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn) {
    return nearText(NearText.of(concepts), fn);
  }

  public ResponseT nearText(String text, Function<NearText.Builder, ObjectBuilder<NearText>> nt,
      Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn) {
    return nearText(NearText.of(text, nt), fn);
  }

  public ResponseT nearText(List<String> concepts, Function<NearText.Builder, ObjectBuilder<NearText>> nt,
      Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn) {
    return nearText(NearText.of(concepts, nt), fn);
  }

  public ResponseT nearText(NearText filter, Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn) {
    return performRequest(Aggregation.of(filter, fn));
  }

  public GroupedResponseT nearText(String text, Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn,
      GroupBy groupBy) {
    return nearText(NearText.of(text), fn, groupBy);
  }

  public GroupedResponseT nearText(List<String> concepts, Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn,
      GroupBy groupBy) {
    return nearText(NearText.of(concepts), fn, groupBy);
  }

  public GroupedResponseT nearText(String text, Function<NearText.Builder, ObjectBuilder<NearText>> nt,
      Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn, GroupBy groupBy) {
    return nearText(NearText.of(text, nt), fn, groupBy);
  }

  public GroupedResponseT nearText(List<String> concepts, Function<NearText.Builder, ObjectBuilder<NearText>> nt,
      Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn, GroupBy groupBy) {
    return nearText(NearText.of(concepts, nt), fn, groupBy);
  }

  public GroupedResponseT nearText(NearText filter, Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn,
      GroupBy groupBy) {
    return performRequest(Aggregation.of(filter, fn), groupBy);
  }

  // NearImage ----------------------------------------------------------------

  public ResponseT nearImage(String image, Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn) {
    return nearImage(NearImage.of(image), fn);
  }

  public ResponseT nearImage(String image, Function<NearImage.Builder, ObjectBuilder<NearImage>> nv,
      Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn) {
    return nearImage(NearImage.of(image, nv), fn);
  }

  public ResponseT nearImage(NearImage filter, Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn) {
    return performRequest(Aggregation.of(filter, fn));
  }

  public GroupedResponseT nearImage(String image, Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn,
      GroupBy groupBy) {
    return nearImage(NearImage.of(image), fn, groupBy);
  }

  public GroupedResponseT nearImage(String image, Function<NearImage.Builder, ObjectBuilder<NearImage>> nv,
      Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn, GroupBy groupBy) {
    return nearImage(NearImage.of(image, nv), fn, groupBy);
  }

  public GroupedResponseT nearImage(NearImage filter, Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn,
      GroupBy groupBy) {
    return performRequest(Aggregation.of(filter, fn), groupBy);
  }
}
