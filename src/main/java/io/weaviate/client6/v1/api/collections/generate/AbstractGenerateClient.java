package io.weaviate.client6.v1.api.collections.generate;

import java.util.List;
import java.util.function.Function;

import io.weaviate.client6.v1.api.WeaviateApiException;
import io.weaviate.client6.v1.api.collections.CollectionHandleDefaults;
import io.weaviate.client6.v1.api.collections.query.Bm25;
import io.weaviate.client6.v1.api.collections.query.FetchObjects;
import io.weaviate.client6.v1.api.collections.query.GroupBy;
import io.weaviate.client6.v1.api.collections.query.Hybrid;
import io.weaviate.client6.v1.api.collections.query.NearAudio;
import io.weaviate.client6.v1.api.collections.query.NearDepth;
import io.weaviate.client6.v1.api.collections.query.NearImage;
import io.weaviate.client6.v1.api.collections.query.NearImu;
import io.weaviate.client6.v1.api.collections.query.NearObject;
import io.weaviate.client6.v1.api.collections.query.NearText;
import io.weaviate.client6.v1.api.collections.query.NearThermal;
import io.weaviate.client6.v1.api.collections.query.NearVector;
import io.weaviate.client6.v1.api.collections.query.NearVectorTarget;
import io.weaviate.client6.v1.api.collections.query.NearVideo;
import io.weaviate.client6.v1.api.collections.query.QueryOperator;
import io.weaviate.client6.v1.api.collections.query.Target;
import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.grpc.GrpcTransport;
import io.weaviate.client6.v1.internal.orm.CollectionDescriptor;

abstract class AbstractGenerateClient<PropertiesT, ResponseT, GroupedResponseT> {
  protected final CollectionDescriptor<PropertiesT> collection;
  protected final GrpcTransport grpcTransport;
  protected final CollectionHandleDefaults defaults;

  AbstractGenerateClient(CollectionDescriptor<PropertiesT> collection, GrpcTransport grpcTransport,
      CollectionHandleDefaults defaults) {
    this.collection = collection;
    this.grpcTransport = grpcTransport;
    this.defaults = defaults;
  }

  /** Copy constructor that sets new defaults. */
  AbstractGenerateClient(
      AbstractGenerateClient<PropertiesT, ResponseT, GroupedResponseT> c,
      CollectionHandleDefaults defaults) {
    this(c.collection, c.grpcTransport, defaults);
  }

  protected abstract ResponseT performRequest(QueryOperator operator, GenerativeTask generate);

  protected abstract GroupedResponseT performRequest(QueryOperator operator, GenerativeTask generate, GroupBy groupBy);

  // Object queries -----------------------------------------------------------

  /**
   * Retrieve objects without applying a Vector Search or Keyword Search filter
   * and run a generative task on the query results.
   *
   * @param fn         Lambda expression for optional search parameters.
   * @param generateFn Lambda expression for generative task parameters.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   */
  public ResponseT fetchObjects(Function<FetchObjects.Builder, ObjectBuilder<FetchObjects>> fn,
      Function<GenerativeTask.Builder, ObjectBuilder<GenerativeTask>> generateFn) {
    return fetchObjects(FetchObjects.of(fn), GenerativeTask.of(generateFn));
  }

  /**
   * Retrieve objects without applying a Vector Search or Keyword Search filter
   * and run a generative task on the query results.
   *
   * @param query    FetchObjects query.
   * @param generate Generative task.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   */
  public ResponseT fetchObjects(FetchObjects query, GenerativeTask generate) {
    return performRequest(query, generate);
  }

  /**
   * Retrieve objects without applying a Vector Search or Keyword Search filter
   * and run a generative task on the query results.
   *
   *
   * @param fn         Lambda expression for optional search parameters.
   * @param generateFn Lambda expression for generative task parameters.
   * @param groupBy    Group-by clause.
   * @return Grouped query result.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *
   * @see GroupBy
   * @see GenerativeResponseGrouped
   */
  public GroupedResponseT fetchObjects(Function<FetchObjects.Builder, ObjectBuilder<FetchObjects>> fn,
      Function<GenerativeTask.Builder, ObjectBuilder<GenerativeTask>> generateFn,
      GroupBy groupBy) {
    return fetchObjects(FetchObjects.of(fn), GenerativeTask.of(generateFn), groupBy);
  }

  /**
   * Retrieve objects without applying a Vector Search or Keyword Search filter
   * and run a generative task on the query results.
   *
   * @param query    FetchObjects query.
   * @param generate Generative task.
   * @param groupBy  Group-by clause.
   * @return Grouped query result.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *
   * @see GroupBy
   * @see GenerativeResponseGrouped
   */
  public GroupedResponseT fetchObjects(FetchObjects query, GenerativeTask generate, GroupBy groupBy) {
    return performRequest(query, generate, groupBy);
  }

  // BM25 queries -------------------------------------------------------------

  /**
   * Run a generative task on the results of a keyword (BM25) search.
   *
   * @param query      Query string.
   * @param generateFn Lambda expression for generative task parameters.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   */
  public ResponseT bm25(String query,
      Function<GenerativeTask.Builder, ObjectBuilder<GenerativeTask>> generateFn) {
    return bm25(Bm25.of(query), GenerativeTask.of(generateFn));
  }

  /**
   * Run a generative task on the results of a keyword (BM25) search.
   *
   * @param query      Query string.
   * @param fn         Lambda expression for optional search parameters.
   * @param generateFn Lambda expression for generative task parameters.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   */
  public ResponseT bm25(
      String query,
      Function<Bm25.Builder, ObjectBuilder<Bm25>> fn,
      Function<GenerativeTask.Builder, ObjectBuilder<GenerativeTask>> generateFn) {
    return bm25(Bm25.of(query, fn), GenerativeTask.of(generateFn));
  }

  /**
   * Run a generative task on the results of a keyword (BM25) search.
   *
   * @param query    BM25 query request.
   * @param generate Generative task.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   */
  public ResponseT bm25(Bm25 query, GenerativeTask generate) {
    return performRequest(query, generate);
  }

  /**
   * Run a generative task on the results of a keyword (BM25) search.
   *
   * @param query      Query string.
   * @param generateFn Lambda expression for generative task parameters.
   * @param groupBy    Group-by clause.
   * @return Grouped query result.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *
   * @see GroupBy
   * @see GenerativeResponseGrouped
   */
  public GroupedResponseT bm25(String query,
      Function<GenerativeTask.Builder, ObjectBuilder<GenerativeTask>> generateFn,
      GroupBy groupBy) {
    return bm25(Bm25.of(query), GenerativeTask.of(generateFn), groupBy);
  }

  /**
   * Run a generative task on the results of a keyword (BM25) search.
   *
   * @param query      Query string.
   * @param fn         Lambda expression for optional search parameters.
   * @param generateFn Lambda expression for generative task parameters.
   * @param groupBy    Group-by clause.
   * @return Grouped query result.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *
   * @see GroupBy
   * @see GenerativeResponseGrouped
   */
  public GroupedResponseT bm25(String query,
      Function<Bm25.Builder, ObjectBuilder<Bm25>> fn,
      Function<GenerativeTask.Builder, ObjectBuilder<GenerativeTask>> generateFn,
      GroupBy groupBy) {
    return bm25(Bm25.of(query, fn), GenerativeTask.of(generateFn), groupBy);
  }

  /**
   * Run a generative task on the results of a keyword (BM25) search.
   *
   * @param query    BM25 query request.
   * @param generate Generative task.
   * @param groupBy  Group-by clause.
   * @return Grouped query result.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *
   * @see GroupBy
   * @see GenerativeResponseGrouped
   */
  public GroupedResponseT bm25(Bm25 query, GenerativeTask generate, GroupBy groupBy) {
    return performRequest(query, generate, groupBy);
  }

