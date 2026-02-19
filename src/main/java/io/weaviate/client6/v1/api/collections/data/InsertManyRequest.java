package io.weaviate.client6.v1.api.collections.data;

import static java.util.Objects.requireNonNull;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import io.weaviate.client6.v1.api.collections.CollectionHandleDefaults;
import io.weaviate.client6.v1.api.collections.GeoCoordinates;
import io.weaviate.client6.v1.api.collections.PhoneNumber;
import io.weaviate.client6.v1.api.collections.WeaviateObject;
import io.weaviate.client6.v1.internal.MapUtil;
import io.weaviate.client6.v1.internal.grpc.ByteStringUtil;
import io.weaviate.client6.v1.internal.grpc.Rpc;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateGrpc.WeaviateBlockingStub;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateGrpc.WeaviateFutureStub;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBase;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBase.Vectors.VectorType;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch;
import io.weaviate.client6.v1.internal.orm.CollectionDescriptor;

public record InsertManyRequest<PropertiesT>(List<WeaviateObject<PropertiesT>> objects) {

  @SafeVarargs
  public InsertManyRequest(WeaviateObject<PropertiesT>... objects) {
    this(Arrays.asList(objects));
  }

  @SuppressWarnings("unchecked")
  @SafeVarargs
  public static final <PropertiesT> InsertManyRequest<PropertiesT> of(PropertiesT... properties) {
    var objects = Arrays.stream(properties)
        .map(p -> (WeaviateObject<PropertiesT>) WeaviateObject.of(obj -> obj.properties(p)))
        .toList();
    return new InsertManyRequest<>(objects);
  }

  public static <PropertiesT> Rpc<InsertManyRequest<PropertiesT>, WeaviateProtoBatch.BatchObjectsRequest, InsertManyResponse, WeaviateProtoBatch.BatchObjectsReply> rpc(
      List<WeaviateObject<PropertiesT>> insertObjects,
      CollectionDescriptor<PropertiesT> collection,
      CollectionHandleDefaults defaults) {
    return Rpc.insert(
        request -> {
          var message = WeaviateProtoBatch.BatchObjectsRequest.newBuilder();

          var batch = request.objects.stream()
              .map(obj -> buildObject(obj, collection, defaults))
              .toList();
          message.addAllObjects(batch);

          if (defaults.consistencyLevel().isPresent()) {
            defaults.consistencyLevel().get().appendTo(message);
          }
          var m = message.build();
          m.getSerializedSize();
          return message.build();
        },
        response -> {
          var insertErrors = response.getErrorsList();

          var responses = new ArrayList<InsertManyResponse.InsertObject>(insertObjects.size());
          var errors = new ArrayList<String>(insertErrors.size());
          var uuids = new ArrayList<String>();

          var failed = MapUtil.collect(
              insertErrors.stream(),
              err -> err.getIndex(),
              err -> err.getError());

          var iter = insertObjects.listIterator();
          while (iter.hasNext()) {
            var idx = iter.nextIndex();
            var next = iter.next();

            var uuid = next.uuid();
            if (failed.containsKey(idx)) {
              var err = failed.get(idx);
              errors.add(err);
              responses.add(new InsertManyResponse.InsertObject(uuid, false, err));
            } else {
              uuids.add(uuid);
              responses.add(new InsertManyResponse.InsertObject(uuid, true, null));
            }
          }

          return new InsertManyResponse(response.getTook(), responses, uuids, errors);
        },
        () -> WeaviateBlockingStub::batchObjects,
        () -> WeaviateFutureStub::batchObjects);
  }

  public static <T> WeaviateProtoBatch.BatchObject buildObject(
      WeaviateObject<T> insert,
      CollectionDescriptor<T> collection,
      CollectionHandleDefaults defaults) {
    var object = WeaviateProtoBatch.BatchObject.newBuilder();
    object.setCollection(collection.collectionName());

    if (insert.uuid() != null) {
      object.setUuid(insert.uuid());
    }

    if (insert.vectors() != null) {
      var vectors = insert.vectors().asMap()
          .entrySet().stream().map(entry -> {
            var value = entry.getValue();
            var vector = WeaviateProtoBase.Vectors.newBuilder()
                .setName(entry.getKey());

            if (value instanceof float[] single) {
              vector.setType(VectorType.VECTOR_TYPE_SINGLE_FP32);
              vector.setVectorBytes(ByteStringUtil.encodeVectorSingle(single));
            } else if (value instanceof float[][] multi) {
              vector.setVectorBytes(ByteStringUtil.encodeVectorMulti(multi));
              vector.setType(VectorType.VECTOR_TYPE_MULTI_FP32);
            }

            return vector.build();
          }).toList();
      object.addAllVectors(vectors);
    }
    defaults.tenant().ifPresent(object::setTenant);

    var singleRef = new ArrayList<WeaviateProtoBatch.BatchObject.SingleTargetRefProps>();
    var multiRef = new ArrayList<WeaviateProtoBatch.BatchObject.MultiTargetRefProps>();

    if (insert.references() != null) {
      insert.references().entrySet().stream().forEach(entry -> {
        var references = entry.getValue();

        // dyma: How are we supposed to know if the reference
        // is single- or multi-target?
        for (var ref : references) {
          if (ref.collection() == null) {
            singleRef.add(WeaviateProtoBatch.BatchObject.SingleTargetRefProps.newBuilder()
                .addUuids(ref.uuid())
                .setPropName(entry.getKey()).build());
          } else {
            multiRef.add(WeaviateProtoBatch.BatchObject.MultiTargetRefProps.newBuilder()
                .setTargetCollection(ref.collection()).addUuids(ref.uuid())
                .setPropName(entry.getKey()).build());
          }
        }
      });
    }

    var properties = WeaviateProtoBatch.BatchObject.Properties.newBuilder()
        .addAllSingleTargetRefProps(singleRef)
        .addAllMultiTargetRefProps(multiRef);

    if (insert.properties() != null) {
      var nonRef = marshalStruct(collection.propertiesReader(insert.properties())
          .readProperties());
      properties.setNonRefProperties(nonRef);
    }
    object.setProperties(properties);
    return object.build();
  }

