package io.weaviate.client6.v1.api.collections.aggregate;

import java.util.List;
import java.util.function.Function;

import io.weaviate.client6.v1.api.WeaviateApiException;
import io.weaviate.client6.v1.api.collections.CollectionHandleDefaults;
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
import io.weaviate.client6.v1.api.collections.query.Target;
import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.grpc.GrpcTransport;
import io.weaviate.client6.v1.internal.orm.CollectionDescriptor;

abstract class AbstractAggregateClient<ResponseT, GroupedResponseT> {
  protected final CollectionDescriptor<?> collection;
  protected final GrpcTransport transport;
  protected final CollectionHandleDefaults defaults;

  AbstractAggregateClient(
      CollectionDescriptor<?> collection,
      GrpcTransport transport,
      CollectionHandleDefaults defaults) {
    this.transport = transport;
    this.collection = collection;
    this.defaults = defaults;
  }

  AbstractAggregateClient(
      AbstractAggregateClient<ResponseT, GroupedResponseT> c,
      CollectionHandleDefaults defaults) {
    this(c.collection, c.transport, defaults);
  }

  protected abstract ResponseT performRequest(Aggregation aggregation);

  protected abstract GroupedResponseT performRequest(Aggregation aggregation, GroupBy groupBy);

  // OverAll ------------------------------------------------------------------

  /**
   * Aggregate metrics over all objects in this collection.
   *
   * @param fn Lambda expression for optional parameters.
   * @return Aggregation result.
   *
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @see AggregateResponse
   */
  public ResponseT overAll(Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn) {
    return performRequest(Aggregation.of(fn));
  }

  /**
   * Aggregate metrics over all objects in this collection.
   *
   * @param groupBy GroupBy clause.
   * @return Grouped aggregation result.
   *
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *
   * @see GroupBy
   * @see AggregateResponseGrouped
   */
  public GroupedResponseT overAll(GroupBy groupBy) {
    return performRequest(Aggregation.of(), groupBy);
  }

  /**
   * Aggregate metrics over all objects in this collection.
   *
   * @param fn      Lambda expression for optional aggregation parameters.
   * @param groupBy GroupBy clause.
   * @return Grouped aggregation result.
   *
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *
   * @see GroupBy
   * @see AggregateResponseGrouped
   */
  public GroupedResponseT overAll(Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn, GroupBy groupBy) {
    return performRequest(Aggregation.of(fn), groupBy);
  }

  // Hybrid -------------------------------------------------------------------

  /**
   * Aggregate results of a hybrid search query.
   *
   * @param query Query string.
   * @param fn    Lambda expression for optional aggregation parameters.
   * @return Aggregation result.
   *
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @see AggregateResponse
   */
  public ResponseT hybrid(String query, Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn) {
    return hybrid(Hybrid.of(query), fn);
  }

  /**
   * Aggregate results of a hybrid search query.
   *
   * @param searchTarget Query target.
   * @param fn           Lambda expression for optional aggregation parameters.
   * @return Aggregation result.
   *
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @see AggregateResponse
   */
  public ResponseT hybrid(Target searchTarget, Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn) {
    return hybrid(Hybrid.of(searchTarget), fn);
  }

  /**
   * Aggregate results of a hybrid search query.
   *
   * @param query  Query string.
   * @param hybrid Lambda expression for optional hybrid search parameters.
   * @param fn     Lambda expression for optional aggregation parameters.
   * @return Aggregation result.
   *
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @see AggregateResponse
   */
  public ResponseT hybrid(String query, Function<Hybrid.Builder, ObjectBuilder<Hybrid>> hybrid,
      Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn) {
    return hybrid(Hybrid.of(query, hybrid), fn);
  }

  /**
   * Aggregate results of a hybrid search query.
   *
   * @param searchTarget Query target.
   * @param hybrid       Lambda expression for optional hybrid search parameters.
   * @param fn           Lambda expression for optional aggregation parameters.
   * @return Aggregation result.
   *
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @see AggregateResponse
   */
  public ResponseT hybrid(Target searchTarget, Function<Hybrid.Builder, ObjectBuilder<Hybrid>> hybrid,
      Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn) {
    return hybrid(Hybrid.of(searchTarget, hybrid), fn);
  }

  /**
   * Aggregate results of a hybrid search query.
   *
   * @param filter Hybrid query request.
   * @param fn     Lambda expression for optional aggregation parameters.
   * @return Aggregation result.
   *
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @see AggregateResponse
   */
  public ResponseT hybrid(Hybrid filter, Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn) {
    return performRequest(Aggregation.of(filter, fn));
  }

  /**
   * Aggregate results of a hybrid search query.
   *
   * @param query   Query string.
   * @param fn      Lambda expression for optional aggregation parameters.
   * @param groupBy GroupBy clause.
   * @return Grouped aggregation result.
   *
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *
   * @see GroupBy
   * @see AggregateResponseGrouped
   */
  public GroupedResponseT hybrid(String query, Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn,
      GroupBy groupBy) {
    return hybrid(Hybrid.of(query), fn, groupBy);
  }