  // Hybrid queries -----------------------------------------------------------

  /**
   * Run a generative task on the results of a hybrid search.
   *
   * @param query      Query string.
   * @param generateFn Lambda expression for generative task parameters.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   */
  public ResponseT hybrid(String query,
      Function<GenerativeTask.Builder, ObjectBuilder<GenerativeTask>> generateFn) {
    return hybrid(Hybrid.of(query), GenerativeTask.of(generateFn));
  }

  /**
   * Run a generative task on the results of a hybrid search.
   *
   * @param query      Query string.
   * @param fn         Lambda expression for optional search parameters.
   * @param generateFn Lambda expression for generative task parameters.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   */
  public ResponseT hybrid(
      String query,
      Function<Hybrid.Builder, ObjectBuilder<Hybrid>> fn,
      Function<GenerativeTask.Builder, ObjectBuilder<GenerativeTask>> generateFn) {
    return hybrid(Hybrid.of(query, fn), GenerativeTask.of(generateFn));
  }

  /**
   * Run a generative task on the results of a hybrid search.
   *
   * @param searchTarget Query target.
   * @param generateFn   Lambda expression for generative task parameters.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   */
  public ResponseT hybrid(
      Target searchTarget,
      Function<GenerativeTask.Builder, ObjectBuilder<GenerativeTask>> generateFn) {
    return hybrid(Hybrid.of(searchTarget), GenerativeTask.of(generateFn));
  }

  /**
   * Run a generative task on the results of a hybrid search.
   *
   * @param searchTarget Query target.
   * @param fn           Lambda expression for optional search parameters.
   * @param generateFn   Lambda expression for generative task parameters.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   */
  public ResponseT hybrid(
      Target searchTarget,
      Function<Hybrid.Builder, ObjectBuilder<Hybrid>> fn,
      Function<GenerativeTask.Builder, ObjectBuilder<GenerativeTask>> generateFn) {
    return hybrid(Hybrid.of(searchTarget, fn), GenerativeTask.of(generateFn));
  }

  /**
   * Run a generative task on the results of a hybrid search.
   *
   * @param query    Hybrid query request.
   * @param generate Generative task.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   */
  public ResponseT hybrid(Hybrid query, GenerativeTask generate) {
    return performRequest(query, generate);
  }

  /**
   * Run a generative task on the results of a hybrid search.
   *
   * @param query      Query string.
   * @param generateFn Lambda expression for generative task parameters.
   * @param groupBy    Group-by clause.
   * @return Grouped query result.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *
   * @see GroupBy
   * @see GenerativeResponseGrouped
   */
  public GroupedResponseT hybrid(
      String query,
      Function<GenerativeTask.Builder, ObjectBuilder<GenerativeTask>> generateFn,
      GroupBy groupBy) {
    return hybrid(Hybrid.of(query), GenerativeTask.of(generateFn), groupBy);
  }

  /**
   * Run a generative task on the results of a hybrid search.
   *
   * @param query      Query string.
   * @param fn         Lambda expression for optional search parameters.
   * @param generateFn Lambda expression for generative task parameters.
   * @param groupBy    Group-by clause.
   * @return Grouped query result.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *
   * @see GroupBy
   * @see GenerativeResponseGrouped
   */
  public GroupedResponseT hybrid(
      String query,
      Function<GenerativeTask.Builder, ObjectBuilder<GenerativeTask>> generateFn,
      Function<Hybrid.Builder, ObjectBuilder<Hybrid>> fn, GroupBy groupBy) {
    return hybrid(Hybrid.of(query, fn), GenerativeTask.of(generateFn), groupBy);
  }

  /**
   * Run a generative task on the results of a hybrid search.
   *
   * @param searchTarget Query target.
   * @param generateFn   Lambda expression for generative task parameters.
   * @param groupBy      Group-by clause.
   * @return Grouped query result.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *
   * @see GroupBy
   * @see GenerativeResponseGrouped
   */
  public GroupedResponseT hybrid(
      Target searchTarget,
      Function<GenerativeTask.Builder, ObjectBuilder<GenerativeTask>> generateFn,
      GroupBy groupBy) {
    return hybrid(Hybrid.of(searchTarget), GenerativeTask.of(generateFn), groupBy);
  }

  /**
   * Run a generative task on the results of a hybrid search.
   *
   * @param searchTarget Query target.
   * @param fn           Lambda expression for optional search parameters.
   * @param generateFn   Lambda expression for generative task parameters.
   * @param groupBy      Group-by clause.
   * @return Grouped query result.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *
   * @see GroupBy
   * @see GenerativeResponseGrouped
   */
  public GroupedResponseT hybrid(
      Target searchTarget,
      Function<GenerativeTask.Builder, ObjectBuilder<GenerativeTask>> generateFn,
      Function<Hybrid.Builder, ObjectBuilder<Hybrid>> fn,
      GroupBy groupBy) {
    return hybrid(Hybrid.of(searchTarget, fn), GenerativeTask.of(generateFn), groupBy);
  }

  /**
   * Run a generative task on the results of a hybrid search.
   *
   * @param query    Query string.
   * @param generate Generative task.
   * @param groupBy  Group-by clause.
   * @return Grouped query result.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *
   * @see GroupBy
   * @see GenerativeResponseGrouped
   */
  public GroupedResponseT hybrid(Hybrid query, GenerativeTask generate, GroupBy groupBy) {
    return performRequest(query, generate, groupBy);
  }

  // NearVector queries -------------------------------------------------------

  /**
   * Run a generative task on the results of a near vector search.
   *
   * @param vector     Query vector.
   * @param generateFn Lambda expression for generative task parameters.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   */
  public ResponseT nearVector(float[] vector,
      Function<GenerativeTask.Builder, ObjectBuilder<GenerativeTask>> generateFn) {
    return nearVector(Target.vector(vector), generateFn);
  }

  /**
   * Run a generative task on the results of a near vector search.
   *
   * @param vector     Query vector.
   * @param fn         Lambda expression for optional search parameters.
   * @param generateFn Lambda expression for generative task parameters.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   */
  public ResponseT nearVector(float[] vector,
      Function<NearVector.Builder, ObjectBuilder<NearVector>> fn,
      Function<GenerativeTask.Builder, ObjectBuilder<GenerativeTask>> generateFn) {
    return nearVector(Target.vector(vector), fn, generateFn);
  }

  /**
   * Run a generative task on the results of a near vector search.
   *
   * @param searchTarget Target query vectors.
   * @param generateFn   Lambda expression for generative task parameters.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   */
  public ResponseT nearVector(NearVectorTarget searchTarget,
      Function<GenerativeTask.Builder, ObjectBuilder<GenerativeTask>> generateFn) {
    return nearVector(NearVector.of(searchTarget), GenerativeTask.of(generateFn));
  }

  /**
   * Run a generative task on the results of a near vector search.
   *
   * @param searchTarget Target query vectors.
   * @param fn           Lambda expression for optional search parameters.
   * @param generateFn   Lambda expression for generative task parameters.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   */
  public ResponseT nearVector(NearVectorTarget searchTarget,
      Function<NearVector.Builder, ObjectBuilder<NearVector>> fn,
      Function<GenerativeTask.Builder, ObjectBuilder<GenerativeTask>> generateFn) {
    return nearVector(NearVector.of(searchTarget, fn), GenerativeTask.of(generateFn));
  }

  /**
   * Run a generative task on the results of a near vector search.
   *
   * @param query    Near vector query request.
   * @param generate Generative task.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   */
  public ResponseT nearVector(NearVector query, GenerativeTask generate) {
    return performRequest(query, generate);
  }

