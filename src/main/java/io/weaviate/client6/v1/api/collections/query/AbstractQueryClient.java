package io.weaviate.client6.v1.api.collections.query;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import io.weaviate.client6.v1.api.collections.CollectionHandleDefaults;
import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.grpc.GrpcTransport;
import io.weaviate.client6.v1.internal.orm.CollectionDescriptor;

abstract class AbstractQueryClient<PropertiesT, SingleT, ResponseT, GroupedResponseT> {
  protected final CollectionDescriptor<PropertiesT> collection;
  protected final GrpcTransport grpcTransport;

  protected final CollectionHandleDefaults defaults;

  AbstractQueryClient(CollectionDescriptor<PropertiesT> collection, GrpcTransport grpcTransport,
      CollectionHandleDefaults defaults) {
    this.collection = collection;
    this.grpcTransport = grpcTransport;
    this.defaults = defaults;
  }

  /** Copy constructor that sets new defaults. */
  AbstractQueryClient(AbstractQueryClient<PropertiesT, SingleT, ResponseT, GroupedResponseT> qc,
      CollectionHandleDefaults defaults) {
    this(qc.collection, qc.grpcTransport, defaults);
  }

  protected abstract SingleT byId(ById byId);

  protected abstract ResponseT performRequest(QueryOperator operator);

  protected abstract GroupedResponseT performRequest(QueryOperator operator, GroupBy groupBy);

  // Fetch by ID --------------------------------------------------------------

  public SingleT byId(String uuid) {
    return byId(ById.of(uuid));
  }

  public SingleT byId(String uuid, Function<ById.Builder, ObjectBuilder<ById>> fn) {
    // Collection handle defaults (consistencyLevel / tenant) are irrelevant for
    // by-ID lookup. Do not `applyDefaults` to `fn`.
    return byId(ById.of(uuid, fn));
  }

  /**
   * Retrieve the first result from query response if any.
   *
   * @param objects A list of objects, normally {@link QueryResponse#objects}.
   * @return An object from the list or empty {@link Optional}.
   */
  protected final <T> Optional<T> optionalFirst(List<T> objects) {
    return objects.isEmpty() ? Optional.empty() : Optional.ofNullable(objects.get(0));
  }

  // Object queries -----------------------------------------------------------

  public ResponseT fetchObjects(Function<FetchObjects.Builder, ObjectBuilder<FetchObjects>> fn) {
    return fetchObjects(FetchObjects.of(fn));
  }

  public ResponseT fetchObjects(FetchObjects query) {
    return performRequest(query);
  }

  public GroupedResponseT fetchObjects(Function<FetchObjects.Builder, ObjectBuilder<FetchObjects>> fn,
      GroupBy groupBy) {
    return fetchObjects(FetchObjects.of(fn), groupBy);
  }

  public GroupedResponseT fetchObjects(FetchObjects query, GroupBy groupBy) {
    return performRequest(query, groupBy);
  }

  // BM25 queries -------------------------------------------------------------

  public ResponseT bm25(String query) {
    return bm25(Bm25.of(query));
  }

  public ResponseT bm25(String query, Function<Bm25.Builder, ObjectBuilder<Bm25>> fn) {
    return bm25(Bm25.of(query, fn));
  }

  public ResponseT bm25(Bm25 query) {
    return performRequest(query);
  }

  public GroupedResponseT bm25(String query, GroupBy groupBy) {
    return bm25(Bm25.of(query), groupBy);
  }

  public GroupedResponseT bm25(String query, Function<Bm25.Builder, ObjectBuilder<Bm25>> fn, GroupBy groupBy) {
    return bm25(Bm25.of(query, fn), groupBy);
  }

  public GroupedResponseT bm25(Bm25 query, GroupBy groupBy) {
    return performRequest(query, groupBy);
  }

  // Hybrid queries -----------------------------------------------------------

  public ResponseT hybrid(String query) {
    return hybrid(Hybrid.of(query));
  }

  public ResponseT hybrid(String query, Function<Hybrid.Builder, ObjectBuilder<Hybrid>> fn) {
    return hybrid(Hybrid.of(query, fn));
  }

  public ResponseT hybrid(Hybrid query) {
    return performRequest(query);
  }

  public GroupedResponseT hybrid(String query, GroupBy groupBy) {
    return hybrid(Hybrid.of(query), groupBy);
  }

  public GroupedResponseT hybrid(String query, Function<Hybrid.Builder, ObjectBuilder<Hybrid>> fn, GroupBy groupBy) {
    return hybrid(Hybrid.of(query, fn), groupBy);
  }