  /**
   * Aggregate results of a hybrid search query.
   *
   * @param searchTarget Query target.
   * @param fn           Lambda expression for optional aggregation parameters.
   * @param groupBy      GroupBy clause.
   * @return Grouped aggregation result.
   *
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *
   * @see GroupBy
   * @see AggregateResponseGrouped
   */
  public GroupedResponseT hybrid(Target searchTarget, Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn,
      GroupBy groupBy) {
    return hybrid(Hybrid.of(searchTarget), fn, groupBy);
  }

  /**
   * Aggregate results of a hybrid search query.
   *
   * @param query   Query string.
   * @param hybrid  Lambda expression for optional hybrid search parameters.
   * @param fn      Lambda expression for optional aggregation parameters.
   * @param groupBy GroupBy clause.
   * @return Grouped aggregation result.
   *
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *
   * @see GroupBy
   * @see AggregateResponseGrouped
   */
  public GroupedResponseT hybrid(String query, Function<Hybrid.Builder, ObjectBuilder<Hybrid>> hybrid,
      Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn, GroupBy groupBy) {
    return hybrid(Hybrid.of(query, hybrid), fn, groupBy);
  }

  /**
   * Aggregate results of a hybrid search query.
   *
   * @param searchTarget Query target.
   * @param hybrid       Lambda expression for optional hybrid search parameters.
   * @param fn           Lambda expression for optional aggregation parameters.
   * @param groupBy      GroupBy clause.
   * @return Grouped aggregation result.
   *
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *
   * @see GroupBy
   * @see AggregateResponseGrouped
   */
  public GroupedResponseT hybrid(Target searchTarget, Function<Hybrid.Builder, ObjectBuilder<Hybrid>> hybrid,
      Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn, GroupBy groupBy) {
    return hybrid(Hybrid.of(searchTarget, hybrid), fn, groupBy);
  }

  /**
   * Aggregate results of a hybrid search query.
   *
   * @param filter  Hybrid query request.
   * @param fn      Lambda expression for optional aggregation parameters.
   * @param groupBy GroupBy clause.
   * @return Grouped aggregation result.
   *
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *
   * @see GroupBy
   * @see AggregateResponseGrouped
   */
  public GroupedResponseT hybrid(Hybrid filter, Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn,
      GroupBy groupBy) {
    return performRequest(Aggregation.of(filter, fn), groupBy);
  }

  // NearVector ---------------------------------------------------------------

  /**
   * Aggregate results of a near vector query.
   *
   * @param vector Query vector.
   * @param fn     Lambda expression for optional aggregation parameters.
   * @return Aggregation result.
   *
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @see AggregateResponse
   */
  public ResponseT nearVector(float[] vector,
      Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn) {
    return nearVector(NearVector.of(Target.vector(vector)), fn);
  }

  /**
   * Aggregate results of a near vector query.
   *
   * @param vector Query vector.
   * @param nv     Lambda expression for optional near vector parameters.
   * @param fn     Lambda expression for optional aggregation parameters.
   * @return Aggregation result.
   *
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @see AggregateResponse
   */
  public ResponseT nearVector(float[] vector,
      Function<NearVector.Builder, ObjectBuilder<NearVector>> nv,
      Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn) {
    return nearVector(NearVector.of(Target.vector(vector), nv), fn);
  }

  /**
   * Aggregate results of a near vector query.
   *
   * @param vector Query vector.
   * @param fn     Lambda expression for optional aggregation parameters.
   * @return Aggregation result.
   *
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @see AggregateResponse
   */
  public ResponseT nearVector(float[][] vector, Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn) {
    return nearVector(NearVector.of(Target.vector(vector)), fn);
  }

  /**
   * Aggregate results of a near vector query.
   *
   * @param vector Query vector.
   * @param nv     Lambda expression for optional near vector parameters.
   * @param fn     Lambda expression for optional aggregation parameters.
   * @return Aggregation result.
   *
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @see AggregateResponse
   */
  public ResponseT nearVector(float[][] vector,
      Function<NearVector.Builder, ObjectBuilder<NearVector>> nv,
      Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn) {
    return nearVector(NearVector.of(Target.vector(vector), nv), fn);
  }

  /**
   * Aggregate results of a near vector query.
   *
   * @param searchTarget Query target.
   * @param fn           Lambda expression for optional aggregation parameters.
   * @return Aggregation result.
   *
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @see AggregateResponse
   */
  public ResponseT nearVector(NearVectorTarget searchTarget,
      Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn) {
    return nearVector(NearVector.of(searchTarget), fn);
  }