  /**
   * Run a generative task on the results of a near vector search.
   *
   * @param vector     Query vector.
   * @param generateFn Lambda expression for generative task parameters.
   * @param groupBy    Group-by clause.
   * @return Grouped query result.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *
   * @see GroupBy
   * @see GenerativeResponseGrouped
   */
  public GroupedResponseT nearVector(float[] vector,
      Function<GenerativeTask.Builder, ObjectBuilder<GenerativeTask>> generateFn,
      GroupBy groupBy) {
    return nearVector(Target.vector(vector), generateFn, groupBy);
  }

  /**
   * Run a generative task on the results of a near vector search.
   *
   * @param vector     Query vector.
   * @param fn         Lambda expression for optional search parameters.
   * @param generateFn Lambda expression for generative task parameters.
   * @param groupBy    Group-by clause.
   * @return Grouped query result.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *
   * @see GroupBy
   * @see GenerativeResponseGrouped
   */
  public GroupedResponseT nearVector(float[] vector,
      Function<NearVector.Builder, ObjectBuilder<NearVector>> fn,
      Function<GenerativeTask.Builder, ObjectBuilder<GenerativeTask>> generateFn,
      GroupBy groupBy) {
    return nearVector(Target.vector(vector), fn, generateFn, groupBy);
  }

  /**
   * Run a generative task on the results of a near vector search.
   *
   * @param searchTarget Target query vectors.
   * @param generateFn   Lambda expression for generative task parameters.
   * @param groupBy      Group-by clause.
   * @return Grouped query result.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *
   * @see GroupBy
   * @see GenerativeResponseGrouped
   */
  public GroupedResponseT nearVector(
      NearVectorTarget searchTarget,
      Function<GenerativeTask.Builder, ObjectBuilder<GenerativeTask>> generateFn,
      GroupBy groupBy) {
    return nearVector(NearVector.of(searchTarget), GenerativeTask.of(generateFn), groupBy);
  }

  /**
   * Run a generative task on the results of a near vector search.
   *
   * @param searchTarget Target query vectors.
   * @param fn           Lambda expression for optional search parameters.
   * @param generateFn   Lambda expression for generative task parameters.
   * @return Grouped query result.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *
   * @see GroupBy
   * @see GenerativeResponseGrouped
   */
  public GroupedResponseT nearVector(NearVectorTarget searchTarget,
      Function<NearVector.Builder, ObjectBuilder<NearVector>> fn,
      Function<GenerativeTask.Builder, ObjectBuilder<GenerativeTask>> generateFn,
      GroupBy groupBy) {
    return nearVector(NearVector.of(searchTarget, fn), GenerativeTask.of(generateFn), groupBy);
  }

  /**
   * Run a generative task on the results of a near vector search.
   *
   * @param query    Near vector query request.
   * @param generate Generative task.
   * @param groupBy  Group-by clause.
   * @return Grouped query result.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *
   * @see GroupBy
   * @see GenerativeResponseGrouped
   */
  public GroupedResponseT nearVector(NearVector query, GenerativeTask generate, GroupBy groupBy) {
    return performRequest(query, generate, groupBy);
  }

  // NearObject queries -------------------------------------------------------

  /**
   * Run a generative task on the results of a near object search.
   *
   * @param uuid       Query object UUID.
   * @param generateFn Lambda expression for generative task parameters.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   */
  public ResponseT nearObject(String uuid,
      Function<GenerativeTask.Builder, ObjectBuilder<GenerativeTask>> generateFn) {
    return nearObject(NearObject.of(uuid), GenerativeTask.of(generateFn));
  }

  /**
   * Run a generative task on the results of a near object search.
   *
   * @param uuid       Query object UUID.
   * @param fn         Lambda expression for optional search parameters.
   * @param generateFn Lambda expression for generative task parameters.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   */
  public ResponseT nearObject(String uuid,
      Function<NearObject.Builder, ObjectBuilder<NearObject>> fn,
      Function<GenerativeTask.Builder, ObjectBuilder<GenerativeTask>> generateFn) {
    return nearObject(NearObject.of(uuid, fn), GenerativeTask.of(generateFn));
  }

  /**
   * Run a generative task on the results of a near object search.
   *
   * @param query    Near object query request.
   * @param generate Generative task.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   */
  public ResponseT nearObject(NearObject query, GenerativeTask generate) {
    return performRequest(query, generate);
  }

  /**
   * Run a generative task on the results of a near object search.
   *
   * @param uuid       Query object UUID.
   * @param generateFn Lambda expression for generative task parameters.
   * @param groupBy    Group-by clause.
   * @return Grouped query result.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *
   * @see GroupBy
   * @see GenerativeResponseGrouped
   */
  public GroupedResponseT nearObject(String uuid,
      Function<GenerativeTask.Builder, ObjectBuilder<GenerativeTask>> generateFn,
      GroupBy groupBy) {
    return nearObject(NearObject.of(uuid), GenerativeTask.of(generateFn), groupBy);
  }

  /**
   * Run a generative task on the results of a near object search.
   *
   * @param uuid       Query object UUID.
   * @param fn         Lambda expression for optional search parameters.
   * @param generateFn Lambda expression for generative task parameters.
   * @param groupBy    Group-by clause.
   * @return Grouped query result.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *
   * @see GroupBy
   * @see GenerativeResponseGrouped
   */
  public GroupedResponseT nearObject(String uuid,
      Function<NearObject.Builder, ObjectBuilder<NearObject>> fn,
      Function<GenerativeTask.Builder, ObjectBuilder<GenerativeTask>> generateFn,
      GroupBy groupBy) {
    return nearObject(NearObject.of(uuid, fn), GenerativeTask.of(generateFn), groupBy);
  }

  /**
   * Run a generative task on the results of a near object search.
   *
   * @param query    Near object query request.
   * @param generate Generative task.
   * @param groupBy  Group-by clause.
   * @return Grouped query result.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *
   * @see GroupBy
   * @see GenerativeResponseGrouped
   */
  public GroupedResponseT nearObject(NearObject query, GenerativeTask generate, GroupBy groupBy) {
    return performRequest(query, generate, groupBy);
  }

  // NearText queries ---------------------------------------------------------

  /**
   * Run a generative task on the results of a near text search.
   *
   * @param text       Query concepts.
   * @param fn         Lambda expression for optional parameters.
   * @param generateFn Lambda expression for generative task parameters.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   */
  public ResponseT nearText(String text,
      Function<NearText.Builder, ObjectBuilder<NearText>> fn,
      Function<GenerativeTask.Builder, ObjectBuilder<GenerativeTask>> generateFn) {
    return nearText(Target.text(List.of(text)), fn, generateFn);
  }

  /**
   * Run a generative task on the results of a near text search.
   *
   * @param text       Query concepts.
   * @param fn         Lambda expression for optional parameters.
   * @param generateFn Lambda expression for generative task parameters.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   */
  public ResponseT nearText(List<String> text,
      Function<NearText.Builder, ObjectBuilder<NearText>> fn,
      Function<GenerativeTask.Builder, ObjectBuilder<GenerativeTask>> generateFn) {
    return nearText(Target.text(text), fn, generateFn);
  }

  /**
   * Run a generative task on the results of a near text search.
   *
   * @param searchTarget Target query concepts.
   * @param generateFn   Lambda expression for generative task parameters.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   */
  public ResponseT nearText(Target searchTarget,
      Function<GenerativeTask.Builder, ObjectBuilder<GenerativeTask>> generateFn) {
    return nearText(NearText.of(searchTarget), GenerativeTask.of(generateFn));
  }