  @SuppressWarnings("unchecked")
  private static com.google.protobuf.Value marshalValue(Object value) {
    var protoValue = com.google.protobuf.Value.newBuilder();

    if (value == null) {
      return null;
    }

    if (value instanceof String v) {
      protoValue.setStringValue(v);
    } else if (value instanceof UUID v) {
      protoValue.setStringValue(v.toString());
    } else if (value instanceof OffsetDateTime v) {
      protoValue.setStringValue(v.toString());
    } else if (value instanceof Boolean v) {
      protoValue.setBoolValue(v.booleanValue());
    } else if (value instanceof Number v) {
      protoValue.setNumberValue(v.doubleValue());
    } else if (value instanceof PhoneNumber phone) {
      var phoneProto = com.google.protobuf.Struct.newBuilder();
      if (phone.rawInput() != null) {
        var input = com.google.protobuf.Value.newBuilder().setStringValue(phone.rawInput());
        phoneProto.putFields("input", input.build());
      }
      if (phone.defaultCountry() != null) {
        var defaultCountry = com.google.protobuf.Value.newBuilder().setStringValue(phone.defaultCountry());
        phoneProto.putFields("defaultCountry", defaultCountry.build());
      }
      protoValue.setStructValue(phoneProto);
    } else if (value instanceof GeoCoordinates geo) {
      var latitude = com.google.protobuf.Value.newBuilder().setNumberValue(geo.latitude());
      var longitude = com.google.protobuf.Value.newBuilder().setNumberValue(geo.longitude());
      protoValue.setStructValue(com.google.protobuf.Struct.newBuilder()
          .putFields("latitude", latitude.build())
          .putFields("longitude", longitude.build()));
    } else if (value instanceof List<?> v) {
      protoValue.setListValue(
          com.google.protobuf.ListValue.newBuilder()
              .addAllValues(v.stream()
                  .map(listValue -> {
                    var protoListValue = com.google.protobuf.Value.newBuilder();
                    if (listValue instanceof String lv) {
                      protoListValue.setStringValue(lv);
                    } else if (listValue instanceof UUID lv) {
                      protoListValue.setStringValue(lv.toString());
                    } else if (listValue instanceof OffsetDateTime lv) {
                      protoListValue.setStringValue(lv.toString());
                    } else if (listValue instanceof Boolean lv) {
                      protoListValue.setBoolValue(lv);
                    } else if (listValue instanceof Number lv) {
                      protoListValue.setNumberValue(lv.doubleValue());
                    } else if (listValue instanceof Map<?, ?> properties) {
                      protoListValue.setStructValue(marshalStruct((Map<String, Object>) properties));
                    } else if (listValue instanceof Record r) {
                      CollectionDescriptor<? super Record> recordDescriptor = (CollectionDescriptor<? super Record>) CollectionDescriptor
                          .ofClass(r.getClass());
                      var properties = recordDescriptor.propertiesReader(r).readProperties();
                      protoListValue.setStructValue(marshalStruct(properties));
                    } else {
                      throw new IllegalArgumentException("data type " + value.getClass() + " is not supported");
                    }
                    return protoListValue.build();
                  })
                  .toList()));

    } else if (value.getClass().isArray()) {
      List<com.google.protobuf.Value> values;

      if (value instanceof String[] v) {
        values = Arrays.stream(v)
            .map(lv -> com.google.protobuf.Value.newBuilder().setStringValue(lv).build()).toList();
      } else if (value instanceof UUID[] v) {
        values = Arrays.stream(v)
            .map(lv -> com.google.protobuf.Value.newBuilder().setStringValue(lv.toString()).build()).toList();
      } else if (value instanceof OffsetDateTime[] v) {
        values = Arrays.stream(v)
            .map(lv -> com.google.protobuf.Value.newBuilder().setStringValue(lv.toString()).build()).toList();
      } else if (value instanceof Boolean[] v) {
        values = Arrays.stream(v)
            .map(lv -> com.google.protobuf.Value.newBuilder().setBoolValue(lv).build()).toList();
      } else if (value instanceof boolean[] v) {
        values = new ArrayList<>();
        for (boolean b : v) {
          values.add(com.google.protobuf.Value.newBuilder().setBoolValue(b).build());
        }
      } else if (value instanceof short[] v) {
        values = new ArrayList<>();
        for (short s : v) {
          values.add(com.google.protobuf.Value.newBuilder().setNumberValue(s).build());
        }
      } else if (value instanceof int[] v) {
        values = Arrays.stream(v).boxed()
            .map(lv -> com.google.protobuf.Value.newBuilder().setNumberValue(lv).build()).toList();
      } else if (value instanceof long[] v) {
        values = Arrays.stream(v).boxed()
            .map(lv -> com.google.protobuf.Value.newBuilder().setNumberValue(lv).build()).toList();
      } else if (value instanceof float[] v) {
        values = new ArrayList<>();
        for (float s : v) {
          values.add(com.google.protobuf.Value.newBuilder().setNumberValue(s).build());
        }
      } else if (value instanceof double[] v) {
        values = Arrays.stream(v).boxed()
            .map(lv -> com.google.protobuf.Value.newBuilder().setNumberValue(lv).build()).toList();
      } else if (value instanceof Short[] v) {
        values = Arrays.stream(v)
            .map(lv -> com.google.protobuf.Value.newBuilder().setNumberValue(lv).build()).toList();
      } else if (value instanceof Integer[] v) {
        values = Arrays.stream(v)
            .map(lv -> com.google.protobuf.Value.newBuilder().setNumberValue(lv).build()).toList();
      } else if (value instanceof Long[] v) {
        values = Arrays.stream(v)
            .map(lv -> com.google.protobuf.Value.newBuilder().setNumberValue(lv).build()).toList();
      } else if (value instanceof Float[] v) {
        values = Arrays.stream(v)
            .map(lv -> com.google.protobuf.Value.newBuilder().setNumberValue(lv).build()).toList();
      } else if (value instanceof Double[] v) {
        values = Arrays.stream(v)
            .map(lv -> com.google.protobuf.Value.newBuilder().setNumberValue(lv).build()).toList();
      } else if (value instanceof Map[] v) {
        values = Arrays.stream(v)
            .map(lv -> com.google.protobuf.Value.newBuilder()
                .setStructValue(marshalStruct((Map<String, Object>) lv))
                .build())
            .toList();
      } else if (value instanceof Record[] v) {
        values = Arrays.stream(v)
            .map(lv -> {
              // Get the descriptor for each iteration in case the array is heterogenous.
              final CollectionDescriptor<? super Record> recordDescriptor = (CollectionDescriptor<? super Record>) CollectionDescriptor
                  .ofClass(lv.getClass());
              var properties = recordDescriptor.propertiesReader(lv).readProperties();
              return com.google.protobuf.Value.newBuilder()
                  .setStructValue(marshalStruct(properties))
                  .build();
            })
            .toList();
      } else {
        throw new IllegalArgumentException("array type " + value.getClass() + " is not supported");
      }

      protoValue.setListValue(com.google.protobuf.ListValue.newBuilder()
          .addAllValues(values)
          .build());
    } else if (value instanceof Map<?, ?> properties) {
      protoValue.setStructValue(marshalStruct((Map<String, Object>) properties));
    } else if (value instanceof Record r) {
      CollectionDescriptor<? super Record> recordDescriptor = (CollectionDescriptor<? super Record>) CollectionDescriptor
          .ofClass(r.getClass());
      var properties = recordDescriptor.propertiesReader(r).readProperties();
      protoValue.setStructValue(marshalStruct(properties));
    } else {
      throw new IllegalArgumentException("data type " + value.getClass() + " is not supported");
    }

    return protoValue.build();
  }

  private static com.google.protobuf.Struct marshalStruct(Map<String, Object> properties) {
    var struct = com.google.protobuf.Struct.newBuilder();
    properties.entrySet().stream()
        .forEach(entry -> {
          var nestedValue = marshalValue(entry.getValue());
          if (nestedValue == null) {
            return;
          }
          struct.putFields((String) entry.getKey(), nestedValue);
        });
    return struct.build();
  }

  public static WeaviateProtoBatch.BatchReference buildReference(BatchReference reference, Optional<String> tenant) {
    requireNonNull(reference, "reference is null");
    WeaviateProtoBatch.BatchReference.Builder builder = WeaviateProtoBatch.BatchReference.newBuilder();
    builder
        .setName(reference.fromProperty())
        .setFromCollection(reference.fromCollection())
        .setFromUuid(reference.fromUuid())
        .setToUuid(reference.target().uuid());

    if (reference.target().collection() != null) {
      builder.setToCollection(reference.target().collection());
    }
    tenant.ifPresent(t -> builder.setTenant(t));
    return builder.build();
  }
}