  /**
   * Aggregate results of a near vector query.
   *
   * @param searchTarget Query target.
   * @param nv           Lambda expression for optional near vector parameters.
   * @param fn           Lambda expression for optional aggregation parameters.
   * @return Aggregation result.
   *
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @see AggregateResponse
   */
  public ResponseT nearVector(NearVectorTarget searchTarget,
      Function<NearVector.Builder, ObjectBuilder<NearVector>> nv,
      Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn) {
    return nearVector(NearVector.of(searchTarget), fn);
  }

  /**
   * Aggregate results of a near vector query.
   *
   * @param filter Near vector query request.
   * @param fn     Lambda expression for optional aggregation parameters.
   * @return Aggregation result.
   *
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @see AggregateResponse
   */
  public ResponseT nearVector(NearVector filter,
      Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn) {
    return performRequest(Aggregation.of(filter, fn));
  }

  /**
   * Aggregate results of a near vector query.
   *
   * @param vector  Query vector.
   * @param fn      Lambda expression for optional aggregation parameters.
   * @param groupBy GroupBy clause.
   * @return Grouped aggregation result.
   *
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *
   * @see GroupBy
   * @see AggregateResponseGrouped
   */
  public GroupedResponseT nearVector(float[] vector,
      Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn,
      GroupBy groupBy) {
    return nearVector(NearVector.of(Target.vector(vector)), fn, groupBy);
  }

  /**
   * Aggregate results of a near vector query.
   *
   * @param vector  Query vector.
   * @param nv      Lambda expression for optional near vector parameters.
   * @param fn      Lambda expression for optional aggregation parameters.
   * @param groupBy GroupBy clause.
   * @return Grouped aggregation result.
   *
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *
   * @see GroupBy
   * @see AggregateResponseGrouped
   */
  public GroupedResponseT nearVector(float[] vector,
      Function<NearVector.Builder, ObjectBuilder<NearVector>> nv,
      Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn,
      GroupBy groupBy) {
    return nearVector(NearVector.of(Target.vector(vector), nv), fn, groupBy);
  }

  /**
   * Aggregate results of a near vector query.
   *
   * @param vector  Query vector.
   * @param fn      Lambda expression for optional aggregation parameters.
   * @param groupBy GroupBy clause.
   * @return Grouped aggregation result.
   *
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *
   * @see GroupBy
   * @see AggregateResponseGrouped
   */
  public GroupedResponseT nearVector(float[][] vector,
      Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn,
      GroupBy groupBy) {
    return nearVector(NearVector.of(Target.vector(vector)), fn, groupBy);
  }

  /**
   * Aggregate results of a near vector query.
   *
   * @param vector  Query vector.
   * @param nv      Lambda expression for optional near vector parameters.
   * @param fn      Lambda expression for optional aggregation parameters.
   * @param groupBy GroupBy clause.
   * @return Grouped aggregation result.
   *
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *
   * @see GroupBy
   * @see AggregateResponseGrouped
   */
  public GroupedResponseT nearVector(float[][] vector,
      Function<NearVector.Builder, ObjectBuilder<NearVector>> nv,
      Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn,
      GroupBy groupBy) {
    return nearVector(NearVector.of(Target.vector(vector), nv), fn, groupBy);
  }

  /**
   * Aggregate results of a near vector query.
   *
   * @param searchTarget Query target.
   * @param fn           Lambda expression for optional aggregation parameters.
   * @param groupBy      GroupBy clause.
   * @return Grouped aggregation result.
   *
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *
   * @see GroupBy
   * @see AggregateResponseGrouped
   */
  public GroupedResponseT nearVector(NearVectorTarget searchTarget,
      Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn,
      GroupBy groupBy) {
    return nearVector(NearVector.of(searchTarget), fn, groupBy);
  }

  /**
   * Aggregate results of a near vector query.
   *
   * @param searchTarget Query target.
   * @param nv           Lambda expression for optional near vector parameters.
   * @param fn           Lambda expression for optional aggregation parameters.
   * @param groupBy      GroupBy clause.
   * @return Grouped aggregation result.
   *
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *
   * @see GroupBy
   * @see AggregateResponseGrouped
   */
  public GroupedResponseT nearVector(NearVectorTarget searchTarget,
      Function<NearVector.Builder, ObjectBuilder<NearVector>> nv,
      Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn, GroupBy groupBy) {
    return nearVector(NearVector.of(searchTarget, nv), fn, groupBy);
  }

  /**
   * Aggregate results of a near vector query.
   *
   * @param filter  Near vector query request.
   * @param fn      Lambda expression for optional aggregation parameters.
   * @param groupBy GroupBy clause.
   * @return Grouped aggregation result.
   *
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *
   * @see GroupBy
   * @see AggregateResponseGrouped
   */
  public GroupedResponseT nearVector(NearVector filter,
      Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn,
      GroupBy groupBy) {
    return performRequest(Aggregation.of(filter, fn), groupBy);
  }

  // NearObject ---------------------------------------------------------------