  public GroupedResponseT hybrid(Hybrid query, GroupBy groupBy) {
    return performRequest(query, groupBy);
  }

  // NearVector queries -------------------------------------------------------

  public ResponseT nearVector(float[] vector) {
    return nearVector(NearVector.of(vector));
  }

  public ResponseT nearVector(float[] vector, Function<NearVector.Builder, ObjectBuilder<NearVector>> fn) {
    return nearVector(NearVector.of(vector, fn));
  }

  public ResponseT nearVector(NearVector query) {
    return performRequest(query);
  }

  public GroupedResponseT nearVector(float[] vector, GroupBy groupBy) {
    return nearVector(NearVector.of(vector), groupBy);
  }

  public GroupedResponseT nearVector(float[] vector, Function<NearVector.Builder, ObjectBuilder<NearVector>> fn,
      GroupBy groupBy) {
    return nearVector(NearVector.of(vector, fn), groupBy);
  }

  public GroupedResponseT nearVector(NearVector query, GroupBy groupBy) {
    return performRequest(query, groupBy);
  }

  // NearObject queries -------------------------------------------------------

  public ResponseT nearObject(String uuid) {
    return nearObject(NearObject.of(uuid));
  }

  public ResponseT nearObject(String uuid, Function<NearObject.Builder, ObjectBuilder<NearObject>> fn) {
    return nearObject(NearObject.of(uuid, fn));
  }

  public ResponseT nearObject(NearObject query) {
    return performRequest(query);
  }

  public GroupedResponseT nearObject(String uuid, GroupBy groupBy) {
    return nearObject(NearObject.of(uuid), groupBy);
  }

  public GroupedResponseT nearObject(String uuid, Function<NearObject.Builder, ObjectBuilder<NearObject>> fn,
      GroupBy groupBy) {
    return nearObject(NearObject.of(uuid, fn), groupBy);
  }

  public GroupedResponseT nearObject(NearObject query, GroupBy groupBy) {
    return performRequest(query, groupBy);
  }

  // NearText queries ---------------------------------------------------------

  public ResponseT nearText(String... text) {
    return nearText(NearText.of(text));
  }

  public ResponseT nearText(String text, Function<NearText.Builder, ObjectBuilder<NearText>> fn) {
    return nearText(NearText.of(text, fn));
  }

  public ResponseT nearText(List<String> text, Function<NearText.Builder, ObjectBuilder<NearText>> fn) {
    return nearText(NearText.of(text, fn));
  }

  public ResponseT nearText(NearText query) {
    return performRequest(query);
  }

  public GroupedResponseT nearText(String text, GroupBy groupBy) {
    return nearText(NearText.of(text), groupBy);
  }

  public GroupedResponseT nearText(List<String> text, GroupBy groupBy) {
    return nearText(NearText.of(text), groupBy);
  }

  public GroupedResponseT nearText(String text, Function<NearText.Builder, ObjectBuilder<NearText>> fn,
      GroupBy groupBy) {
    return nearText(NearText.of(text), groupBy);
  }

  public GroupedResponseT nearText(List<String> text, Function<NearText.Builder, ObjectBuilder<NearText>> fn,
      GroupBy groupBy) {
    return nearText(NearText.of(text), groupBy);
  }

  public GroupedResponseT nearText(NearText query, GroupBy groupBy) {
    return performRequest(query, groupBy);
  }

  // NearImage queries --------------------------------------------------------

  public ResponseT nearImage(String image) {
    return nearImage(NearImage.of(image));
  }

  public ResponseT nearImage(String image, Function<NearImage.Builder, ObjectBuilder<NearImage>> fn) {
    return nearImage(NearImage.of(image, fn));
  }

  public ResponseT nearImage(NearImage query) {
    return performRequest(query);
  }

  public GroupedResponseT nearImage(String image, GroupBy groupBy) {
    return nearImage(NearImage.of(image), groupBy);
  }

  public GroupedResponseT nearImage(String image, Function<NearImage.Builder, ObjectBuilder<NearImage>> fn,
      GroupBy groupBy) {
    return nearImage(NearImage.of(image, fn), groupBy);
  }

  public GroupedResponseT nearImage(NearImage query, GroupBy groupBy) {
    return performRequest(query, groupBy);
  }

  // NearAudio queries --------------------------------------------------------

  public ResponseT nearAudio(String audio) {
    return nearAudio(NearAudio.of(audio));
  }