  /**
   * Run a generative task on the results of a near text search.
   *
   * @param searchTarget Target query concepts.
   * @param fn           Lambda expression for optional parameters.
   * @param generateFn   Lambda expression for generative task parameters.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   */
  public ResponseT nearText(Target searchTarget,
      Function<NearText.Builder, ObjectBuilder<NearText>> fn,
      Function<GenerativeTask.Builder, ObjectBuilder<GenerativeTask>> generateFn) {
    return nearText(NearText.of(searchTarget, fn), GenerativeTask.of(generateFn));
  }

  /**
   * Run a generative task on the results of a near text search.
   *
   * @param query    Near text query request.
   * @param generate Generative task.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   */
  public ResponseT nearText(NearText query, GenerativeTask generate) {
    return performRequest(query, generate);
  }

  /**
   * Run a generative task on the results of a near text search.
   *
   * @param text       Query concepts.
   * @param generateFn Lambda expression for generative task parameters.
   * @param groupBy    Group-by clause.
   * @return Grouped query result.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *
   * @see GroupBy
   * @see GenerativeResponseGrouped
   */
  public GroupedResponseT nearText(String text,
      Function<GenerativeTask.Builder, ObjectBuilder<GenerativeTask>> generateFn,
      GroupBy groupBy) {
    return nearText(Target.text(List.of(text)), generateFn, groupBy);
  }

  /**
   * Run a generative task on the results of a near text search.
   *
   * @param text       Query concepts.
   * @param generateFn Lambda expression for generative task parameters.
   * @param groupBy    Group-by clause.
   * @return Grouped query result.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *
   * @see GroupBy
   * @see GenerativeResponseGrouped
   */
  public GroupedResponseT nearText(List<String> text,
      Function<GenerativeTask.Builder, ObjectBuilder<GenerativeTask>> generateFn, GroupBy groupBy) {
    return nearText(Target.text(text), generateFn, groupBy);
  }

  /**
   * Run a generative task on the results of a near text search.
   *
   * @param text       Query concepts.
   * @param fn         Lambda expression for optional parameters.
   * @param generateFn Lambda expression for generative task parameters.
   * @param groupBy    Group-by clause.
   * @return Grouped query result.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *
   * @see GroupBy
   * @see GenerativeResponseGrouped
   */
  public GroupedResponseT nearText(String text,
      Function<NearText.Builder, ObjectBuilder<NearText>> fn,
      Function<GenerativeTask.Builder, ObjectBuilder<GenerativeTask>> generateFn,
      GroupBy groupBy) {
    return nearText(Target.text(List.of(text)), fn, generateFn, groupBy);
  }

  /**
   * Run a generative task on the results of a near text search.
   *
   * @param text       Query concepts.
   * @param fn         Lambda expression for optional parameters.
   * @param generateFn Lambda expression for generative task parameters.
   * @param groupBy    Group-by clause.
   * @return Grouped query result.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *
   * @see GroupBy
   * @see GenerativeResponseGrouped
   */
  public GroupedResponseT nearText(List<String> text,
      Function<NearText.Builder, ObjectBuilder<NearText>> fn,
      Function<GenerativeTask.Builder, ObjectBuilder<GenerativeTask>> generateFn,
      GroupBy groupBy) {
    return nearText(Target.text(text), fn, generateFn, groupBy);
  }

  /**
   * Run a generative task on the results of a near text search.
   *
   * @param searchTarget Target query concepts.
   * @param generateFn   Lambda expression for generative task parameters.
   * @param groupBy      Group-by clause.
   * @return Grouped query result.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *
   * @see GroupBy
   * @see GenerativeResponseGrouped
   */
  public GroupedResponseT nearText(Target searchTarget,
      Function<GenerativeTask.Builder, ObjectBuilder<GenerativeTask>> generateFn, GroupBy groupBy) {
    return nearText(NearText.of(searchTarget), GenerativeTask.of(generateFn), groupBy);
  }

  /**
   * Run a generative task on the results of a near text search.
   *
   * @param searchTarget Target query concepts.
   * @param fn           Lambda expression for optional parameters.
   * @param generateFn   Lambda expression for generative task parameters.
   * @param groupBy      Group-by clause.
   * @return Grouped query result.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *
   * @see GroupBy
   * @see GenerativeResponseGrouped
   */
  public GroupedResponseT nearText(Target searchTarget,
      Function<NearText.Builder, ObjectBuilder<NearText>> fn,
      Function<GenerativeTask.Builder, ObjectBuilder<GenerativeTask>> generateFn,
      GroupBy groupBy) {
    return nearText(NearText.of(searchTarget, fn), GenerativeTask.of(generateFn), groupBy);
  }

  /**
   * Run a generative task on the results of a near text search.
   *
   * @param query    Near text query request.
   * @param generate Generative task.
   * @param groupBy  Group-by clause.
   * @return Grouped query result.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *
   * @see GroupBy
   * @see GenerativeResponseGrouped
   */
  public GroupedResponseT nearText(NearText query, GenerativeTask generate, GroupBy groupBy) {
    return performRequest(query, generate, groupBy);
  }

  // NearImage queries --------------------------------------------------------

  /**
   * Run a generative task on the results of a near image search.
   *
   * @param image      Query image (base64-encoded).
   * @param generateFn Lambda expression for generative task parameters.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   */
  public ResponseT nearImage(String image,
      Function<GenerativeTask.Builder, ObjectBuilder<GenerativeTask>> generateFn) {
    return nearImage(NearImage.of(image), GenerativeTask.of(generateFn));
  }

  /**
   * Run a generative task on the results of a near image search.
   *
   * @param image      Query image (base64-encoded).
   * @param fn         Lambda expression for optional search parameters.
   * @param generateFn Lambda expression for generative task parameters.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   */
  public ResponseT nearImage(String image, Function<NearImage.Builder, ObjectBuilder<NearImage>> fn,
      Function<GenerativeTask.Builder, ObjectBuilder<GenerativeTask>> generateFn) {
    return nearImage(NearImage.of(image, fn), GenerativeTask.of(generateFn));
  }

  /**
   * Run a generative task on the results of a near image search.
   *
   * @param searchTarget Query target (base64-encoded image).
   * @param generateFn   Lambda expression for generative task parameters.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   */
  public ResponseT nearImage(Target searchTarget,
      Function<GenerativeTask.Builder, ObjectBuilder<GenerativeTask>> generateFn) {
    return nearImage(NearImage.of(searchTarget), GenerativeTask.of(generateFn));
  }

  /**
   * Run a generative task on the results of a near image search.
   *
   * @param searchTarget Query target (base64-encoded image).
   * @param fn           Lambda expression for optional search parameters.
   * @param generateFn   Lambda expression for generative task parameters.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   */
  public ResponseT nearImage(Target searchTarget, Function<NearImage.Builder, ObjectBuilder<NearImage>> fn,
      Function<GenerativeTask.Builder, ObjectBuilder<GenerativeTask>> generateFn) {
    return nearImage(NearImage.of(searchTarget, fn), GenerativeTask.of(generateFn));
  }

  /**
   * Run a generative task on the results of a near image search.
   *
   * @param query    Near image query request.
   * @param generate Generative task.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   */
  public ResponseT nearImage(NearImage query, GenerativeTask generate) {
    return performRequest(query, generate);
  }

  /**
   * Run a generative task on the results of a near image search.
   *
   * @param image      Query image (base64-encoded).
   * @param generateFn Lambda expression for generative task parameters.
   * @param groupBy    Group-by clause.
   * @return Grouped query result.
   *
   * @see GroupBy
   * @see GenerativeResponseGrouped
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   */
  public GroupedResponseT nearImage(String image,
      Function<GenerativeTask.Builder, ObjectBuilder<GenerativeTask>> generateFn, GroupBy groupBy) {
    return nearImage(NearImage.of(image), GenerativeTask.of(generateFn), groupBy);
  }