  /**
   * Aggregate results of a near object query.
   *
   * @param uuid Query object UUID.
   * @param fn   Lambda expression for optional aggregation parameters.
   * @return Aggregation result.
   *
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @see AggregateResponse
   */
  public ResponseT nearObject(String uuid, Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn) {
    return nearObject(NearObject.of(uuid), fn);
  }

  /**
   * Aggregate results of a near object query.
   *
   * @param uuid Query object UUID.
   * @param nobj Lambda expression for optional near object parameters.
   * @param fn   Lambda expression for optional aggregation parameters.
   * @return Aggregation result.
   *
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @see AggregateResponse
   */
  public ResponseT nearObject(String uuid, Function<NearObject.Builder, ObjectBuilder<NearObject>> nobj,
      Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn) {
    return nearObject(NearObject.of(uuid, nobj), fn);
  }

  /**
   * Aggregate results of a near object query.
   *
   * @param filter Near object query request.
   * @param fn     Lambda expression for optional aggregation parameters.
   * @return Aggregation result.
   *
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @see AggregateResponse
   */
  public ResponseT nearObject(NearObject filter, Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn) {
    return performRequest(Aggregation.of(filter, fn));
  }

  /**
   * Aggregate results of a near object query.
   *
   * @param uuid    Query object UUID.
   * @param fn      Lambda expression for optional aggregation parameters.
   * @param groupBy GroupBy clause.
   * @return Grouped aggregation result.
   *
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *
   * @see GroupBy
   * @see AggregateResponseGrouped
   */
  public GroupedResponseT nearObject(String uuid, Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn,
      GroupBy groupBy) {
    return nearObject(NearObject.of(uuid), fn, groupBy);
  }

  /**
   * Aggregate results of a near object query.
   *
   * @param uuid    Query object UUID.
   * @param nobj    Lambda expression for optional near object parameters.
   * @param fn      Lambda expression for optional aggregation parameters.
   * @param groupBy GroupBy clause.
   * @return Grouped aggregation result.
   *
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *
   * @see GroupBy
   * @see AggregateResponseGrouped
   */
  public GroupedResponseT nearObject(String uuid, Function<NearObject.Builder, ObjectBuilder<NearObject>> nobj,
      Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn, GroupBy groupBy) {
    return nearObject(NearObject.of(uuid, nobj), fn, groupBy);
  }

  /**
   * Aggregate results of a near object query.
   *
   * @param filter  Near object query request.
   * @param fn      Lambda expression for optional aggregation parameters.
   * @param groupBy GroupBy clause.
   * @return Grouped aggregation result.
   *
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *
   * @see GroupBy
   * @see AggregateResponseGrouped
   */
  public GroupedResponseT nearObject(NearObject filter, Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn,
      GroupBy groupBy) {
    return performRequest(Aggregation.of(filter, fn), groupBy);
  }

  // NearText -----------------------------------------------------------------

  /**
   * Aggregate results of a near text query.
   *
   * @param text Query string.
   * @param fn   Lambda expression for optional aggregation parameters.
   * @return Aggregation result.
   *
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @see AggregateResponse
   */
  public ResponseT nearText(String text, Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn) {
    return nearText(NearText.of(text), fn);
  }

  /**
   * Aggregate results of a near text query.
   *
   * @param concepts Query concepts.
   * @param fn       Lambda expression for optional aggregation parameters.
   * @return Aggregation result.
   *
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @see AggregateResponse
   */
  public ResponseT nearText(List<String> concepts, Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn) {
    return nearText(NearText.of(Target.text(concepts)), fn);
  }

  /**
   * Aggregate results of a near text query.
   *
   * @param text Query string.
   * @param nt   Lambda expression for optional near text parameters.
   * @param fn   Lambda expression for optional aggregation parameters.
   * @return Aggregation result.
   *
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @see AggregateResponse
   */
  public ResponseT nearText(String text, Function<NearText.Builder, ObjectBuilder<NearText>> nt,
      Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn) {
    return nearText(NearText.of(Target.text(List.of(text)), nt), fn);
  }

  /**
   * Aggregate results of a near text query.
   *
   * @param concepts Query concepts.
   * @param nt       Lambda expression for optional near text parameters.
   * @param fn       Lambda expression for optional aggregation parameters.
   * @return Aggregation result.
   *
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @see AggregateResponse
   */
  public ResponseT nearText(List<String> concepts, Function<NearText.Builder, ObjectBuilder<NearText>> nt,
      Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn) {
    return nearText(NearText.of(Target.text(concepts), nt), fn);
  }

  /**
   * Aggregate results of a near text query.
   *
   * @param filter Near text query request.
   * @param fn     Lambda expression for optional aggregation parameters.
   * @return Aggregation result.
   *
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   */
  public ResponseT nearText(NearText filter, Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn) {
    return performRequest(Aggregation.of(filter, fn));
  }

