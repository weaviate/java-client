package io.weaviate.client6.v1.api.collections.query;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import io.weaviate.client6.v1.api.collections.GeoCoordinates;
import io.weaviate.client6.v1.api.collections.PhoneNumber;
import io.weaviate.client6.v1.api.collections.Reference;
import io.weaviate.client6.v1.api.collections.Vectors;
import io.weaviate.client6.v1.api.collections.WeaviateObject;
import io.weaviate.client6.v1.internal.DateUtil;
import io.weaviate.client6.v1.internal.grpc.ByteStringUtil;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoProperties;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoSearchGet;
import io.weaviate.client6.v1.internal.orm.CollectionDescriptor;
import io.weaviate.client6.v1.internal.orm.PropertiesBuilder;

public record QueryResponse<PropertiesT>(
    List<WeaviateObject<PropertiesT>> objects) {

  static <PropertiesT> QueryResponse<PropertiesT> unmarshal(WeaviateProtoSearchGet.SearchReply reply,
      CollectionDescriptor<PropertiesT> collection) {
    var objects = reply
        .getResultsList()
        .stream()
        .map(obj -> QueryResponse.unmarshalResultObject(
            obj.getProperties(), obj.getMetadata(), collection))
        .toList();
    return new QueryResponse<>(objects);
  }

  public static <PropertiesT> WeaviateObject<PropertiesT> unmarshalResultObject(
      WeaviateProtoSearchGet.PropertiesResult propertiesResult,
      WeaviateProtoSearchGet.MetadataResult metadataResult,
      CollectionDescriptor<PropertiesT> collection) {
    var object = unmarshalWithReferences(propertiesResult, metadataResult, collection);

    Long createdAt = null;
    Long lastUpdatedAt = null;
    var metadata = new QueryMetadata.Builder();

    if (metadataResult.getCreationTimeUnixPresent()) {
      createdAt = metadataResult.getCreationTimeUnix();
    }
    if (metadataResult.getLastUpdateTimeUnixPresent()) {
      lastUpdatedAt = metadataResult.getLastUpdateTimeUnix();
    }
    if (metadataResult.getDistancePresent()) {
      metadata.distance(metadataResult.getDistance());
    }
    if (metadataResult.getCertaintyPresent()) {
      metadata.certainty(metadataResult.getCertainty());
    }
    if (metadataResult.getScorePresent()) {
      metadata.score(metadataResult.getScore());
    }
    if (metadataResult.getExplainScorePresent()) {
      metadata.explainScore(metadataResult.getExplainScore());
    }
    return new WeaviateObject<>(
        object.uuid(),
        collection.collectionName(),
        null, // tenant is not reeturned in the query
        object.properties(),
        object.vectors(),
        createdAt,
        lastUpdatedAt,
        metadata.build(),
        object.references());
  }

  static <PropertiesT> WeaviateObject<PropertiesT> unmarshalWithReferences(
      WeaviateProtoSearchGet.PropertiesResult propertiesResult,
      WeaviateProtoSearchGet.MetadataResult metadataResult,
      CollectionDescriptor<PropertiesT> descriptor) {
    var properties = descriptor.propertiesBuilder();
    propertiesResult.getNonRefProps().getFieldsMap().entrySet().stream()
        .forEach(entry -> setProperty(entry.getKey(), entry.getValue(), properties, descriptor));

    // In case a reference is multi-target, there will be a separate
    // "reference property" for each of the targets, so instead of
    // `collect` we need to `reduce` the map, merging related references
    // as we go.
    // I.e. { "ref": A-1 } , { "ref": B-1 } => { "ref": [A-1, B-1] }
    var referenceProperties = propertiesResult.getRefPropsList()
        .stream().reduce(
            new HashMap<String, List<Reference>>(),
            (map, ref) -> {
              var refObjects = ref.getPropertiesList().stream()
                  .map(property -> {
                    var reference = unmarshalWithReferences(
                        property, property.getMetadata(),
                        CollectionDescriptor.ofMap(property.getTargetCollection()));
                    return (Reference) new WeaviateObject<>(
                        reference.uuid(),
                        reference.collection(),
                        // TODO(dyma): we can get tenant from CollectionHandle
                        null, // tenant is not returned in the query
                        (Map<String, Object>) reference.properties(),
                        reference.vectors(),
                        reference.createdAt(),
                        reference.lastUpdatedAt(),
                        reference.queryMetadata(),
                        reference.references());
                  })
                  .toList();

              // Merge ObjectReferences by joining the underlying WeaviateObjects.
              map.merge(
                  ref.getPropName(),
                  refObjects,
                  (left, right) -> {
                    var joined = Stream.concat(
                        left.stream(),
                        right.stream()).toList();
                    return joined;
                  });
              return map;
            },
            (left, right) -> {
              left.putAll(right);
              return left;
            });

    String uuid = null;
    Vectors vectors = null;
    QueryMetadata metadata = null;
    if (metadataResult != null) {
      var metadataBuilder = new QueryMetadata.Builder();

      uuid = metadataResult.getId();

      // Read legacy (unnamed) vector.
      if (metadataResult.getVectorBytes() != null && !metadataResult.getVectorBytes().isEmpty()) {
        var unnamed = ByteStringUtil.decodeVectorSingle(metadataResult.getVectorBytes());
        vectors = Vectors.of(unnamed);
      } else {
        var namedVectors = new Vectors[metadataResult.getVectorsList().size()];
        var i = 0;
        for (final var vector : metadataResult.getVectorsList()) {
          var vectorName = vector.getName();
          var vbytes = vector.getVectorBytes();
          switch (vector.getType()) {
            case VECTOR_TYPE_SINGLE_FP32:
              namedVectors[i++] = Vectors.of(vectorName, ByteStringUtil.decodeVectorSingle(vbytes));
              break;
            case VECTOR_TYPE_MULTI_FP32:
              namedVectors[i++] = Vectors.of(vectorName, ByteStringUtil.decodeVectorMulti(vbytes));
              break;
            default:
              continue;
          }
        }
        vectors = new Vectors(namedVectors);
      }

      metadata = metadataBuilder.build();
    }

    return new WeaviateObject<>(
        uuid,
        descriptor.collectionName(),
        null, // tenant is not returned in the query
        properties.build(),
        vectors,
        null,
        null,
        metadata,
        referenceProperties);
  }

  static <PropertiesT> void setProperty(String property, WeaviateProtoProperties.Value value,
      PropertiesBuilder<PropertiesT> builder, CollectionDescriptor<PropertiesT> descriptor) {
    if (value.hasNullValue()) {
      builder.setNull(property);
    } else if (value.hasTextValue()) {
      builder.setText(property, value.getTextValue());
    } else if (value.hasBoolValue()) {
      builder.setBoolean(property, value.getBoolValue());
    } else if (value.hasIntValue()) {
      builder.setLong(property, value.getIntValue());
    } else if (value.hasNumberValue()) {
      builder.setDouble(property, value.getNumberValue());
    } else if (value.hasBlobValue()) {
      builder.setBlob(property, value.getBlobValue());
    } else if (value.hasDateValue()) {
      builder.setOffsetDateTime(property, DateUtil.fromISO8601(value.getDateValue()));
    } else if (value.hasUuidValue()) {
      builder.setUuid(property, UUID.fromString(value.getUuidValue()));
    } else if (value.hasPhoneValue()) {
      var phone = value.getPhoneValue();
      builder.setPhoneNumber(property, new PhoneNumber(
          phone.getInput(),
          phone.getDefaultCountry(),
          Long.valueOf(phone.getCountryCode()).intValue(),
          phone.getInternationalFormatted(),
          Long.valueOf(phone.getNational()).intValue(),
          phone.getNationalFormatted(),
          phone.getValid()));
    } else if (value.hasGeoValue()) {
      var geo = value.getGeoValue();
      builder.setGeoCoordinates(property, new GeoCoordinates(
          geo.getLatitude(),
          geo.getLongitude()));
    } else if (value.hasListValue()) {
      var list = value.getListValue();
      if (list.hasTextValues()) {
        builder.setTextArray(property, list.getTextValues().getValuesList());
      } else if (list.hasIntValues()) {
        var ints = Arrays.stream(
            ByteStringUtil.decodeIntValues(list.getIntValues().getValues()))
            .boxed().toList();
        builder.setLongArray(property, ints);
      } else if (list.hasNumberValues()) {
        var numbers = Arrays.stream(
            ByteStringUtil.decodeNumberValues(list.getNumberValues().getValues()))
            .boxed().toList();
        builder.setDoubleArray(property, numbers);
      } else if (list.hasUuidValues()) {
        var uuids = list.getUuidValues().getValuesList().stream()
            .map(UUID::fromString).toList();
        builder.setUuidArray(property, uuids);
      } else if (list.hasBoolValues()) {
        builder.setBooleanArray(property, list.getBoolValues().getValuesList());
      } else if (list.hasDateValues()) {
        var dates = list.getDateValues().getValuesList().stream()
            .map(DateUtil::fromISO8601).toList();
        builder.setOffsetDateTimeArray(property, dates);
      } else if (list.hasObjectValues()) {
        List<? extends Object> objects = list.getObjectValues().getValuesList().stream()
            .map(object -> {
              var properties = descriptor.propertiesBuilder();
              object.getFieldsMap().entrySet().stream()
                  .forEach(entry -> setProperty(entry.getKey(), entry.getValue(), properties, descriptor));
              return properties.build();
            }).toList();
        builder.setNestedObjectArray(property, objects);
      }
    } else if (value.hasObjectValue()) {
      var object = value.getObjectValue();
      var properties = descriptor.propertiesBuilder();
      object.getFieldsMap().entrySet().stream()
          .forEach(entry -> setProperty(entry.getKey(), entry.getValue(), properties, descriptor));
      builder.setNestedObject(property, properties.build());
    } else {
      throw new IllegalArgumentException(property + " data type is not supported");
    }
  }
}