  /**
   * Run a generative task on the results of a near image search.
   *
   * @param searchTarget Query target (base64-encoded image).
   * @param fn           Lambda expression for optional search parameters.
   * @param generateFn   Lambda expression for generative task parameters.
   * @param groupBy      Group-by clause.
   * @return Grouped query result.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *
   * @see GroupBy
   * @see GenerativeResponseGrouped
   */
  public GroupedResponseT nearImage(Target searchTarget, Function<NearImage.Builder, ObjectBuilder<NearImage>> fn,
      Function<GenerativeTask.Builder, ObjectBuilder<GenerativeTask>> generateFn,
      GroupBy groupBy) {
    return nearImage(NearImage.of(searchTarget, fn), GenerativeTask.of(generateFn), groupBy);
  }

  /**
   * Run a generative task on the results of a near image search.
   *
   * @param searchTarget Query target (base64-encoded image).
   * @param generateFn   Lambda expression for generative task parameters.
   * @param groupBy      Group-by clause.
   * @return Grouped query result.
   *
   * @see GroupBy
   * @see GenerativeResponseGrouped
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   */
  public GroupedResponseT nearImage(Target searchTarget,
      Function<GenerativeTask.Builder, ObjectBuilder<GenerativeTask>> generateFn, GroupBy groupBy) {
    return nearImage(NearImage.of(searchTarget), GenerativeTask.of(generateFn), groupBy);
  }

  /**
   * Run a generative task on the results of a near image search.
   *
   * @param image      Query image (base64-encoded).
   * @param fn         Lambda expression for optional search parameters.
   * @param generateFn Lambda expression for generative task parameters.
   * @param groupBy    Group-by clause.
   * @return Grouped query result.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *
   * @see GroupBy
   * @see GenerativeResponseGrouped
   */
  public GroupedResponseT nearImage(String image, Function<NearImage.Builder, ObjectBuilder<NearImage>> fn,
      Function<GenerativeTask.Builder, ObjectBuilder<GenerativeTask>> generateFn,
      GroupBy groupBy) {
    return nearImage(NearImage.of(image, fn), GenerativeTask.of(generateFn), groupBy);
  }

  /**
   * Run a generative task on the results of a near image search.
   *
   * @param query    Near image query request.
   * @param generate Generative task.
   * @param groupBy  Group-by clause.
   * @return Grouped query result.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *
   * @see GroupBy
   * @see GenerativeResponseGrouped
   */
  public GroupedResponseT nearImage(NearImage query, GenerativeTask generate, GroupBy groupBy) {
    return performRequest(query, generate, groupBy);
  }

  // NearAudio queries --------------------------------------------------------

  /**
   * Run a generative task on the results of a near audio search.
   *
   * @param audio      Query audio (base64-encoded).
   * @param generateFn Lambda expression for generative task parameters.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   */
  public ResponseT nearAudio(String audio,
      Function<GenerativeTask.Builder, ObjectBuilder<GenerativeTask>> generateFn) {
    return nearAudio(NearAudio.of(audio), GenerativeTask.of(generateFn));
  }

  /**
   * Run a generative task on the results of a near audio search.
   *
   * @param audio      Query audio (base64-encoded).
   * @param fn         Lambda expression for optional search parameters.
   * @param generateFn Lambda expression for generative task parameters.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   */
  public ResponseT nearAudio(String audio, Function<NearAudio.Builder, ObjectBuilder<NearAudio>> fn,
      Function<GenerativeTask.Builder, ObjectBuilder<GenerativeTask>> generateFn) {
    return nearAudio(NearAudio.of(audio, fn), GenerativeTask.of(generateFn));
  }

  /**
   * Run a generative task on the results of a near audio search.
   *
   * @param searchTarget Query target (base64-encoded audio).
   * @param generateFn   Lambda expression for generative task parameters.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   */
  public ResponseT nearAudio(Target searchTarget,
      Function<GenerativeTask.Builder, ObjectBuilder<GenerativeTask>> generateFn) {
    return nearAudio(NearAudio.of(searchTarget), GenerativeTask.of(generateFn));
  }

  /**
   * Run a generative task on the results of a near audio search.
   *
   * @param searchTarget Query target (base64-encoded audio).
   * @param fn           Lambda expression for optional search parameters.
   * @param generateFn   Lambda expression for generative task parameters.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   */
  public ResponseT nearAudio(Target searchTarget, Function<NearAudio.Builder, ObjectBuilder<NearAudio>> fn,
      Function<GenerativeTask.Builder, ObjectBuilder<GenerativeTask>> generateFn) {
    return nearAudio(NearAudio.of(searchTarget, fn), GenerativeTask.of(generateFn));
  }

  /**
   * Run a generative task on the results of a near audio search.
   *
   * @param query    Near audio query request.
   * @param generate Generative task.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   */
  public ResponseT nearAudio(NearAudio query, GenerativeTask generate) {
    return performRequest(query, generate);
  }

  /**
   * Run a generative task on the results of a near audio search.
   *
   * @param audio      Query audio (base64-encoded).
   * @param generateFn Lambda expression for generative task parameters.
   * @param groupBy    Group-by clause.
   * @return Grouped query result.
   *
   * @see GroupBy
   * @see GenerativeResponseGrouped
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   */
  public GroupedResponseT nearAudio(String audio,
      Function<GenerativeTask.Builder, ObjectBuilder<GenerativeTask>> generateFn, GroupBy groupBy) {
    return nearAudio(NearAudio.of(audio), GenerativeTask.of(generateFn), groupBy);
  }

  /**
   * Run a generative task on the results of a near audio search.
   *
   * @param searchTarget Query target (base64-encoded audio).
   * @param fn           Lambda expression for optional search parameters.
   * @param generateFn   Lambda expression for generative task parameters.
   * @param groupBy      Group-by clause.
   * @return Grouped query result.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *
   * @see GroupBy
   * @see GenerativeResponseGrouped
   */
  public GroupedResponseT nearAudio(Target searchTarget, Function<NearAudio.Builder, ObjectBuilder<NearAudio>> fn,
      Function<GenerativeTask.Builder, ObjectBuilder<GenerativeTask>> generateFn,
      GroupBy groupBy) {
    return nearAudio(NearAudio.of(searchTarget, fn), GenerativeTask.of(generateFn), groupBy);
  }

  /**
   * Run a generative task on the results of a near audio search.
   *
   * @param searchTarget Query target (base64-encoded audio).
   * @param generateFn   Lambda expression for generative task parameters.
   * @param groupBy      Group-by clause.
   * @return Grouped query result.
   *
   * @see GroupBy
   * @see GenerativeResponseGrouped
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   */
  public GroupedResponseT nearAudio(Target searchTarget,
      Function<GenerativeTask.Builder, ObjectBuilder<GenerativeTask>> generateFn, GroupBy groupBy) {
    return nearAudio(NearAudio.of(searchTarget), GenerativeTask.of(generateFn), groupBy);
  }

  /**
   * Run a generative task on the results of a near audio search.
   *
   * @param audio      Query audio (base64-encoded).
   * @param fn         Lambda expression for optional search parameters.
   * @param generateFn Lambda expression for generative task parameters.
   * @param groupBy    Group-by clause.
   * @return Grouped query result.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *
   * @see GroupBy
   * @see GenerativeResponseGrouped
   */
  public GroupedResponseT nearAudio(String audio, Function<NearAudio.Builder, ObjectBuilder<NearAudio>> fn,
      Function<GenerativeTask.Builder, ObjectBuilder<GenerativeTask>> generateFn,
      GroupBy groupBy) {
    return nearAudio(NearAudio.of(audio, fn), GenerativeTask.of(generateFn), groupBy);
  }