  /**
   * Aggregate results of a near text query.
   *
   * @param text    Query string.
   * @param fn      Lambda expression for optional aggregation parameters.
   * @param groupBy GroupBy clause.
   * @return Grouped aggregation result.
   *
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *
   * @see GroupBy
   * @see AggregateResponseGrouped
   */
  public GroupedResponseT nearText(String text, Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn,
      GroupBy groupBy) {
    return nearText(NearText.of(text), fn, groupBy);
  }

  /**
   * Aggregate results of a near text query.
   *
   * @param concepts Query concepts.
   * @param fn       Lambda expression for optional aggregation parameters.
   * @param groupBy  GroupBy clause.
   * @return Grouped aggregation result.
   *
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *
   * @see GroupBy
   * @see AggregateResponseGrouped
   */
  public GroupedResponseT nearText(List<String> concepts, Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn,
      GroupBy groupBy) {
    return nearText(NearText.of(Target.text(concepts)), fn, groupBy);
  }

  /**
   * Aggregate results of a near text query.
   *
   * @param text    Query string.
   * @param nt      Lambda expression for optional near text parameters.
   * @param fn      Lambda expression for optional aggregation parameters.
   * @param groupBy GroupBy clause.
   * @return Grouped aggregation result.
   *
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *
   * @see GroupBy
   * @see AggregateResponseGrouped
   */
  public GroupedResponseT nearText(String text, Function<NearText.Builder, ObjectBuilder<NearText>> nt,
      Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn, GroupBy groupBy) {
    return nearText(NearText.of(text, nt), fn, groupBy);
  }

  /**
   * Aggregate results of a near text query.
   *
   * @param concepts Query concepts.
   * @param nt       Lambda expression for optional near text parameters.
   * @param fn       Lambda expression for optional aggregation parameters.
   * @param groupBy  GroupBy clause.
   * @return Grouped aggregation result.
   *
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *
   * @see GroupBy
   * @see AggregateResponseGrouped
   */
  public GroupedResponseT nearText(List<String> concepts, Function<NearText.Builder, ObjectBuilder<NearText>> nt,
      Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn, GroupBy groupBy) {
    return nearText(NearText.of(Target.text(concepts), nt), fn, groupBy);
  }

  /**
   * Aggregate results of a near text query.
   *
   * @param filter  Near text query request.
   * @param fn      Lambda expression for optional aggregation parameters.
   * @param groupBy GroupBy clause.
   * @return Grouped aggregation result.
   *
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *
   * @see GroupBy
   * @see AggregateResponseGrouped
   */
  public GroupedResponseT nearText(NearText filter, Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn,
      GroupBy groupBy) {
    return performRequest(Aggregation.of(filter, fn), groupBy);
  }

  // NearImage ----------------------------------------------------------------

  /**
   * Aggregate results of a near image query.
   *
   * @param image Query image.
   * @param fn    Lambda expression for optional aggregation parameters.
   * @return Aggregation result.
   *
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @see AggregateResponse
   */
  public ResponseT nearImage(String image, Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn) {
    return nearImage(NearImage.of(image), fn);
  }

  /**
   * Aggregate results of a near image query.
   *
   * @param image Query image.
   * @param ni    Lambda expression for optional near image parameters.
   * @param fn    Lambda expression for optional aggregation parameters.
   * @return Aggregation result.
   *
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @see AggregateResponse
   */
  public ResponseT nearImage(String image, Function<NearImage.Builder, ObjectBuilder<NearImage>> ni,
      Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn) {
    return nearImage(NearImage.of(image, ni), fn);
  }

  /**
   * Aggregate results of a near image query.
   *
   * @param filter Near image query request.
   * @param fn     Lambda expression for optional aggregation parameters.
   * @return Aggregation result.
   *
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @see AggregateResponse
   */
  public ResponseT nearImage(NearImage filter, Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn) {
    return performRequest(Aggregation.of(filter, fn));
  }

  /**
   * Aggregate results of a near image query.
   *
   * @param image   Query image.
   * @param fn      Lambda expression for optional aggregation parameters.
   * @param groupBy GroupBy clause.
   * @return Grouped aggregation result.
   *
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *
   * @see GroupBy
   * @see AggregateResponseGrouped
   */
  public GroupedResponseT nearImage(String image, Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn,
      GroupBy groupBy) {
    return nearImage(NearImage.of(image), fn, groupBy);
  }

  /**
   * Aggregate results of a near image query.
   *
   * @param image   Query image.
   * @param ni      Lambda expression for optional near image parameters.
   * @param fn      Lambda expression for optional aggregation parameters.
   * @param groupBy GroupBy clause.
   * @return Grouped aggregation result.
   *
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *
   * @see GroupBy
   * @see AggregateResponseGrouped
   */
  public GroupedResponseT nearImage(String image, Function<NearImage.Builder, ObjectBuilder<NearImage>> ni,
      Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn, GroupBy groupBy) {
    return nearImage(NearImage.of(image, ni), fn, groupBy);
  }

