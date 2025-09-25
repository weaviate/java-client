package io.weaviate.client6.v1.api.collections.query;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import io.weaviate.client6.v1.api.collections.CollectionHandleDefaults;
import io.weaviate.client6.v1.api.collections.WeaviateObject;
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
  AbstractQueryClient(
      AbstractQueryClient<PropertiesT, SingleT, ResponseT, GroupedResponseT> c,
      CollectionHandleDefaults defaults) {
    this(c.collection, c.grpcTransport, defaults);
  }

  protected abstract SingleT byId(ById byId);

  protected abstract ResponseT performRequest(QueryOperator operator);

  protected abstract GroupedResponseT performRequest(QueryOperator operator, GroupBy groupBy);

  // Fetch by ID --------------------------------------------------------------

  /** Retrieve the object by its UUID. */
  public SingleT byId(String uuid) {
    return byId(ById.of(uuid));
  }

  /**
   * Retrieve the object by its UUID.
   *
   * @param fn Lambda expression for optional parameters.
   * @return An object from the list or empty {@link Optional}.
   */
  public SingleT byId(String uuid, Function<ById.Builder, ObjectBuilder<ById>> fn) {
    // Collection handle defaults (consistencyLevel / tenant) are irrelevant for
    // by-ID lookup. Do not `applyDefaults` to `fn`.
    return byId(ById.of(uuid, fn));
  }

  /**
   * Retrieve the first result from query response if any.
   *
   * @param response Query response.
   * @return An object from the list or empty {@link Optional}.
   */
  protected final <T> Optional<WeaviateObject<T, Object, QueryMetadata>> optionalFirst(QueryResponse<T> response) {
    return response == null || response.objects().isEmpty()
        ? Optional.empty()
        : Optional.ofNullable(response.objects().get(0));

  }

  // Object queries -----------------------------------------------------------

  /**
   * Retrieve objects without applying a Vector Search or Keyword Search filter.
   *
   * @param fn Lambda expression for optional parameters.
   */
  public ResponseT fetchObjects(Function<FetchObjects.Builder, ObjectBuilder<FetchObjects>> fn) {
    return fetchObjects(FetchObjects.of(fn));
  }

  /**
   * Retrieve objects without applying a Vector Search or Keyword Search filter.
   *
   * @param query FetchObjects query.
   */
  public ResponseT fetchObjects(FetchObjects query) {
    return performRequest(query);
  }

  /**
   * Retrieve objects without applying a Vector Search or Keyword Search filter.
   *
   *
   * @param fn      Lambda expression for optional parameters.
   * @param groupBy Group-by clause.
   * @return Grouped query result.
   *
   * @see GroupBy
   * @see QueryResponseGrouped
   */
  public GroupedResponseT fetchObjects(Function<FetchObjects.Builder, ObjectBuilder<FetchObjects>> fn,
      GroupBy groupBy) {
    return fetchObjects(FetchObjects.of(fn), groupBy);
  }

  /**
   * Retrieve objects without applying a Vector Search or Keyword Search filter.
   *
   * @param query   FetchObjects query.
   * @param groupBy Group-by clause.
   * @return Grouped query result.
   *
   * @see GroupBy
   * @see QueryResponseGrouped
   */
  public GroupedResponseT fetchObjects(FetchObjects query, GroupBy groupBy) {
    return performRequest(query, groupBy);
  }

  // BM25 queries -------------------------------------------------------------

  /**
   * Query collection objects using keyword (BM25) search.
   *
   * @param query Query string.
   */
  public ResponseT bm25(String query) {
    return bm25(Bm25.of(query));
  }

  /**
   * Query collection objects using keyword (BM25) search.
   *
   * @param query Query string.
   * @param fn    Lambda expression for optional parameters.
   */
  public ResponseT bm25(String query, Function<Bm25.Builder, ObjectBuilder<Bm25>> fn) {
    return bm25(Bm25.of(query, fn));
  }

  /**
   * Query collection objects using keyword (BM25) search.
   *
   * @param query BM25 query request.
   */
  public ResponseT bm25(Bm25 query) {
    return performRequest(query);
  }

  /**
   * Query collection objects using keyword (BM25) search.
   *
   * @param query   Query string.
   * @param groupBy Group-by clause.
   * @return Grouped query result.
   *
   * @see GroupBy
   * @see QueryResponseGrouped
   */
  public GroupedResponseT bm25(String query, GroupBy groupBy) {
    return bm25(Bm25.of(query), groupBy);
  }

  /**
   * Query collection objects using keyword (BM25) search.
   *
   * @param query   Query string.
   * @param fn      Lambda expression for optional parameters.
   * @param groupBy Group-by clause.
   * @return Grouped query result.
   *
   * @see GroupBy
   * @see QueryResponseGrouped
   */
  public GroupedResponseT bm25(String query, Function<Bm25.Builder, ObjectBuilder<Bm25>> fn, GroupBy groupBy) {
    return bm25(Bm25.of(query, fn), groupBy);
  }

  /**
   * Query collection objects using keyword (BM25) search.
   *
   * @param query   BM25 query request.
   * @param groupBy Group-by clause.
   * @return Grouped query result.
   *
   * @see GroupBy
   * @see QueryResponseGrouped
   */
  public GroupedResponseT bm25(Bm25 query, GroupBy groupBy) {
    return performRequest(query, groupBy);
  }

  // Hybrid queries -----------------------------------------------------------

  /**
   * Query collection objects using hybrid search.
   *
   * @param query Query string.
   */
  public ResponseT hybrid(String query) {
    return hybrid(Hybrid.of(query));
  }

  /**
   * Query collection objects using hybrid search.
   *
   * @param query Query string.
   * @param fn    Lambda expression for optional parameters.
   */
  public ResponseT hybrid(String query, Function<Hybrid.Builder, ObjectBuilder<Hybrid>> fn) {
    return hybrid(Hybrid.of(query, fn));
  }

  /**
   * Query collection objects using hybrid search.
   *
   * @param query Hybrid query request.
   */
  public ResponseT hybrid(Hybrid query) {
    return performRequest(query);
  }

  /**
   * Query collection objects using hybrid search.
   *
   * @param query   Query string.
   * @param groupBy Group-by clause.
   * @return Grouped query result.
   *
   * @see GroupBy
   * @see QueryResponseGrouped
   */
  public GroupedResponseT hybrid(String query, GroupBy groupBy) {
    return hybrid(Hybrid.of(query), groupBy);
  }

  /**
   * Query collection objects using hybrid search.
   *
   * @param query   Query string.
   * @param fn      Lambda expression for optional parameters.
   * @param groupBy Group-by clause.
   * @return Grouped query result.
   *
   * @see GroupBy
   * @see QueryResponseGrouped
   */
  public GroupedResponseT hybrid(String query, Function<Hybrid.Builder, ObjectBuilder<Hybrid>> fn, GroupBy groupBy) {
    return hybrid(Hybrid.of(query, fn), groupBy);
  }

  /**
   * Query collection objects using hybrid search.
   *
   * @param query   Query string.
   * @param groupBy Group-by clause.
   * @return Grouped query result.
   *
   * @see GroupBy
   * @see QueryResponseGrouped
   */
  public GroupedResponseT hybrid(Hybrid query, GroupBy groupBy) {
    return performRequest(query, groupBy);
  }

  // NearVector queries -------------------------------------------------------

  /**
   * Query collection objects using near vector search.
   *
   * @param vector Query vector.
   */
  public ResponseT nearVector(float[] vector) {
    return nearVector(NearVector.of(vector));
  }

  /**
   * Query collection objects using near vector search.
   *
   * @param vector Query vector.
   * @param fn     Lambda expression for optional parameters.
   */
  public ResponseT nearVector(float[] vector, Function<NearVector.Builder, ObjectBuilder<NearVector>> fn) {
    return nearVector(NearVector.of(vector, fn));
  }

  /**
   * Query collection objects using near vector search.
   *
   * @param query Near vector query request.
   */
  public ResponseT nearVector(NearVector query) {
    return performRequest(query);
  }

  /**
   * Query collection objects using near vector search.
   *
   * @param vector  Query vector.
   * @param groupBy Group-by clause.
   * @return Grouped query result.
   *
   * @see GroupBy
   * @see QueryResponseGrouped
   */
  public GroupedResponseT nearVector(float[] vector, GroupBy groupBy) {
    return nearVector(NearVector.of(vector), groupBy);
  }

  /**
   * Query collection objects using near vector search.
   *
   * @param vector  Query vector.
   * @param fn      Lambda expression for optional parameters.
   * @param groupBy Group-by clause.
   * @return Grouped query result.
   *
   * @see GroupBy
   * @see QueryResponseGrouped
   */
  public GroupedResponseT nearVector(float[] vector, Function<NearVector.Builder, ObjectBuilder<NearVector>> fn,
      GroupBy groupBy) {
    return nearVector(NearVector.of(vector, fn), groupBy);
  }

  /**
   * Query collection objects using near vector search.
   *
   * @param query   Near vector query request.
   * @param groupBy Group-by clause.
   * @return Grouped query result.
   *
   * @see GroupBy
   * @see QueryResponseGrouped
   */
  public GroupedResponseT nearVector(NearVector query, GroupBy groupBy) {
    return performRequest(query, groupBy);
  }

  // NearObject queries -------------------------------------------------------

  /**
   * Query collection objects using near object search.
   *
   * @param uuid Query object UUID.
   */
  public ResponseT nearObject(String uuid) {
    return nearObject(NearObject.of(uuid));
  }

  /**
   * Query collection objects using near object search.
   *
   * @param uuid Query object UUID.
   * @param fn   Lambda expression for optional parameters.
   */
  public ResponseT nearObject(String uuid, Function<NearObject.Builder, ObjectBuilder<NearObject>> fn) {
    return nearObject(NearObject.of(uuid, fn));
  }

  /**
   * Query collection objects using near object search.
   *
   * @param query Near object query request.
   */
  public ResponseT nearObject(NearObject query) {
    return performRequest(query);
  }

  /**
   * Query collection objects using near object search.
   *
   * @param uuid    Query object UUID.
   * @param groupBy Group-by clause.
   * @return Grouped query result.
   *
   * @see GroupBy
   * @see QueryResponseGrouped
   */
  public GroupedResponseT nearObject(String uuid, GroupBy groupBy) {
    return nearObject(NearObject.of(uuid), groupBy);
  }

  /**
   * Query collection objects using near object search.
   *
   * @param uuid    Query object UUID.
   * @param fn      Lambda expression for optional parameters.
   * @param groupBy Group-by clause.
   * @return Grouped query result.
   *
   * @see GroupBy
   * @see QueryResponseGrouped
   */
  public GroupedResponseT nearObject(String uuid, Function<NearObject.Builder, ObjectBuilder<NearObject>> fn,
      GroupBy groupBy) {
    return nearObject(NearObject.of(uuid, fn), groupBy);
  }

  /**
   * Query collection objects using near object search.
   *
   * @param query   Near object query request.
   * @param groupBy Group-by clause.
   * @return Grouped query result.
   *
   * @see GroupBy
   * @see QueryResponseGrouped
   */
  public GroupedResponseT nearObject(NearObject query, GroupBy groupBy) {
    return performRequest(query, groupBy);
  }

  // NearText queries ---------------------------------------------------------

  /**
   * Query collection objects using near text search.
   *
   * @param text Query concepts.
   */
  public ResponseT nearText(String... text) {
    return nearText(NearText.of(text));
  }

  /**
   * Query collection objects using near text search.
   *
   * @param text Query concepts.
   * @param fn   Lambda expression for optional parameters.
   */
  public ResponseT nearText(String text, Function<NearText.Builder, ObjectBuilder<NearText>> fn) {
    return nearText(NearText.of(text, fn));
  }

  /**
   * Query collection objects using near text search.
   *
   * @param text Query concepts.
   * @param fn   Lambda expression for optional parameters.
   */
  public ResponseT nearText(List<String> text, Function<NearText.Builder, ObjectBuilder<NearText>> fn) {
    return nearText(NearText.of(text, fn));
  }

  /**
   * Query collection objects using near text search.
   *
   * @param query Near text query request.
   */
  public ResponseT nearText(NearText query) {
    return performRequest(query);
  }

  /**
   * Query collection objects using near text search.
   *
   * @param text    Query concepts.
   * @param groupBy Group-by clause.
   * @return Grouped query result.
   *
   * @see GroupBy
   * @see QueryResponseGrouped
   */
  public GroupedResponseT nearText(String text, GroupBy groupBy) {
    return nearText(NearText.of(text), groupBy);
  }

  /**
   * Query collection objects using near text search.
   *
   * @param text    Query concepts.
   * @param groupBy Group-by clause.
   * @return Grouped query result.
   *
   * @see GroupBy
   * @see QueryResponseGrouped
   */
  public GroupedResponseT nearText(List<String> text, GroupBy groupBy) {
    return nearText(NearText.of(text), groupBy);
  }

  /**
   * Query collection objects using near text search.
   *
   * @param text    Query concepts.
   * @param fn      Lambda expression for optional parameters.
   * @param groupBy Group-by clause.
   * @return Grouped query result.
   *
   * @see GroupBy
   * @see QueryResponseGrouped
   */
  public GroupedResponseT nearText(String text, Function<NearText.Builder, ObjectBuilder<NearText>> fn,
      GroupBy groupBy) {
    return nearText(NearText.of(text), groupBy);
  }

  /**
   * Query collection objects using near text search.
   *
   * @param text    Query concepts.
   * @param fn      Lambda expression for optional parameters.
   * @param groupBy Group-by clause.
   * @return Grouped query result.
   *
   * @see GroupBy
   * @see QueryResponseGrouped
   */
  public GroupedResponseT nearText(List<String> text, Function<NearText.Builder, ObjectBuilder<NearText>> fn,
      GroupBy groupBy) {
    return nearText(NearText.of(text), groupBy);
  }

  /**
   * Query collection objects using near text search.
   *
   * @param query   Near text query request.
   * @param groupBy Group-by clause.
   * @return Grouped query result.
   *
   * @see GroupBy
   * @see QueryResponseGrouped
   */
  public GroupedResponseT nearText(NearText query, GroupBy groupBy) {
    return performRequest(query, groupBy);
  }

  // NearImage queries --------------------------------------------------------

  /**
   * Query collection objects using near image search.
   *
   * @param image Query image (base64-encoded).
   */
  public ResponseT nearImage(String image) {
    return nearImage(NearImage.of(image));
  }

  /**
   * Query collection objects using near image search.
   *
   * @param image Query image (base64-encoded).
   * @param fn    Lambda expression for optional parameters.
   */
  public ResponseT nearImage(String image, Function<NearImage.Builder, ObjectBuilder<NearImage>> fn) {
    return nearImage(NearImage.of(image, fn));
  }

  /**
   * Query collection objects using near image search.
   *
   * @param query Near image query request.
   */
  public ResponseT nearImage(NearImage query) {
    return performRequest(query);
  }

  /**
   * Query collection objects using near image search.
   *
   * @param image   Query image (base64-encoded).
   * @param groupBy Group-by clause.
   * @return Grouped query result.
   *
   * @see GroupBy
   * @see QueryResponseGrouped
   */
  public GroupedResponseT nearImage(String image, GroupBy groupBy) {
    return nearImage(NearImage.of(image), groupBy);
  }

  /**
   * Query collection objects using near image search.
   *
   * @param image   Query image (base64-encoded).
   * @param fn      Lambda expression for optional parameters.
   * @param groupBy Group-by clause.
   * @return Grouped query result.
   *
   * @see GroupBy
   * @see QueryResponseGrouped
   */
  public GroupedResponseT nearImage(String image, Function<NearImage.Builder, ObjectBuilder<NearImage>> fn,
      GroupBy groupBy) {
    return nearImage(NearImage.of(image, fn), groupBy);
  }

  /**
   * Query collection objects using near image search.
   *
   * @param query   Near image query request.
   * @param groupBy Group-by clause.
   * @return Grouped query result.
   *
   * @see GroupBy
   * @see QueryResponseGrouped
   */
  public GroupedResponseT nearImage(NearImage query, GroupBy groupBy) {
    return performRequest(query, groupBy);
  }

  // NearAudio queries --------------------------------------------------------

  /**
   * Query collection objects using near audio search.
   *
   * @param audio Query audio (base64-encoded).
   */
  public ResponseT nearAudio(String audio) {
    return nearAudio(NearAudio.of(audio));
  }

  /**
   * Query collection objects using near audio search.
   *
   * @param audio Query audio (base64-encoded).
   * @param fn    Lambda expression for optional parameters.
   */
  public ResponseT nearAudio(String audio, Function<NearAudio.Builder, ObjectBuilder<NearAudio>> fn) {
    return nearAudio(NearAudio.of(audio, fn));
  }

  /**
   * Query collection objects using near audio search.
   *
   * @param query Near audio query request.
   */
  public ResponseT nearAudio(NearAudio query) {
    return performRequest(query);
  }

  /**
   * Query collection objects using near audio search.
   *
   * @param audio   Query audio (base64-encoded).
   * @param groupBy Group-by clause.
   * @return Grouped query result.
   *
   * @see GroupBy
   * @see QueryResponseGrouped
   */
  public GroupedResponseT nearAudio(String audio, GroupBy groupBy) {
    return nearAudio(NearAudio.of(audio), groupBy);
  }

  /**
   * Query collection objects using near audio search.
   *
   * @param audio   Query audio (base64-encoded).
   * @param fn      Lambda expression for optional parameters.
   * @param groupBy Group-by clause.
   * @return Grouped query result.
   *
   * @see GroupBy
   * @see QueryResponseGrouped
   */

  public GroupedResponseT nearAudio(String audio, Function<NearAudio.Builder, ObjectBuilder<NearAudio>> fn,
      GroupBy groupBy) {
    return nearAudio(NearAudio.of(audio, fn), groupBy);
  }

  /**
   * Query collection objects using near audio search.
   *
   * @param query   Near audio query request.
   * @param groupBy Group-by clause.
   * @return Grouped query result.
   *
   * @see GroupBy
   * @see QueryResponseGrouped
   */
  public GroupedResponseT nearAudio(NearAudio query, GroupBy groupBy) {
    return performRequest(query, groupBy);
  }

  // NearVideo queries --------------------------------------------------------

  /**
   * Query collection objects using near video search.
   *
   * @param video Query video (base64-encoded).
   */
  public ResponseT nearVideo(String video) {
    return nearVideo(NearVideo.of(video));
  }

  /**
   * Query collection objects using near video search.
   *
   * @param video Query video (base64-encoded).
   * @param fn    Lambda expression for optional parameters.
   */
  public ResponseT nearVideo(String video, Function<NearVideo.Builder, ObjectBuilder<NearVideo>> fn) {
    return nearVideo(NearVideo.of(video, fn));
  }

  /**
   * Query collection objects using near video search.
   *
   * @param query Near video query request.
   */
  public ResponseT nearVideo(NearVideo query) {
    return performRequest(query);
  }

  /**
   * Query collection objects using near video search.
   *
   * @param video   Query video (base64-encoded).
   * @param groupBy Group-by clause.
   * @return Grouped query result.
   *
   * @see GroupBy
   * @see QueryResponseGrouped
   */
  public GroupedResponseT nearVideo(String video, GroupBy groupBy) {
    return nearVideo(NearVideo.of(video), groupBy);
  }

  /**
   * Query collection objects using near video search.
   *
   * @param video   Query video (base64-encoded).
   * @param fn      Lambda expression for optional parameters.
   * @param groupBy Group-by clause.
   * @return Grouped query result.
   *
   * @see GroupBy
   * @see QueryResponseGrouped
   */
  public GroupedResponseT nearVideo(String video, Function<NearVideo.Builder, ObjectBuilder<NearVideo>> fn,
      GroupBy groupBy) {
    return nearVideo(NearVideo.of(video, fn), groupBy);
  }

  /**
   * Query collection objects using near video search.
   *
   * @param query   Near video query request.
   * @param groupBy Group-by clause.
   * @return Grouped query result.
   *
   * @see GroupBy
   * @see QueryResponseGrouped
   */
  public GroupedResponseT nearVideo(NearVideo query, GroupBy groupBy) {
    return performRequest(query, groupBy);
  }

  // NearThermal queries ------------------------------------------------------

  /**
   * Query collection objects using near thermal search.
   *
   * @param thermal Query thermal (base64-encoded).
   */
  public ResponseT nearThermal(String thermal) {
    return nearThermal(NearThermal.of(thermal));
  }

  /**
   * Query collection objects using near thermal search.
   *
   * @param thermal Query thermal (base64-encoded).
   * @param fn      Lambda expression for optional parameters.
   */
  public ResponseT nearThermal(String thermal, Function<NearThermal.Builder, ObjectBuilder<NearThermal>> fn) {
    return nearThermal(NearThermal.of(thermal, fn));
  }

  /**
   * Query collection objects using near thermal search.
   *
   * @param query Near thermal query request.
   */
  public ResponseT nearThermal(NearThermal query) {
    return performRequest(query);
  }

  /**
   * Query collection objects using near thermal search.
   *
   * @param thermal Query thermal (base64-encoded).
   * @param groupBy Group-by clause.
   * @return Grouped query result.
   *
   * @see GroupBy
   * @see QueryResponseGrouped
   */
  public GroupedResponseT nearThermal(String thermal, GroupBy groupBy) {
    return nearThermal(NearThermal.of(thermal), groupBy);
  }

  /**
   * Query collection objects using near thermal search.
   *
   * @param thermal Query thermal (base64-encoded).
   * @param fn      Lambda expression for optional parameters.
   * @param groupBy Group-by clause.
   * @return Grouped query result.
   *
   * @see GroupBy
   * @see QueryResponseGrouped
   */
  public GroupedResponseT nearThermal(String thermal, Function<NearThermal.Builder, ObjectBuilder<NearThermal>> fn,
      GroupBy groupBy) {
    return nearThermal(NearThermal.of(thermal, fn), groupBy);
  }

  /**
   * Query collection objects using near thermal search.
   *
   * @param query   Near thermal query request.
   * @param groupBy Group-by clause.
   * @return Grouped query result.
   *
   * @see GroupBy
   * @see QueryResponseGrouped
   */
  public GroupedResponseT nearThermal(NearThermal query, GroupBy groupBy) {
    return performRequest(query, groupBy);
  }

  // NearDepth queries --------------------------------------------------------

  /**
   * Query collection objects using near depth search.
   *
   * @param depth Query depth (base64-encoded).
   */
  public ResponseT nearDepth(String depth) {
    return nearDepth(NearDepth.of(depth));
  }

  /**
   * Query collection objects using near depth search.
   *
   * @param depth Query depth (base64-encoded).
   * @param fn    Lambda expression for optional parameters.
   */
  public ResponseT nearDepth(String depth, Function<NearDepth.Builder, ObjectBuilder<NearDepth>> fn) {
    return nearDepth(NearDepth.of(depth, fn));
  }

  /**
   * Query collection objects using near depth search.
   *
   * @param query Near depth query request.
   */
  public ResponseT nearDepth(NearDepth query) {
    return performRequest(query);
  }

  /**
   * Query collection objects using near depth search.
   *
   * @param depth   Query depth (base64-encoded).
   * @param groupBy Group-by clause.
   * @return Grouped query result.
   *
   * @see GroupBy
   * @see QueryResponseGrouped
   */
  public GroupedResponseT nearDepth(String depth, GroupBy groupBy) {
    return nearDepth(NearDepth.of(depth), groupBy);
  }

  /**
   * Query collection objects using near depth search.
   *
   * @param depth   Query depth (base64-encoded).
   * @param fn      Lambda expression for optional parameters.
   * @param groupBy Group-by clause.
   * @return Grouped query result.
   *
   * @see GroupBy
   * @see QueryResponseGrouped
   */
  public GroupedResponseT nearDepth(String depth, Function<NearDepth.Builder, ObjectBuilder<NearDepth>> fn,
      GroupBy groupBy) {
    return nearDepth(NearDepth.of(depth, fn), groupBy);
  }

  /**
   * Query collection objects using near depth search.
   *
   * @param query   Near depth query request.
   * @param groupBy Group-by clause.
   * @return Grouped query result.
   *
   * @see GroupBy
   * @see QueryResponseGrouped
   */
  public GroupedResponseT nearDepth(NearDepth query, GroupBy groupBy) {
    return performRequest(query, groupBy);
  }

  // NearImu queries ----------------------------------------------------------

  /**
   * Query collection objects using near IMU search.
   *
   * @param imu Query IMU (base64-encoded).
   */
  public ResponseT nearImu(String imu) {
    return nearImu(NearImu.of(imu));
  }

  /**
   * Query collection objects using near IMU search.
   *
   * @param imu Query IMU (base64-encoded).
   * @param fn  Lambda expression for optional parameters.
   */
  public ResponseT nearImu(String imu, Function<NearImu.Builder, ObjectBuilder<NearImu>> fn) {
    return nearImu(NearImu.of(imu, fn));
  }

  /**
   * Query collection objects using near IMU search.
   *
   * @param query Near IMU query request.
   */
  public ResponseT nearImu(NearImu query) {
    return performRequest(query);
  }

  /**
   * Query collection objects using near IMU search.
   *
   * @param imu     Query IMU (base64-encoded).
   * @param groupBy Group-by clause.
   * @return Grouped query result.
   *
   * @see GroupBy
   * @see QueryResponseGrouped
   */
  public GroupedResponseT nearImu(String imu, GroupBy groupBy) {
    return nearImu(NearImu.of(imu), groupBy);
  }

  /**
   * Query collection objects using near IMU search.
   *
   * @param imu     Query IMU (base64-encoded).
   * @param fn      Lambda expression for optional parameters.
   * @param groupBy Group-by clause.
   * @return Grouped query result.
   *
   * @see GroupBy
   * @see QueryResponseGrouped
   */
  public GroupedResponseT nearImu(String imu, Function<NearImu.Builder, ObjectBuilder<NearImu>> fn,
      GroupBy groupBy) {
    return nearImu(NearImu.of(imu, fn), groupBy);
  }

  /**
   * Query collection objects using near IMU search.
   *
   * @param query   Near IMU query request.
   * @param groupBy Group-by clause.
   * @return Grouped query result.
   *
   * @see GroupBy
   * @see QueryResponseGrouped
   */
  public GroupedResponseT nearImu(NearImu query, GroupBy groupBy) {
    return performRequest(query, groupBy);
  }
}
