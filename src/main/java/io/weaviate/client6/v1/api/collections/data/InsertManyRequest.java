package io.weaviate.client6.v1.api.collections.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import io.weaviate.client6.v1.api.collections.ObjectMetadata;
import io.weaviate.client6.v1.api.collections.WeaviateObject;
import io.weaviate.client6.v1.internal.grpc.ByteStringUtil;
import io.weaviate.client6.v1.internal.grpc.Rpc;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateGrpc.WeaviateBlockingStub;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateGrpc.WeaviateFutureStub;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBase;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBase.Vectors.VectorType;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch;
import io.weaviate.client6.v1.internal.orm.CollectionDescriptor;

public record InsertManyRequest<T>(List<WeaviateObject<T, Reference, ObjectMetadata>> objects) {

  @SafeVarargs
  public InsertManyRequest(WeaviateObject<T, Reference, ObjectMetadata>... objects) {
    this(Arrays.asList(objects));
  }

  @SafeVarargs
  public static final <T> InsertManyRequest<T> of(T... properties) {
    var objects = Arrays.stream(properties)
        .map(p -> WeaviateObject.<T, Reference, ObjectMetadata>of(
            obj -> obj
                .properties(p)
                .metadata(ObjectMetadata.of(m -> m.uuid(UUID.randomUUID())))))
        .toList();
    return new InsertManyRequest<T>(objects);
  }

  public static <T> Rpc<InsertManyRequest<T>, WeaviateProtoBatch.BatchObjectsRequest, InsertManyResponse, WeaviateProtoBatch.BatchObjectsReply> rpc(
      List<WeaviateObject<T, Reference, ObjectMetadata>> insertObjects,
      CollectionDescriptor<T> collectionsDescriptor) {
    return Rpc.of(
        request -> {
          var message = WeaviateProtoBatch.BatchObjectsRequest.newBuilder();

          var batch = request.objects.stream().map(obj -> {
            var batchObject = WeaviateProtoBatch.BatchObject.newBuilder();
            buildObject(batchObject, obj, collectionsDescriptor);
            return batchObject.build();
          }).toList();

          message.addAllObjects(batch);
          return message.build();
        },
        response -> {
          var insertErrors = response.getErrorsList();

          var responses = new ArrayList<InsertManyResponse.InsertObject>(insertObjects.size());
          var errors = new ArrayList<String>(insertErrors.size());
          var uuids = new ArrayList<String>();

          var failed = insertErrors.stream()
              .collect(Collectors.toMap(err -> err.getIndex(), err -> err.getError()));

          var iter = insertObjects.listIterator();
          while (iter.hasNext()) {
            var idx = iter.nextIndex();
            var next = iter.next();
            var uuid = next.metadata() != null ? next.metadata().uuid() : null;

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

  public static <T> void buildObject(WeaviateProtoBatch.BatchObject.Builder object,
      WeaviateObject<T, Reference, ObjectMetadata> insert,
      CollectionDescriptor<T> collectionDescriptor) {
    object.setCollection(collectionDescriptor.name());

    var metadata = insert.metadata();
    if (metadata != null) {
      if (metadata.uuid() != null) {
        object.setUuid(metadata.uuid());
      }

      if (metadata.vectors() != null) {
        var vectors = metadata.vectors().asMap()
            .entrySet().stream().map(entry -> {
              var value = entry.getValue();
              var vector = WeaviateProtoBase.Vectors.newBuilder()
                  .setName(entry.getKey());

              if (value instanceof Float[] single) {
                vector.setType(VectorType.VECTOR_TYPE_SINGLE_FP32);
                vector.setVectorBytes(ByteStringUtil.encodeVectorSingle(single));
              } else if (value instanceof Float[][] multi) {
                vector.setVectorBytes(ByteStringUtil.encodeVectorMulti(multi));
                vector.setType(VectorType.VECTOR_TYPE_MULTI_FP32);
              }

              return vector.build();
            }).toList();
        object.addAllVectors(vectors);
      }
    }

    var properties = WeaviateProtoBatch.BatchObject.Properties.newBuilder();
    var nonRef = com.google.protobuf.Struct.newBuilder();
    var singleRef = new ArrayList<WeaviateProtoBatch.BatchObject.SingleTargetRefProps>();
    var multiRef = new ArrayList<WeaviateProtoBatch.BatchObject.MultiTargetRefProps>();

    collectionDescriptor
        .propertiesReader(insert.properties()).readProperties()
        .entrySet().stream().forEach(entry -> {
          var value = entry.getValue();
          var protoValue = com.google.protobuf.Value.newBuilder();

          if (value instanceof String v) {
            protoValue.setStringValue(v);
          } else if (value instanceof Number v) {
            protoValue.setNumberValue(v.doubleValue());
          } else {
            assert false : "(insertMany) branch not covered";
          }

          nonRef.putFields(entry.getKey(), protoValue.build());
        });

    insert.references()
        .entrySet().stream().forEach(entry -> {
          var references = entry.getValue();

          // dyma: How are we supposed to know if the reference
          // is single- or multi-target?
          for (var ref : references) {
            if (ref.collection() == null) {
              singleRef.add(
                  WeaviateProtoBatch.BatchObject.SingleTargetRefProps.newBuilder()
                      .addAllUuids(ref.uuids())
                      .setPropName(entry.getKey())
                      .build());
            } else {
              multiRef.add(
                  WeaviateProtoBatch.BatchObject.MultiTargetRefProps.newBuilder()
                      .setTargetCollection(ref.collection())
                      .addAllUuids(ref.uuids())
                      .setPropName(entry.getKey())
                      .build());
            }
          }
        });

    properties
        .setNonRefProperties(nonRef)
        .addAllSingleTargetRefProps(singleRef)
        .addAllMultiTargetRefProps(multiRef);

    object.setProperties(properties);
  }
}