  /**
   * Run a generative task on the results of a near audio search.
   *
   * @param query    Near audio query request.
   * @param generate Generative task.
   * @param groupBy  Group-by clause.
   * @return Grouped query result.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *
   * @see GroupBy
   * @see GenerativeResponseGrouped
   */
  public GroupedResponseT nearAudio(NearAudio query, GenerativeTask generate, GroupBy groupBy) {
    return performRequest(query, generate, groupBy);
  }

  // NearVideo queries --------------------------------------------------------

  /**
   * Run a generative task on the results of a near video search.
   *
   * @param video      Query video (base64-encoded).
   * @param generateFn Lambda expression for generative task parameters.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   */
  public ResponseT nearVideo(String video,
      Function<GenerativeTask.Builder, ObjectBuilder<GenerativeTask>> generateFn) {
    return nearVideo(NearVideo.of(video), GenerativeTask.of(generateFn));
  }

  /**
   * Run a generative task on the results of a near video search.
   *
   * @param video      Query video (base64-encoded).
   * @param fn         Lambda expression for optional search parameters.
   * @param generateFn Lambda expression for generative task parameters.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   */
  public ResponseT nearVideo(String video, Function<NearVideo.Builder, ObjectBuilder<NearVideo>> fn,
      Function<GenerativeTask.Builder, ObjectBuilder<GenerativeTask>> generateFn) {
    return nearVideo(NearVideo.of(video, fn), GenerativeTask.of(generateFn));
  }

  /**
   * Run a generative task on the results of a near video search.
   *
   * @param searchTarget Query target (base64-encoded video).
   * @param generateFn   Lambda expression for generative task parameters.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   */
  public ResponseT nearVideo(Target searchTarget,
      Function<GenerativeTask.Builder, ObjectBuilder<GenerativeTask>> generateFn) {
    return nearVideo(NearVideo.of(searchTarget), GenerativeTask.of(generateFn));
  }

  /**
   * Run a generative task on the results of a near video search.
   *
   * @param searchTarget Query target (base64-encoded video).
   * @param fn           Lambda expression for optional search parameters.
   * @param generateFn   Lambda expression for generative task parameters.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   */
  public ResponseT nearVideo(Target searchTarget, Function<NearVideo.Builder, ObjectBuilder<NearVideo>> fn,
      Function<GenerativeTask.Builder, ObjectBuilder<GenerativeTask>> generateFn) {
    return nearVideo(NearVideo.of(searchTarget, fn), GenerativeTask.of(generateFn));
  }

  /**
   * Run a generative task on the results of a near video search.
   *
   * @param query    Near video query request.
   * @param generate Generative task.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   */
  public ResponseT nearVideo(NearVideo query, GenerativeTask generate) {
    return performRequest(query, generate);
  }

  /**
   * Run a generative task on the results of a near video search.
   *
   * @param video      Query video (base64-encoded).
   * @param generateFn Lambda expression for generative task parameters.
   * @param groupBy    Group-by clause.
   * @return Grouped query result.
   *
   * @see GroupBy
   * @see GenerativeResponseGrouped
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   */
  public GroupedResponseT nearVideo(String video,
      Function<GenerativeTask.Builder, ObjectBuilder<GenerativeTask>> generateFn, GroupBy groupBy) {
    return nearVideo(NearVideo.of(video), GenerativeTask.of(generateFn), groupBy);
  }

  /**
   * Run a generative task on the results of a near video search.
   *
   * @param searchTarget Query target (base64-encoded video).
   * @param fn           Lambda expression for optional search parameters.
   * @param generateFn   Lambda expression for generative task parameters.
   * @param groupBy      Group-by clause.
   * @return Grouped query result.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *
   * @see GroupBy
   * @see GenerativeResponseGrouped
   */
  public GroupedResponseT nearVideo(Target searchTarget, Function<NearVideo.Builder, ObjectBuilder<NearVideo>> fn,
      Function<GenerativeTask.Builder, ObjectBuilder<GenerativeTask>> generateFn,
      GroupBy groupBy) {
    return nearVideo(NearVideo.of(searchTarget, fn), GenerativeTask.of(generateFn), groupBy);
  }

  /**
   * Run a generative task on the results of a near video search.
   *
   * @param searchTarget Query target (base64-encoded video).
   * @param generateFn   Lambda expression for generative task parameters.
   * @param groupBy      Group-by clause.
   * @return Grouped query result.
   *
   * @see GroupBy
   * @see GenerativeResponseGrouped
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   */
  public GroupedResponseT nearVideo(Target searchTarget,
      Function<GenerativeTask.Builder, ObjectBuilder<GenerativeTask>> generateFn, GroupBy groupBy) {
    return nearVideo(NearVideo.of(searchTarget), GenerativeTask.of(generateFn), groupBy);
  }

  /**
   * Run a generative task on the results of a near video search.
   *
   * @param video      Query video (base64-encoded).
   * @param fn         Lambda expression for optional search parameters.
   * @param generateFn Lambda expression for generative task parameters.
   * @param groupBy    Group-by clause.
   * @return Grouped query result.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *
   * @see GroupBy
   * @see GenerativeResponseGrouped
   */
  public GroupedResponseT nearVideo(String video, Function<NearVideo.Builder, ObjectBuilder<NearVideo>> fn,
      Function<GenerativeTask.Builder, ObjectBuilder<GenerativeTask>> generateFn,
      GroupBy groupBy) {
    return nearVideo(NearVideo.of(video, fn), GenerativeTask.of(generateFn), groupBy);
  }

  /**
   * Run a generative task on the results of a near video search.
   *
   * @param query    Near video query request.
   * @param generate Generative task.
   * @param groupBy  Group-by clause.
   * @return Grouped query result.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *
   * @see GroupBy
   * @see GenerativeResponseGrouped
   */
  public GroupedResponseT nearVideo(NearVideo query, GenerativeTask generate, GroupBy groupBy) {
    return performRequest(query, generate, groupBy);
  }

  // NearThermal queries --------------------------------------------------------

  /**
   * Run a generative task on the results of a near thermal search.
   *
   * @param thermal    Query thermal (base64-encoded).
   * @param generateFn Lambda expression for generative task parameters.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   */
  public ResponseT nearThermal(String thermal,
      Function<GenerativeTask.Builder, ObjectBuilder<GenerativeTask>> generateFn) {
    return nearThermal(NearThermal.of(thermal), GenerativeTask.of(generateFn));
  }

  /**
   * Run a generative task on the results of a near thermal search.
   *
   * @param thermal    Query thermal (base64-encoded).
   * @param fn         Lambda expression for optional search parameters.
   * @param generateFn Lambda expression for generative task parameters.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   */
  public ResponseT nearThermal(String thermal, Function<NearThermal.Builder, ObjectBuilder<NearThermal>> fn,
      Function<GenerativeTask.Builder, ObjectBuilder<GenerativeTask>> generateFn) {
    return nearThermal(NearThermal.of(thermal, fn), GenerativeTask.of(generateFn));
  }

  /**
   * Run a generative task on the results of a near thermal search.
   *
   * @param searchTarget Query target (base64-encoded thermal).
   * @param generateFn   Lambda expression for generative task parameters.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   */
  public ResponseT nearThermal(Target searchTarget,
      Function<GenerativeTask.Builder, ObjectBuilder<GenerativeTask>> generateFn) {
    return nearThermal(NearThermal.of(searchTarget), GenerativeTask.of(generateFn));
  }

