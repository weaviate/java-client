package io.weaviate.client6.v1.api.collections.aggregate;

import java.util.List;
import java.util.function.Function;

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
import io.weaviate.client6.v1.api.collections.query.NearVideo;
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

  public ResponseT overAll(Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn) {
    return performRequest(Aggregation.of(fn));
  }

  public GroupedResponseT overAll(Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn, GroupBy groupBy) {
    return performRequest(Aggregation.of(fn), groupBy);
  }

  // Hybrid -------------------------------------------------------------------

  public ResponseT hybrid(String query, Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn) {
    return hybrid(Hybrid.of(query), fn);
  }

  public ResponseT hybrid(String query, Function<Hybrid.Builder, ObjectBuilder<Hybrid>> nv,
      Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn) {
    return hybrid(Hybrid.of(query, nv), fn);
  }

  public ResponseT hybrid(Hybrid filter, Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn) {
    return performRequest(Aggregation.of(filter, fn));
  }

  public GroupedResponseT hybrid(String query, Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn,
      GroupBy groupBy) {
    return hybrid(Hybrid.of(query), fn, groupBy);
  }

  public GroupedResponseT hybrid(String query, Function<Hybrid.Builder, ObjectBuilder<Hybrid>> nv,
      Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn, GroupBy groupBy) {
    return hybrid(Hybrid.of(query, nv), fn, groupBy);
  }

  public GroupedResponseT hybrid(Hybrid filter, Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn,
      GroupBy groupBy) {
    return performRequest(Aggregation.of(filter, fn), groupBy);
  }

  // NearVector ---------------------------------------------------------------

  public ResponseT nearVector(float[] vector, Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn) {
    return nearVector(NearVector.of(vector), fn);
  }

  public ResponseT nearVector(float[] vector, Function<NearVector.Builder, ObjectBuilder<NearVector>> nv,
      Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn) {
    return nearVector(NearVector.of(vector, nv), fn);
  }

  public ResponseT nearVector(NearVector filter, Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn) {
    return performRequest(Aggregation.of(filter, fn));
  }

  public GroupedResponseT nearVector(float[] vector, Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn,
      GroupBy groupBy) {
    return nearVector(NearVector.of(vector), fn, groupBy);
  }

  public GroupedResponseT nearVector(float[] vector, Function<NearVector.Builder, ObjectBuilder<NearVector>> nv,
      Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn, GroupBy groupBy) {
    return nearVector(NearVector.of(vector, nv), fn, groupBy);
  }

  public GroupedResponseT nearVector(NearVector filter, Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn,
      GroupBy groupBy) {
    return performRequest(Aggregation.of(filter, fn), groupBy);
  }

  // NearObject ---------------------------------------------------------------

  public ResponseT nearObject(String uuid, Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn) {
    return nearObject(NearObject.of(uuid), fn);
  }

  public ResponseT nearObject(String uuid, Function<NearObject.Builder, ObjectBuilder<NearObject>> nv,
      Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn) {
    return nearObject(NearObject.of(uuid, nv), fn);
  }

  public ResponseT nearObject(NearObject filter, Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn) {
    return performRequest(Aggregation.of(filter, fn));
  }

  public GroupedResponseT nearObject(String uuid, Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn,
      GroupBy groupBy) {
    return nearObject(NearObject.of(uuid), fn, groupBy);
  }

  public GroupedResponseT nearObject(String uuid, Function<NearObject.Builder, ObjectBuilder<NearObject>> nv,
      Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn, GroupBy groupBy) {
    return nearObject(NearObject.of(uuid, nv), fn, groupBy);
  }

  public GroupedResponseT nearObject(NearObject filter, Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn,
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

  // NearAudio ----------------------------------------------------------------

  public ResponseT nearAudio(String audio, Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn) {
    return nearAudio(NearAudio.of(audio), fn);
  }

  public ResponseT nearAudio(String audio, Function<NearAudio.Builder, ObjectBuilder<NearAudio>> nv,
      Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn) {
    return nearAudio(NearAudio.of(audio, nv), fn);
  }

  public ResponseT nearAudio(NearAudio filter, Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn) {
    return performRequest(Aggregation.of(filter, fn));
  }

  public GroupedResponseT nearAudio(String audio, Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn,
      GroupBy groupBy) {
    return nearAudio(NearAudio.of(audio), fn, groupBy);
  }

  public GroupedResponseT nearAudio(String audio, Function<NearAudio.Builder, ObjectBuilder<NearAudio>> nv,
      Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn, GroupBy groupBy) {
    return nearAudio(NearAudio.of(audio, nv), fn, groupBy);
  }

  public GroupedResponseT nearAudio(NearAudio filter, Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn,
      GroupBy groupBy) {
    return performRequest(Aggregation.of(filter, fn), groupBy);
  }

  // NearVideo ----------------------------------------------------------------

  public ResponseT nearVideo(String video, Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn) {
    return nearVideo(NearVideo.of(video), fn);
  }

  public ResponseT nearVideo(String video, Function<NearVideo.Builder, ObjectBuilder<NearVideo>> nv,
      Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn) {
    return nearVideo(NearVideo.of(video, nv), fn);
  }

  public ResponseT nearVideo(NearVideo filter, Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn) {
    return performRequest(Aggregation.of(filter, fn));
  }

  public GroupedResponseT nearVideo(String video, Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn,
      GroupBy groupBy) {
    return nearVideo(NearVideo.of(video), fn, groupBy);
  }

  public GroupedResponseT nearVideo(String video, Function<NearVideo.Builder, ObjectBuilder<NearVideo>> nv,
      Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn, GroupBy groupBy) {
    return nearVideo(NearVideo.of(video, nv), fn, groupBy);
  }

  public GroupedResponseT nearVideo(NearVideo filter, Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn,
      GroupBy groupBy) {
    return performRequest(Aggregation.of(filter, fn), groupBy);
  }

  // NearThermal --------------------------------------------------------------

  public ResponseT nearThermal(String thermal, Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn) {
    return nearThermal(NearThermal.of(thermal), fn);
  }

  public ResponseT nearThermal(String thermal, Function<NearThermal.Builder, ObjectBuilder<NearThermal>> nv,
      Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn) {
    return nearThermal(NearThermal.of(thermal, nv), fn);
  }

  public ResponseT nearThermal(NearThermal filter, Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn) {
    return performRequest(Aggregation.of(filter, fn));
  }

  public GroupedResponseT nearThermal(String thermal, Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn,
      GroupBy groupBy) {
    return nearThermal(NearThermal.of(thermal), fn, groupBy);
  }

  public GroupedResponseT nearThermal(String thermal, Function<NearThermal.Builder, ObjectBuilder<NearThermal>> nv,
      Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn, GroupBy groupBy) {
    return nearThermal(NearThermal.of(thermal, nv), fn, groupBy);
  }

  public GroupedResponseT nearThermal(NearThermal filter, Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn,
      GroupBy groupBy) {
    return performRequest(Aggregation.of(filter, fn), groupBy);
  }

  // NearDepth --------------------------------------------------------------

  public ResponseT nearDepth(String depth, Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn) {
    return nearDepth(NearDepth.of(depth), fn);
  }

  public ResponseT nearDepth(String depth, Function<NearDepth.Builder, ObjectBuilder<NearDepth>> nv,
      Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn) {
    return nearDepth(NearDepth.of(depth, nv), fn);
  }

  public ResponseT nearDepth(NearDepth filter, Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn) {
    return performRequest(Aggregation.of(filter, fn));
  }

  public GroupedResponseT nearDepth(String depth, Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn,
      GroupBy groupBy) {
    return nearDepth(NearDepth.of(depth), fn, groupBy);
  }

  public GroupedResponseT nearDepth(String depth, Function<NearDepth.Builder, ObjectBuilder<NearDepth>> nv,
      Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn, GroupBy groupBy) {
    return nearDepth(NearDepth.of(depth, nv), fn, groupBy);
  }

  public GroupedResponseT nearDepth(NearDepth filter, Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn,
      GroupBy groupBy) {
    return performRequest(Aggregation.of(filter, fn), groupBy);
  }

  // NearImu ------------------------------------------------------------------

  public ResponseT nearImu(String imu, Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn) {
    return nearImu(NearImu.of(imu), fn);
  }

  public ResponseT nearImu(String imu, Function<NearImu.Builder, ObjectBuilder<NearImu>> nv,
      Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn) {
    return nearImu(NearImu.of(imu, nv), fn);
  }

  public ResponseT nearImu(NearImu filter, Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn) {
    return performRequest(Aggregation.of(filter, fn));
  }

  public GroupedResponseT nearImu(String imu, Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn,
      GroupBy groupBy) {
    return nearImu(NearImu.of(imu), fn, groupBy);
  }

  public GroupedResponseT nearImu(String imu, Function<NearImu.Builder, ObjectBuilder<NearImu>> nv,
      Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn, GroupBy groupBy) {
    return nearImu(NearImu.of(imu, nv), fn, groupBy);
  }

  public GroupedResponseT nearImu(NearImu filter, Function<Aggregation.Builder, ObjectBuilder<Aggregation>> fn,
      GroupBy groupBy) {
    return performRequest(Aggregation.of(filter, fn), groupBy);
  }
}
