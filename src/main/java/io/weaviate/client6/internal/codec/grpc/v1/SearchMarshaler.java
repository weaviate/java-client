package io.weaviate.client6.internal.codec.grpc.v1;

import org.apache.commons.lang3.StringUtils;

import io.weaviate.client6.grpc.protocol.v1.WeaviateProtoBaseSearch;
import io.weaviate.client6.grpc.protocol.v1.WeaviateProtoSearchGet;
import io.weaviate.client6.grpc.protocol.v1.WeaviateProtoSearchGet.MetadataRequest;
import io.weaviate.client6.grpc.protocol.v1.WeaviateProtoSearchGet.PropertiesRequest;
import io.weaviate.client6.grpc.protocol.v1.WeaviateProtoSearchGet.SearchRequest;
import io.weaviate.client6.internal.GRPC;
import io.weaviate.client6.internal.codec.grpc.GrpcMarshaler;
import io.weaviate.client6.v1.collections.query.CommonQueryOptions;
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

  public SearchMarshaler addNearVector(NearVector nv) {
    setCommon(nv.common());

    var nearVector = WeaviateProtoBaseSearch.NearVector.newBuilder();
    nearVector.setVectorBytes(GRPC.toByteString(nv.vector()));

    if (nv.certainty() != null) {
      nearVector.setCertainty(nv.certainty());
    } else if (nv.distance() != null) {
      nearVector.setDistance(nv.distance());
    }

    req.setNearVector(nearVector);
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
