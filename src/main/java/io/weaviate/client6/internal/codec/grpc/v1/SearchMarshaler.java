package io.weaviate.client6.internal.codec.grpc.v1;

import org.apache.commons.lang3.StringUtils;

import com.google.protobuf.util.JsonFormat;

import io.weaviate.client6.grpc.protocol.v1.WeaviateProtoBaseSearch;
import io.weaviate.client6.grpc.protocol.v1.WeaviateProtoBaseSearch.NearTextSearch;
import io.weaviate.client6.grpc.protocol.v1.WeaviateProtoSearchGet;
import io.weaviate.client6.grpc.protocol.v1.WeaviateProtoSearchGet.MetadataRequest;
import io.weaviate.client6.grpc.protocol.v1.WeaviateProtoSearchGet.PropertiesRequest;
import io.weaviate.client6.grpc.protocol.v1.WeaviateProtoSearchGet.SearchRequest;
import io.weaviate.client6.internal.GRPC;
import io.weaviate.client6.internal.codec.grpc.GrpcMarshaler;
import io.weaviate.client6.v1.collections.query.CommonQueryOptions;
import io.weaviate.client6.v1.collections.query.NearImage;
import io.weaviate.client6.v1.collections.query.NearText;
import io.weaviate.client6.v1.collections.query.NearVector;

public class SearchMarshaler implements GrpcMarshaler<SearchRequest> {
  private final WeaviateProtoSearchGet.SearchRequest.Builder req = WeaviateProtoSearchGet.SearchRequest.newBuilder();

  public SearchMarshaler(String collectionName) {
    req.setCollection(collectionName);
    req.setUses123Api(true);
    req.setUses125Api(true);
    req.setUses127Api(true);
  }

  public SearchMarshaler addGroupBy(NearVector.GroupBy gb) {
    var groupBy = WeaviateProtoSearchGet.GroupBy.newBuilder();
    groupBy.addPath(gb.property());
    groupBy.setNumberOfGroups(gb.maxGroups());
    groupBy.setObjectsPerGroup(gb.maxObjectsPerGroup());
    req.setGroupBy(groupBy);
    return this;
  }

  public SearchMarshaler addGroupBy(NearText.GroupBy gb) {
    var groupBy = WeaviateProtoSearchGet.GroupBy.newBuilder();
    groupBy.addPath(gb.property());
    groupBy.setNumberOfGroups(gb.maxGroups());
    groupBy.setObjectsPerGroup(gb.maxObjectsPerGroup());
    req.setGroupBy(groupBy);
    return this;
  }

  public SearchMarshaler addNearVector(NearVector nv) {
    setCommon(nv.common());

    var nearVector = WeaviateProtoBaseSearch.NearVector.newBuilder();
    nearVector.setVectorBytes(GRPC.toByteString(nv.vector()));

    if (nv.certainty() != null) {
      nearVector.setCertainty(nv.certainty());
    } else if (nv.distance() != null) {
      nearVector.setDistance(nv.distance());
    }

    // TODO: add targets, vector_for_targets
    req.setNearVector(nearVector);
    return this;
  }

  public SearchMarshaler addNearImage(NearImage ni) {
    setCommon(ni.common());

    var nearImage = WeaviateProtoBaseSearch.NearImageSearch.newBuilder();
    nearImage.setImage(ni.image());

    if (ni.certainty() != null) {
      nearImage.setCertainty(ni.certainty());
    } else if (ni.distance() != null) {
      nearImage.setDistance(ni.distance());
    }

    req.setNearImage(nearImage);
    return this;
  }

  public SearchMarshaler addNearText(NearText nt) {
    setCommon(nt.common());

    var nearText = WeaviateProtoBaseSearch.NearTextSearch.newBuilder();
    nearText.addAllQuery(nt.text());

    if (nt.certainty() != null) {
      nearText.setCertainty(nt.certainty());
    } else if (nt.distance() != null) {
      nearText.setDistance(nt.distance());
    }

    // TODO: add targets
    if (nt.moveTo() != null) {
      var to = NearTextSearch.Move.newBuilder();
      nt.moveTo().appendTo(to);
      nearText.setMoveTo(to);
    }

    if (nt.moveAway() != null) {
      var away = NearTextSearch.Move.newBuilder();
      nt.moveAway().appendTo(away);
      nearText.setMoveAway(away);
    }

    req.setNearText(nearText);
    return this;
  }

  private void setCommon(CommonQueryOptions o) {
    if (o.limit() != null) {
      req.setLimit(o.limit());
    }
    if (o.offset() != null) {
      req.setOffset(o.offset());
    }
    if (StringUtils.isNotBlank(o.after())) {
      req.setAfter(o.after());
    }
    if (StringUtils.isNotBlank(o.consistencyLevel())) {
      req.setConsistencyLevelValue(Integer.valueOf(o.consistencyLevel()));
    }
    if (o.autocut() != null) {
      req.setAutocut(o.autocut());
    }

    if (!o.returnMetadata().isEmpty()) {
      var metadata = MetadataRequest.newBuilder();
      o.returnMetadata().forEach(m -> m.appendTo(metadata));
      req.setMetadata(metadata);
    }

    if (!o.returnProperties().isEmpty()) {
      var properties = PropertiesRequest.newBuilder();
      for (String property : o.returnProperties()) {
        properties.addNonRefProperties(property);
      }
      req.setProperties(properties);
    }
  }

  @Override
  public SearchRequest marshal() {
    return req.build();
  }
}