  /**
   * Aggregate results of a near image query.
   *
   * @param filter  Near image query request.
   * @param fn      Lambda expression for optional aggregation parameters.
   * @param groupBy GroupBy clause.
   * @return Grouped aggregation result.
   *
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *
   * @see GroupBy
   * @see AggregateResponseGrouped
   */
  public GroupedResponseT nearImage(NearImage filter, Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn,
      GroupBy groupBy) {
    return performRequest(Aggregation.of(filter, fn), groupBy);
  }

  // NearAudio ----------------------------------------------------------------

  /**
   * Aggregate results of a near audio query.
   *
   * @param audio Query audio.
   * @param fn    Lambda expression for optional aggregation parameters.
   * @return Aggregation result.
   *
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @see AggregateResponse
   */
  public ResponseT nearAudio(String audio, Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn) {
    return nearAudio(NearAudio.of(audio), fn);
  }

  /**
   * Aggregate results of a near audio query.
   *
   * @param audio Query audio.
   * @param na    Lambda expression for optional near audio parameters.
   * @param fn    Lambda expression for optional aggregation parameters.
   * @return Aggregation result.
   *
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @see AggregateResponse
   */
  public ResponseT nearAudio(String audio, Function<NearAudio.Builder, ObjectBuilder<NearAudio>> na,
      Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn) {
    return nearAudio(NearAudio.of(audio, na), fn);
  }

  /**
   * Aggregate results of a near audio query.
   *
   * @param filter Near audio query request.
   * @param fn     Lambda expression for optional aggregation parameters.
   * @return Aggregation result.
   *
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @see AggregateResponse
   */
  public ResponseT nearAudio(NearAudio filter, Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn) {
    return performRequest(Aggregation.of(filter, fn));
  }

  /**
   * Aggregate results of a near audio query.
   *
   * @param audio   Query audio.
   * @param fn      Lambda expression for optional aggregation parameters.
   * @param groupBy GroupBy clause.
   * @return Grouped aggregation result.
   *
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *
   * @see GroupBy
   * @see AggregateResponseGrouped
   */
  public GroupedResponseT nearAudio(String audio, Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn,
      GroupBy groupBy) {
    return nearAudio(NearAudio.of(audio), fn, groupBy);
  }

  /**
   * Aggregate results of a near audio query.
   *
   * @param audio   Query audio.
   * @param na      Lambda expression for optional near audio parameters.
   * @param fn      Lambda expression for optional aggregation parameters.
   * @param groupBy GroupBy clause.
   * @return Grouped aggregation result.
   *
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *
   * @see GroupBy
   * @see AggregateResponseGrouped
   */
  public GroupedResponseT nearAudio(String audio, Function<NearAudio.Builder, ObjectBuilder<NearAudio>> na,
      Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn, GroupBy groupBy) {
    return nearAudio(NearAudio.of(audio, na), fn, groupBy);
  }

  /**
   * Aggregate results of a near audio query.
   *
   * @param filter  Near audio query request.
   * @param fn      Lambda expression for optional aggregation parameters.
   * @param groupBy GroupBy clause.
   * @return Grouped aggregation result.
   *
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *
   * @see GroupBy
   * @see AggregateResponseGrouped
   */
  public GroupedResponseT nearAudio(NearAudio filter, Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn,
      GroupBy groupBy) {
    return performRequest(Aggregation.of(filter, fn), groupBy);
  }

  // NearVideo ----------------------------------------------------------------

  /**
   * Aggregate results of a near video query.
   *
   * @param video Query video.
   * @param fn    Lambda expression for optional aggregation parameters.
   * @return Aggregation result.
   *
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @see AggregateResponse
   */
  public ResponseT nearVideo(String video, Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn) {
    return nearVideo(NearVideo.of(video), fn);
  }

  /**
   * Aggregate results of a near video query.
   *
   * @param video Query video.
   * @param nv    Lambda expression for optional near video parameters.
   * @param fn    Lambda expression for optional aggregation parameters.
   * @return Aggregation result.
   *
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @see AggregateResponse
   */
  public ResponseT nearVideo(String video, Function<NearVideo.Builder, ObjectBuilder<NearVideo>> nv,
      Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn) {
    return nearVideo(NearVideo.of(video, nv), fn);
  }

  /**
   * Aggregate results of a near video query.
   *
   * @param filter Near video query request.
   * @param fn     Lambda expression for optional aggregation parameters.
   * @return Aggregation result.
   *
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @see AggregateResponse
   */
  public ResponseT nearVideo(NearVideo filter, Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn) {
    return performRequest(Aggregation.of(filter, fn));
  }

  /**
   * Aggregate results of a near video query.
   *
   * @param video   Query video.
   * @param fn      Lambda expression for optional aggregation parameters.
   * @param groupBy GroupBy clause.
   * @return Grouped aggregation result.
   *
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *
   * @see GroupBy
   * @see AggregateResponseGrouped
   */
  public GroupedResponseT nearVideo(String video, Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn,
      GroupBy groupBy) {
    return nearVideo(NearVideo.of(video), fn, groupBy);
  }