  public ResponseT nearAudio(String audio, Function<NearAudio.Builder, ObjectBuilder<NearAudio>> fn) {
    return nearAudio(NearAudio.of(audio, fn));
  }

  public ResponseT nearAudio(NearAudio query) {
    return performRequest(query);
  }

  public GroupedResponseT nearAudio(String audio, GroupBy groupBy) {
    return nearAudio(NearAudio.of(audio), groupBy);
  }

  public GroupedResponseT nearAudio(String audio, Function<NearAudio.Builder, ObjectBuilder<NearAudio>> fn,
      GroupBy groupBy) {
    return nearAudio(NearAudio.of(audio, fn), groupBy);
  }

  public GroupedResponseT nearAudio(NearAudio query, GroupBy groupBy) {
    return performRequest(query, groupBy);
  }

  // NearVideo queries --------------------------------------------------------

  public ResponseT nearVideo(String video) {
    return nearVideo(NearVideo.of(video));
  }

  public ResponseT nearVideo(String video, Function<NearVideo.Builder, ObjectBuilder<NearVideo>> fn) {
    return nearVideo(NearVideo.of(video, fn));
  }

  public ResponseT nearVideo(NearVideo query) {
    return performRequest(query);
  }

  public GroupedResponseT nearVideo(String video, GroupBy groupBy) {
    return nearVideo(NearVideo.of(video), groupBy);
  }

  public GroupedResponseT nearVideo(String video, Function<NearVideo.Builder, ObjectBuilder<NearVideo>> fn,
      GroupBy groupBy) {
    return nearVideo(NearVideo.of(video, fn), groupBy);
  }

  public GroupedResponseT nearVideo(NearVideo query, GroupBy groupBy) {
    return performRequest(query, groupBy);
  }

  // NearThermal queries ------------------------------------------------------

  public ResponseT nearThermal(String thermal) {
    return nearThermal(NearThermal.of(thermal));
  }

  public ResponseT nearThermal(String thermal, Function<NearThermal.Builder, ObjectBuilder<NearThermal>> fn) {
    return nearThermal(NearThermal.of(thermal, fn));
  }

  public ResponseT nearThermal(NearThermal query) {
    return performRequest(query);
  }

  public GroupedResponseT nearThermal(String thermal, GroupBy groupBy) {
    return nearThermal(NearThermal.of(thermal), groupBy);
  }

  public GroupedResponseT nearThermal(String thermal, Function<NearThermal.Builder, ObjectBuilder<NearThermal>> fn,
      GroupBy groupBy) {
    return nearThermal(NearThermal.of(thermal, fn), groupBy);
  }

  public GroupedResponseT nearThermal(NearThermal query, GroupBy groupBy) {
    return performRequest(query, groupBy);
  }

  // NearDepth queries --------------------------------------------------------

  public ResponseT nearDepth(String depth) {
    return nearDepth(NearDepth.of(depth));
  }

  public ResponseT nearDepth(String depth, Function<NearDepth.Builder, ObjectBuilder<NearDepth>> fn) {
    return nearDepth(NearDepth.of(depth, fn));
  }

  public ResponseT nearDepth(NearDepth query) {
    return performRequest(query);
  }

  public GroupedResponseT nearDepth(String depth, GroupBy groupBy) {
    return nearDepth(NearDepth.of(depth), groupBy);
  }

  public GroupedResponseT nearDepth(String depth, Function<NearDepth.Builder, ObjectBuilder<NearDepth>> fn,
      GroupBy groupBy) {
    return nearDepth(NearDepth.of(depth, fn), groupBy);
  }

  public GroupedResponseT nearDepth(NearDepth query, GroupBy groupBy) {
    return performRequest(query, groupBy);
  }

  // NearImu queries ----------------------------------------------------------

  public ResponseT nearImu(String imu) {
    return nearImu(NearImu.of(imu));
  }

  public ResponseT nearImu(String imu, Function<NearImu.Builder, ObjectBuilder<NearImu>> fn) {
    return nearImu(NearImu.of(imu, fn));
  }

  public ResponseT nearImu(NearImu query) {
    return performRequest(query);
  }

  public GroupedResponseT nearImu(String imu, GroupBy groupBy) {
    return nearImu(NearImu.of(imu), groupBy);
  }

  public GroupedResponseT nearImu(String imu, Function<NearImu.Builder, ObjectBuilder<NearImu>> fn,
      GroupBy groupBy) {
    return nearImu(NearImu.of(imu, fn), groupBy);
  }

  public GroupedResponseT nearImu(NearImu query, GroupBy groupBy) {
    return performRequest(query, groupBy);
  }
}