  /**
   * Run a generative task on the results of a near thermal search.
   *
   * @param searchTarget Query target (base64-encoded thermal).
   * @param fn           Lambda expression for optional search parameters.
   * @param generateFn   Lambda expression for generative task parameters.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   */
  public ResponseT nearThermal(Target searchTarget, Function<NearThermal.Builder, ObjectBuilder<NearThermal>> fn,
      Function<GenerativeTask.Builder, ObjectBuilder<GenerativeTask>> generateFn) {
    return nearThermal(NearThermal.of(searchTarget, fn), GenerativeTask.of(generateFn));
  }

  /**
   * Run a generative task on the results of a near thermal search.
   *
   * @param query    Near thermal query request.
   * @param generate Generative task.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   */
  public ResponseT nearThermal(NearThermal query, GenerativeTask generate) {
    return performRequest(query, generate);
  }

  /**
   * Run a generative task on the results of a near thermal search.
   *
   * @param thermal    Query thermal (base64-encoded).
   * @param generateFn Lambda expression for generative task parameters.
   * @param groupBy    Group-by clause.
   * @return Grouped query result.
   *
   * @see GroupBy
   * @see GenerativeResponseGrouped
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   */
  public GroupedResponseT nearThermal(String thermal,
      Function<GenerativeTask.Builder, ObjectBuilder<GenerativeTask>> generateFn, GroupBy groupBy) {
    return nearThermal(NearThermal.of(thermal), GenerativeTask.of(generateFn), groupBy);
  }

  /**
   * Run a generative task on the results of a near thermal search.
   *
   * @param searchTarget Query target (base64-encoded thermal).
   * @param fn           Lambda expression for optional search parameters.
   * @param generateFn   Lambda expression for generative task parameters.
   * @param groupBy      Group-by clause.
   * @return Grouped query result.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *
   * @see GroupBy
   * @see GenerativeResponseGrouped
   */
  public GroupedResponseT nearThermal(Target searchTarget, Function<NearThermal.Builder, ObjectBuilder<NearThermal>> fn,
      Function<GenerativeTask.Builder, ObjectBuilder<GenerativeTask>> generateFn,
      GroupBy groupBy) {
    return nearThermal(NearThermal.of(searchTarget, fn), GenerativeTask.of(generateFn), groupBy);
  }

  /**
   * Run a generative task on the results of a near thermal search.
   *
   * @param searchTarget Query target (base64-encoded thermal).
   * @param generateFn   Lambda expression for generative task parameters.
   * @param groupBy      Group-by clause.
   * @return Grouped query result.
   *
   * @see GroupBy
   * @see GenerativeResponseGrouped
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   */
  public GroupedResponseT nearThermal(Target searchTarget,
      Function<GenerativeTask.Builder, ObjectBuilder<GenerativeTask>> generateFn, GroupBy groupBy) {
    return nearThermal(NearThermal.of(searchTarget), GenerativeTask.of(generateFn), groupBy);
  }

  /**
   * Run a generative task on the results of a near thermal search.
   *
   * @param thermal    Query thermal (base64-encoded).
   * @param fn         Lambda expression for optional search parameters.
   * @param generateFn Lambda expression for generative task parameters.
   * @param groupBy    Group-by clause.
   * @return Grouped query result.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *
   * @see GroupBy
   * @see GenerativeResponseGrouped
   */
  public GroupedResponseT nearThermal(String thermal, Function<NearThermal.Builder, ObjectBuilder<NearThermal>> fn,
      Function<GenerativeTask.Builder, ObjectBuilder<GenerativeTask>> generateFn,
      GroupBy groupBy) {
    return nearThermal(NearThermal.of(thermal, fn), GenerativeTask.of(generateFn), groupBy);
  }

  /**
   * Run a generative task on the results of a near thermal search.
   *
   * @param query    Near thermal query request.
   * @param generate Generative task.
   * @param groupBy  Group-by clause.
   * @return Grouped query result.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *
   * @see GroupBy
   * @see GenerativeResponseGrouped
   */
  public GroupedResponseT nearThermal(NearThermal query, GenerativeTask generate, GroupBy groupBy) {
    return performRequest(query, generate, groupBy);
  }

  // NearDepth queries --------------------------------------------------------

  /**
   * Run a generative task on the results of a near depth search.
   *
   * @param depth      Query depth (base64-encoded).
   * @param generateFn Lambda expression for generative task parameters.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   */
  public ResponseT nearDepth(String depth,
      Function<GenerativeTask.Builder, ObjectBuilder<GenerativeTask>> generateFn) {
    return nearDepth(NearDepth.of(depth), GenerativeTask.of(generateFn));
  }

  /**
   * Run a generative task on the results of a near depth search.
   *
   * @param depth      Query depth (base64-encoded).
   * @param fn         Lambda expression for optional search parameters.
   * @param generateFn Lambda expression for generative task parameters.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   */
  public ResponseT nearDepth(String depth, Function<NearDepth.Builder, ObjectBuilder<NearDepth>> fn,
      Function<GenerativeTask.Builder, ObjectBuilder<GenerativeTask>> generateFn) {
    return nearDepth(NearDepth.of(depth, fn), GenerativeTask.of(generateFn));
  }

  /**
   * Run a generative task on the results of a near depth search.
   *
   * @param searchTarget Query target (base64-encoded depth).
   * @param generateFn   Lambda expression for generative task parameters.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   */
  public ResponseT nearDepth(Target searchTarget,
      Function<GenerativeTask.Builder, ObjectBuilder<GenerativeTask>> generateFn) {
    return nearDepth(NearDepth.of(searchTarget), GenerativeTask.of(generateFn));
  }

  /**
   * Run a generative task on the results of a near depth search.
   *
   * @param searchTarget Query target (base64-encoded depth).
   * @param fn           Lambda expression for optional search parameters.
   * @param generateFn   Lambda expression for generative task parameters.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   */
  public ResponseT nearDepth(Target searchTarget, Function<NearDepth.Builder, ObjectBuilder<NearDepth>> fn,
      Function<GenerativeTask.Builder, ObjectBuilder<GenerativeTask>> generateFn) {
    return nearDepth(NearDepth.of(searchTarget, fn), GenerativeTask.of(generateFn));
  }

  /**
   * Run a generative task on the results of a near depth search.
   *
   * @param query    Near depth query request.
   * @param generate Generative task.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   */
  public ResponseT nearDepth(NearDepth query, GenerativeTask generate) {
    return performRequest(query, generate);
  }

  /**
   * Run a generative task on the results of a near depth search.
   *
   * @param depth      Query depth (base64-encoded).
   * @param generateFn Lambda expression for generative task parameters.
   * @param groupBy    Group-by clause.
   * @return Grouped query result.
   *
   * @see GroupBy
   * @see GenerativeResponseGrouped
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   */
  public GroupedResponseT nearDepth(String depth,
      Function<GenerativeTask.Builder, ObjectBuilder<GenerativeTask>> generateFn, GroupBy groupBy) {
    return nearDepth(NearDepth.of(depth), GenerativeTask.of(generateFn), groupBy);
  }

  /**
   * Run a generative task on the results of a near depth search.
   *
   * @param searchTarget Query target (base64-encoded depth).
   * @param fn           Lambda expression for optional search parameters.
   * @param generateFn   Lambda expression for generative task parameters.
   * @param groupBy      Group-by clause.
   * @return Grouped query result.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *
   * @see GroupBy
   * @see GenerativeResponseGrouped
   */
  public GroupedResponseT nearDepth(Target searchTarget, Function<NearDepth.Builder, ObjectBuilder<NearDepth>> fn,
      Function<GenerativeTask.Builder, ObjectBuilder<GenerativeTask>> generateFn,
      GroupBy groupBy) {
    return nearDepth(NearDepth.of(searchTarget, fn), GenerativeTask.of(generateFn), groupBy);
  }