  /**
   * Aggregate results of a near video query.
   *
   * @param video   Query video.
   * @param nv      Lambda expression for optional near video parameters.
   * @param fn      Lambda expression for optional aggregation parameters.
   * @param groupBy GroupBy clause.
   * @return Grouped aggregation result.
   *
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *
   * @see GroupBy
   * @see AggregateResponseGrouped
   */
  public GroupedResponseT nearVideo(String video, Function<NearVideo.Builder, ObjectBuilder<NearVideo>> nv,
      Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn, GroupBy groupBy) {
    return nearVideo(NearVideo.of(video, nv), fn, groupBy);
  }

  /**
   * Aggregate results of a near video query.
   *
   * @param filter  Near video query request.
   * @param fn      Lambda expression for optional aggregation parameters.
   * @param groupBy GroupBy clause.
   * @return Grouped aggregation result.
   *
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *
   * @see GroupBy
   * @see AggregateResponseGrouped
   */
  public GroupedResponseT nearVideo(NearVideo filter, Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn,
      GroupBy groupBy) {
    return performRequest(Aggregation.of(filter, fn), groupBy);
  }

  // NearThermal --------------------------------------------------------------

  /**
   * Aggregate results of a near thermal query.
   *
   * @param thermal Query thermal.
   * @param fn      Lambda expression for optional aggregation parameters.
   * @return Aggregation result.
   *
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @see AggregateResponse
   */
  public ResponseT nearThermal(String thermal, Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn) {
    return nearThermal(NearThermal.of(thermal), fn);
  }

  /**
   * Aggregate results of a near thermal query.
   *
   * @param thermal Query thermal.
   * @param nt      Lambda expression for optional near thermal parameters.
   * @param fn      Lambda expression for optional aggregation parameters.
   * @return Aggregation result.
   *
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @see AggregateResponse
   */
  public ResponseT nearThermal(String thermal, Function<NearThermal.Builder, ObjectBuilder<NearThermal>> nt,
      Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn) {
    return nearThermal(NearThermal.of(thermal, nt), fn);
  }

  /**
   * Aggregate results of a near thermal query.
   *
   * @param filter Near thermal query request.
   * @param fn     Lambda expression for optional aggregation parameters.
   * @return Aggregation result.
   *
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @see AggregateResponse
   */
  public ResponseT nearThermal(NearThermal filter, Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn) {
    return performRequest(Aggregation.of(filter, fn));
  }

  /**
   * Aggregate results of a near thermal query.
   *
   * @param thermal Query thermal.
   * @param fn      Lambda expression for optional aggregation parameters.
   * @param groupBy GroupBy clause.
   * @return Grouped aggregation result.
   *
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *
   * @see GroupBy
   * @see AggregateResponseGrouped
   */
  public GroupedResponseT nearThermal(String thermal, Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn,
      GroupBy groupBy) {
    return nearThermal(NearThermal.of(thermal), fn, groupBy);
  }

  /**
   * Aggregate results of a near thermal query.
   *
   * @param thermal Query thermal.
   * @param nt      Lambda expression for optional near thermal parameters.
   * @param fn      Lambda expression for optional aggregation parameters.
   * @param groupBy GroupBy clause.
   * @return Grouped aggregation result.
   *
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *
   * @see GroupBy
   * @see AggregateResponseGrouped
   */
  public GroupedResponseT nearThermal(String thermal, Function<NearThermal.Builder, ObjectBuilder<NearThermal>> nt,
      Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn, GroupBy groupBy) {
    return nearThermal(NearThermal.of(thermal, nt), fn, groupBy);
  }

  /**
   * Aggregate results of a near thermal query.
   *
   * @param filter  Near thermal query request.
   * @param fn      Lambda expression for optional aggregation parameters.
   * @param groupBy GroupBy clause.
   * @return Grouped aggregation result.
   *
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *
   * @see GroupBy
   * @see AggregateResponseGrouped
   */
  public GroupedResponseT nearThermal(NearThermal filter, Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn,
      GroupBy groupBy) {
    return performRequest(Aggregation.of(filter, fn), groupBy);
  }

  // NearDepth --------------------------------------------------------------

  /**
   * Aggregate results of a near depth query.
   *
   * @param depth Query depth.
   * @param fn    Lambda expression for optional aggregation parameters.
   * @return Aggregation result.
   *
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @see AggregateResponse
   */
  public ResponseT nearDepth(String depth, Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn) {
    return nearDepth(NearDepth.of(depth), fn);
  }

  /**
   * Aggregate results of a near depth query.
   *
   * @param depth Query depth.
   * @param nd    Lambda expression for optional near depth parameters.
   * @param fn    Lambda expression for optional aggregation parameters.
   * @return Aggregation result.
   *
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @see AggregateResponse
   */
  public ResponseT nearDepth(String depth, Function<NearDepth.Builder, ObjectBuilder<NearDepth>> nd,
      Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn) {
    return nearDepth(NearDepth.of(depth, nd), fn);
  }

  /**
   * Aggregate results of a near depth query.
   *
   * @param filter Near depth query request.
   * @param fn     Lambda expression for optional aggregation parameters.
   * @return Aggregation result.
   *
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @see AggregateResponse
   */
  public ResponseT nearDepth(NearDepth filter, Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn) {
    return performRequest(Aggregation.of(filter, fn));
  }

  /**
   * Aggregate results of a near depth query.
   *
   * @param depth   Query depth.
   * @param fn      Lambda expression for optional aggregation parameters.
   * @param groupBy GroupBy clause.
   * @return Grouped aggregation result.
   *
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *
   * @see GroupBy
   * @see AggregateResponseGrouped
   */
  public GroupedResponseT nearDepth(String depth, Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn,
      GroupBy groupBy) {
    return nearDepth(NearDepth.of(depth), fn, groupBy);
  }

  /**
   * Aggregate results of a near depth query.
   *
   * @param depth   Query depth.
   * @param nd      Lambda expression for optional near depth parameters.
   * @param fn      Lambda expression for optional aggregation parameters.
   * @param groupBy GroupBy clause.
   * @return Grouped aggregation result.
   *
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *
   * @see GroupBy
   * @see AggregateResponseGrouped
   */
  public GroupedResponseT nearDepth(String depth, Function<NearDepth.Builder, ObjectBuilder<NearDepth>> nd,
      Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn, GroupBy groupBy) {
    return nearDepth(NearDepth.of(depth, nd), fn, groupBy);
  }

  /**
   * Aggregate results of a near depth query.
   *
   * @param filter  Near depth query request.
   * @param fn      Lambda expression for optional aggregation parameters.
   * @param groupBy GroupBy clause.
   * @return Grouped aggregation result.
   *
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *
   * @see GroupBy
   * @see AggregateResponseGrouped
   */
  public GroupedResponseT nearDepth(NearDepth filter, Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn,
      GroupBy groupBy) {
    return performRequest(Aggregation.of(filter, fn), groupBy);
  }

  // NearImu ------------------------------------------------------------------

  /**
   * Aggregate results of a near IMU query.
   *
   * @param imu Query IMU.
   * @param fn  Lambda expression for optional aggregation parameters.
   * @return Aggregation result.
   *
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @see AggregateResponse
   */
  public ResponseT nearImu(String imu, Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn) {
    return nearImu(NearImu.of(imu), fn);
  }

  /**
   * Aggregate results of a near IMU query.
   *
   * @param imu Query IMU.
   * @param ni  Lambda expression for optional near IMU parameters.
   * @param fn  Lambda expression for optional aggregation parameters.
   * @return Aggregation result.
   *
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @see AggregateResponse
   */
  public ResponseT nearImu(String imu, Function<NearImu.Builder, ObjectBuilder<NearImu>> ni,
      Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn) {
    return nearImu(NearImu.of(imu, ni), fn);
  }

  /**
   * Aggregate results of a near IMU query.
   *
   * @param filter Near IMU query request.
   * @param fn     Lambda expression for optional aggregation parameters.
   * @return Aggregation result.
   *
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @see AggregateResponse
   */
  public ResponseT nearImu(NearImu filter, Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn) {
    return performRequest(Aggregation.of(filter, fn));
  }

  /**
   * Aggregate results of a near IMU query.
   *
   * @param imu     Query IMU.
   * @param fn      Lambda expression for optional aggregation parameters.
   * @param groupBy GroupBy clause.
   * @return Grouped aggregation result.
   *
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *
   * @see GroupBy
   * @see AggregateResponseGrouped
   */
  public GroupedResponseT nearImu(String imu, Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn,
      GroupBy groupBy) {
    return nearImu(NearImu.of(imu), fn, groupBy);
  }

  /**
   * Aggregate results of a near IMU query.
   *
   * @param imu     Query IMU.
   * @param ni      Lambda expression for optional near IMU parameters.
   * @param fn      Lambda expression for optional aggregation parameters.
   * @param groupBy GroupBy clause.
   * @return Grouped aggregation result.
   *
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *
   * @see GroupBy
   * @see AggregateResponseGrouped
   */
  public GroupedResponseT nearImu(String imu, Function<NearImu.Builder, ObjectBuilder<NearImu>> ni,
      Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn, GroupBy groupBy) {
    return nearImu(NearImu.of(imu, ni), fn, groupBy);
  }

  /**
   * Aggregate results of a near IMU query.
   *
   * @param filter  Near IMU query request.
   * @param fn      Lambda expression for optional aggregation parameters.
   * @param groupBy GroupBy clause.
   * @return Grouped aggregation result.
   *
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   *
   * @see GroupBy
   * @see AggregateResponseGrouped
   */
  public GroupedResponseT nearImu(NearImu filter, Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn,
      GroupBy groupBy) {
    return performRequest(Aggregation.of(filter, fn), groupBy);
  }
}