  /**
   * Run a generative task on the results of a near depth search.
   *
   * @param searchTarget Query target (base64-encoded depth).
   * @param generateFn   Lambda expression for generative task parameters.
   * @param groupBy      Group-by clause.
   * @return Grouped query result.
   *
   * @see GroupBy
   * @see GenerativeResponseGrouped
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   */
  public GroupedResponseT nearDepth(Target searchTarget,
      Function<GenerativeTask.Builder, ObjectBuilder<GenerativeTask>> generateFn, GroupBy groupBy) {
    return nearDepth(NearDepth.of(searchTarget), GenerativeTask.of(generateFn), groupBy);
  }

  /**
   * Run a generative task on the results of a near depth search.
   *
   * @param depth      Query depth (base64-encoded).
   * @param fn         Lambda expression for optional search parameters.
   * @param generateFn Lambda expression for generative task parameters.
   * @param groupBy    Group-by clause.
   * @return Grouped query result.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *
   * @see GroupBy
   * @see GenerativeResponseGrouped
   */
  public GroupedResponseT nearDepth(String depth, Function<NearDepth.Builder, ObjectBuilder<NearDepth>> fn,
      Function<GenerativeTask.Builder, ObjectBuilder<GenerativeTask>> generateFn,
      GroupBy groupBy) {
    return nearDepth(NearDepth.of(depth, fn), GenerativeTask.of(generateFn), groupBy);
  }

  /**
   * Run a generative task on the results of a near depth search.
   *
   * @param query    Near depth query request.
   * @param generate Generative task.
   * @param groupBy  Group-by clause.
   * @return Grouped query result.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *
   * @see GroupBy
   * @see GenerativeResponseGrouped
   */
  public GroupedResponseT nearDepth(NearDepth query, GenerativeTask generate, GroupBy groupBy) {
    return performRequest(query, generate, groupBy);
  }

  // NearImu queries --------------------------------------------------------

  /**
   * Run a generative task on the results of a near IMU search.
   *
   * @param imu        Query IMU (base64-encoded).
   * @param generateFn Lambda expression for generative task parameters.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   */
  public ResponseT nearImu(String imu,
      Function<GenerativeTask.Builder, ObjectBuilder<GenerativeTask>> generateFn) {
    return nearImu(NearImu.of(imu), GenerativeTask.of(generateFn));
  }

  /**
   * Run a generative task on the results of a near IMU search.
   *
   * @param imu        Query IMU (base64-encoded).
   * @param fn         Lambda expression for optional search parameters.
   * @param generateFn Lambda expression for generative task parameters.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   */
  public ResponseT nearImu(String imu, Function<NearImu.Builder, ObjectBuilder<NearImu>> fn,
      Function<GenerativeTask.Builder, ObjectBuilder<GenerativeTask>> generateFn) {
    return nearImu(NearImu.of(imu, fn), GenerativeTask.of(generateFn));
  }

  /**
   * Run a generative task on the results of a near IMU search.
   *
   * @param searchTarget Query target (base64-encoded IMU).
   * @param generateFn   Lambda expression for generative task parameters.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   */
  public ResponseT nearImu(Target searchTarget,
      Function<GenerativeTask.Builder, ObjectBuilder<GenerativeTask>> generateFn) {
    return nearImu(NearImu.of(searchTarget), GenerativeTask.of(generateFn));
  }

  /**
   * Run a generative task on the results of a near IMU search.
   *
   * @param searchTarget Query target (base64-encoded IMU).
   * @param fn           Lambda expression for optional search parameters.
   * @param generateFn   Lambda expression for generative task parameters.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   */
  public ResponseT nearImu(Target searchTarget, Function<NearImu.Builder, ObjectBuilder<NearImu>> fn,
      Function<GenerativeTask.Builder, ObjectBuilder<GenerativeTask>> generateFn) {
    return nearImu(NearImu.of(searchTarget, fn), GenerativeTask.of(generateFn));
  }

  /**
   * Run a generative task on the results of a near IMU search.
   *
   * @param query    Near IMU query request.
   * @param generate Generative task.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   */
  public ResponseT nearImu(NearImu query, GenerativeTask generate) {
    return performRequest(query, generate);
  }

  /**
   * Run a generative task on the results of a near IMU search.
   *
   * @param imu        Query IMU (base64-encoded).
   * @param generateFn Lambda expression for generative task parameters.
   * @param groupBy    Group-by clause.
   * @return Grouped query result.
   *
   * @see GroupBy
   * @see GenerativeResponseGrouped
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   */
  public GroupedResponseT nearImu(String imu,
      Function<GenerativeTask.Builder, ObjectBuilder<GenerativeTask>> generateFn, GroupBy groupBy) {
    return nearImu(NearImu.of(imu), GenerativeTask.of(generateFn), groupBy);
  }

  /**
   * Run a generative task on the results of a near IMU search.
   *
   * @param searchTarget Query target (base64-encoded IMU).
   * @param fn           Lambda expression for optional search parameters.
   * @param generateFn   Lambda expression for generative task parameters.
   * @param groupBy      Group-by clause.
   * @return Grouped query result.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *
   * @see GroupBy
   * @see GenerativeResponseGrouped
   */
  public GroupedResponseT nearImu(Target searchTarget, Function<NearImu.Builder, ObjectBuilder<NearImu>> fn,
      Function<GenerativeTask.Builder, ObjectBuilder<GenerativeTask>> generateFn,
      GroupBy groupBy) {
    return nearImu(NearImu.of(searchTarget, fn), GenerativeTask.of(generateFn), groupBy);
  }

  /**
   * Run a generative task on the results of a near IMU search.
   *
   * @param searchTarget Query target (base64-encoded IMU).
   * @param generateFn   Lambda expression for generative task parameters.
   * @param groupBy      Group-by clause.
   * @return Grouped query result.
   *
   * @see GroupBy
   * @see GenerativeResponseGrouped
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   */
  public GroupedResponseT nearImu(Target searchTarget,
      Function<GenerativeTask.Builder, ObjectBuilder<GenerativeTask>> generateFn, GroupBy groupBy) {
    return nearImu(NearImu.of(searchTarget), GenerativeTask.of(generateFn), groupBy);
  }

  /**
   * Run a generative task on the results of a near IMU search.
   *
   * @param imu        Query IMU (base64-encoded).
   * @param fn         Lambda expression for optional search parameters.
   * @param generateFn Lambda expression for generative task parameters.
   * @param groupBy    Group-by clause.
   * @return Grouped query result.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *
   * @see GroupBy
   * @see GenerativeResponseGrouped
   */
  public GroupedResponseT nearImu(String imu, Function<NearImu.Builder, ObjectBuilder<NearImu>> fn,
      Function<GenerativeTask.Builder, ObjectBuilder<GenerativeTask>> generateFn,
      GroupBy groupBy) {
    return nearImu(NearImu.of(imu, fn), GenerativeTask.of(generateFn), groupBy);
  }

  /**
   * Run a generative task on the results of a near IMU search.
   *
   * @param query    Near IMU query request.
   * @param generate Generative task.
   * @param groupBy  Group-by clause.
   * @return Grouped query result.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *
   * @see GroupBy
   * @see GenerativeResponseGrouped
   */
  public GroupedResponseT nearImu(NearImu query, GenerativeTask generate, GroupBy groupBy) {
    return performRequest(query, generate, groupBy);
  }
}
