package io.weaviate.client6.v1.internal.grpc.protocol;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.58.0)",
    comments = "Source: v1/weaviate.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class WeaviateGrpc {

  private WeaviateGrpc() {}

  public static final java.lang.String SERVICE_NAME = "weaviate.v1.Weaviate";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoSearchGet.SearchRequest,
      io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoSearchGet.SearchReply> getSearchMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "Search",
      requestType = io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoSearchGet.SearchRequest.class,
      responseType = io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoSearchGet.SearchReply.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoSearchGet.SearchRequest,
      io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoSearchGet.SearchReply> getSearchMethod() {
    io.grpc.MethodDescriptor<io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoSearchGet.SearchRequest, io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoSearchGet.SearchReply> getSearchMethod;
    if ((getSearchMethod = WeaviateGrpc.getSearchMethod) == null) {
      synchronized (WeaviateGrpc.class) {
        if ((getSearchMethod = WeaviateGrpc.getSearchMethod) == null) {
          WeaviateGrpc.getSearchMethod = getSearchMethod =
              io.grpc.MethodDescriptor.<io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoSearchGet.SearchRequest, io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoSearchGet.SearchReply>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "Search"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoSearchGet.SearchRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoSearchGet.SearchReply.getDefaultInstance()))
              .setSchemaDescriptor(new WeaviateMethodDescriptorSupplier("Search"))
              .build();
        }
      }
    }
    return getSearchMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch.BatchObjectsRequest,
      io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch.BatchObjectsReply> getBatchObjectsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "BatchObjects",
      requestType = io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch.BatchObjectsRequest.class,
      responseType = io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch.BatchObjectsReply.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch.BatchObjectsRequest,
      io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch.BatchObjectsReply> getBatchObjectsMethod() {
    io.grpc.MethodDescriptor<io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch.BatchObjectsRequest, io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch.BatchObjectsReply> getBatchObjectsMethod;
    if ((getBatchObjectsMethod = WeaviateGrpc.getBatchObjectsMethod) == null) {
      synchronized (WeaviateGrpc.class) {
        if ((getBatchObjectsMethod = WeaviateGrpc.getBatchObjectsMethod) == null) {
          WeaviateGrpc.getBatchObjectsMethod = getBatchObjectsMethod =
              io.grpc.MethodDescriptor.<io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch.BatchObjectsRequest, io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch.BatchObjectsReply>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "BatchObjects"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch.BatchObjectsRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch.BatchObjectsReply.getDefaultInstance()))
              .setSchemaDescriptor(new WeaviateMethodDescriptorSupplier("BatchObjects"))
              .build();
        }
      }
    }
    return getBatchObjectsMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch.BatchReferencesRequest,
      io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch.BatchReferencesReply> getBatchReferencesMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "BatchReferences",
      requestType = io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch.BatchReferencesRequest.class,
      responseType = io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch.BatchReferencesReply.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch.BatchReferencesRequest,
      io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch.BatchReferencesReply> getBatchReferencesMethod() {
    io.grpc.MethodDescriptor<io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch.BatchReferencesRequest, io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch.BatchReferencesReply> getBatchReferencesMethod;
    if ((getBatchReferencesMethod = WeaviateGrpc.getBatchReferencesMethod) == null) {
      synchronized (WeaviateGrpc.class) {
        if ((getBatchReferencesMethod = WeaviateGrpc.getBatchReferencesMethod) == null) {
          WeaviateGrpc.getBatchReferencesMethod = getBatchReferencesMethod =
              io.grpc.MethodDescriptor.<io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch.BatchReferencesRequest, io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch.BatchReferencesReply>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "BatchReferences"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch.BatchReferencesRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch.BatchReferencesReply.getDefaultInstance()))
              .setSchemaDescriptor(new WeaviateMethodDescriptorSupplier("BatchReferences"))
              .build();
        }
      }
    }
    return getBatchReferencesMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatchDelete.BatchDeleteRequest,
      io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatchDelete.BatchDeleteReply> getBatchDeleteMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "BatchDelete",
      requestType = io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatchDelete.BatchDeleteRequest.class,
      responseType = io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatchDelete.BatchDeleteReply.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatchDelete.BatchDeleteRequest,
      io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatchDelete.BatchDeleteReply> getBatchDeleteMethod() {
    io.grpc.MethodDescriptor<io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatchDelete.BatchDeleteRequest, io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatchDelete.BatchDeleteReply> getBatchDeleteMethod;
    if ((getBatchDeleteMethod = WeaviateGrpc.getBatchDeleteMethod) == null) {
      synchronized (WeaviateGrpc.class) {
        if ((getBatchDeleteMethod = WeaviateGrpc.getBatchDeleteMethod) == null) {
          WeaviateGrpc.getBatchDeleteMethod = getBatchDeleteMethod =
              io.grpc.MethodDescriptor.<io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatchDelete.BatchDeleteRequest, io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatchDelete.BatchDeleteReply>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "BatchDelete"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatchDelete.BatchDeleteRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatchDelete.BatchDeleteReply.getDefaultInstance()))
              .setSchemaDescriptor(new WeaviateMethodDescriptorSupplier("BatchDelete"))
              .build();
        }
      }
    }
    return getBatchDeleteMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoTenants.TenantsGetRequest,
      io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoTenants.TenantsGetReply> getTenantsGetMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "TenantsGet",
      requestType = io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoTenants.TenantsGetRequest.class,
      responseType = io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoTenants.TenantsGetReply.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoTenants.TenantsGetRequest,
      io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoTenants.TenantsGetReply> getTenantsGetMethod() {
    io.grpc.MethodDescriptor<io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoTenants.TenantsGetRequest, io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoTenants.TenantsGetReply> getTenantsGetMethod;
    if ((getTenantsGetMethod = WeaviateGrpc.getTenantsGetMethod) == null) {
      synchronized (WeaviateGrpc.class) {
        if ((getTenantsGetMethod = WeaviateGrpc.getTenantsGetMethod) == null) {
          WeaviateGrpc.getTenantsGetMethod = getTenantsGetMethod =
              io.grpc.MethodDescriptor.<io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoTenants.TenantsGetRequest, io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoTenants.TenantsGetReply>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "TenantsGet"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoTenants.TenantsGetRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoTenants.TenantsGetReply.getDefaultInstance()))
              .setSchemaDescriptor(new WeaviateMethodDescriptorSupplier("TenantsGet"))
              .build();
        }
      }
    }
    return getTenantsGetMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoAggregate.AggregateRequest,
      io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoAggregate.AggregateReply> getAggregateMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "Aggregate",
      requestType = io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoAggregate.AggregateRequest.class,
      responseType = io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoAggregate.AggregateReply.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoAggregate.AggregateRequest,
      io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoAggregate.AggregateReply> getAggregateMethod() {
    io.grpc.MethodDescriptor<io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoAggregate.AggregateRequest, io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoAggregate.AggregateReply> getAggregateMethod;
    if ((getAggregateMethod = WeaviateGrpc.getAggregateMethod) == null) {
      synchronized (WeaviateGrpc.class) {
        if ((getAggregateMethod = WeaviateGrpc.getAggregateMethod) == null) {
          WeaviateGrpc.getAggregateMethod = getAggregateMethod =
              io.grpc.MethodDescriptor.<io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoAggregate.AggregateRequest, io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoAggregate.AggregateReply>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "Aggregate"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoAggregate.AggregateRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoAggregate.AggregateReply.getDefaultInstance()))
              .setSchemaDescriptor(new WeaviateMethodDescriptorSupplier("Aggregate"))
              .build();
        }
      }
    }
    return getAggregateMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch.BatchSendRequest,
      io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch.BatchSendReply> getBatchSendMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "BatchSend",
      requestType = io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch.BatchSendRequest.class,
      responseType = io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch.BatchSendReply.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch.BatchSendRequest,
      io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch.BatchSendReply> getBatchSendMethod() {
    io.grpc.MethodDescriptor<io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch.BatchSendRequest, io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch.BatchSendReply> getBatchSendMethod;
    if ((getBatchSendMethod = WeaviateGrpc.getBatchSendMethod) == null) {
      synchronized (WeaviateGrpc.class) {
        if ((getBatchSendMethod = WeaviateGrpc.getBatchSendMethod) == null) {
          WeaviateGrpc.getBatchSendMethod = getBatchSendMethod =
              io.grpc.MethodDescriptor.<io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch.BatchSendRequest, io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch.BatchSendReply>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "BatchSend"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch.BatchSendRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch.BatchSendReply.getDefaultInstance()))
              .setSchemaDescriptor(new WeaviateMethodDescriptorSupplier("BatchSend"))
              .build();
        }
      }
    }
    return getBatchSendMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch.BatchStreamRequest,
      io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch.BatchStreamMessage> getBatchStreamMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "BatchStream",
      requestType = io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch.BatchStreamRequest.class,
      responseType = io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch.BatchStreamMessage.class,
      methodType = io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
  public static io.grpc.MethodDescriptor<io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch.BatchStreamRequest,
      io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch.BatchStreamMessage> getBatchStreamMethod() {
    io.grpc.MethodDescriptor<io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch.BatchStreamRequest, io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch.BatchStreamMessage> getBatchStreamMethod;
    if ((getBatchStreamMethod = WeaviateGrpc.getBatchStreamMethod) == null) {
      synchronized (WeaviateGrpc.class) {
        if ((getBatchStreamMethod = WeaviateGrpc.getBatchStreamMethod) == null) {
          WeaviateGrpc.getBatchStreamMethod = getBatchStreamMethod =
              io.grpc.MethodDescriptor.<io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch.BatchStreamRequest, io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch.BatchStreamMessage>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "BatchStream"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch.BatchStreamRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch.BatchStreamMessage.getDefaultInstance()))
              .setSchemaDescriptor(new WeaviateMethodDescriptorSupplier("BatchStream"))
              .build();
        }
      }
    }
    return getBatchStreamMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static WeaviateStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<WeaviateStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<WeaviateStub>() {
        @java.lang.Override
        public WeaviateStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new WeaviateStub(channel, callOptions);
        }
      };
    return WeaviateStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static WeaviateBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<WeaviateBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<WeaviateBlockingStub>() {
        @java.lang.Override
        public WeaviateBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new WeaviateBlockingStub(channel, callOptions);
        }
      };
    return WeaviateBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static WeaviateFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<WeaviateFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<WeaviateFutureStub>() {
        @java.lang.Override
        public WeaviateFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new WeaviateFutureStub(channel, callOptions);
        }
      };
    return WeaviateFutureStub.newStub(factory, channel);
  }

  /**
   */
  public interface AsyncService {

    /**
     */
    default void search(io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoSearchGet.SearchRequest request,
        io.grpc.stub.StreamObserver<io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoSearchGet.SearchReply> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getSearchMethod(), responseObserver);
    }

    /**
     */
    default void batchObjects(io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch.BatchObjectsRequest request,
        io.grpc.stub.StreamObserver<io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch.BatchObjectsReply> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getBatchObjectsMethod(), responseObserver);
    }

    /**
     */
    default void batchReferences(io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch.BatchReferencesRequest request,
        io.grpc.stub.StreamObserver<io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch.BatchReferencesReply> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getBatchReferencesMethod(), responseObserver);
    }

    /**
     */
    default void batchDelete(io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatchDelete.BatchDeleteRequest request,
        io.grpc.stub.StreamObserver<io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatchDelete.BatchDeleteReply> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getBatchDeleteMethod(), responseObserver);
    }

    /**
     */
    default void tenantsGet(io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoTenants.TenantsGetRequest request,
        io.grpc.stub.StreamObserver<io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoTenants.TenantsGetReply> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getTenantsGetMethod(), responseObserver);
    }

    /**
     */
    default void aggregate(io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoAggregate.AggregateRequest request,
        io.grpc.stub.StreamObserver<io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoAggregate.AggregateReply> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getAggregateMethod(), responseObserver);
    }

    /**
     */
    default void batchSend(io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch.BatchSendRequest request,
        io.grpc.stub.StreamObserver<io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch.BatchSendReply> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getBatchSendMethod(), responseObserver);
    }

    /**
     */
    default void batchStream(io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch.BatchStreamRequest request,
        io.grpc.stub.StreamObserver<io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch.BatchStreamMessage> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getBatchStreamMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service Weaviate.
   */
  public static abstract class WeaviateImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return WeaviateGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service Weaviate.
   */
  public static final class WeaviateStub
      extends io.grpc.stub.AbstractAsyncStub<WeaviateStub> {
    private WeaviateStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected WeaviateStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new WeaviateStub(channel, callOptions);
    }

    /**
     */
    public void search(io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoSearchGet.SearchRequest request,
        io.grpc.stub.StreamObserver<io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoSearchGet.SearchReply> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getSearchMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void batchObjects(io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch.BatchObjectsRequest request,
        io.grpc.stub.StreamObserver<io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch.BatchObjectsReply> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getBatchObjectsMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void batchReferences(io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch.BatchReferencesRequest request,
        io.grpc.stub.StreamObserver<io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch.BatchReferencesReply> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getBatchReferencesMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void batchDelete(io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatchDelete.BatchDeleteRequest request,
        io.grpc.stub.StreamObserver<io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatchDelete.BatchDeleteReply> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getBatchDeleteMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void tenantsGet(io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoTenants.TenantsGetRequest request,
        io.grpc.stub.StreamObserver<io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoTenants.TenantsGetReply> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getTenantsGetMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void aggregate(io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoAggregate.AggregateRequest request,
        io.grpc.stub.StreamObserver<io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoAggregate.AggregateReply> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getAggregateMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void batchSend(io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch.BatchSendRequest request,
        io.grpc.stub.StreamObserver<io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch.BatchSendReply> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getBatchSendMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void batchStream(io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch.BatchStreamRequest request,
        io.grpc.stub.StreamObserver<io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch.BatchStreamMessage> responseObserver) {
      io.grpc.stub.ClientCalls.asyncServerStreamingCall(
          getChannel().newCall(getBatchStreamMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service Weaviate.
   */
  public static final class WeaviateBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<WeaviateBlockingStub> {
    private WeaviateBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected WeaviateBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new WeaviateBlockingStub(channel, callOptions);
    }

    /**
     */
    public io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoSearchGet.SearchReply search(io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoSearchGet.SearchRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getSearchMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch.BatchObjectsReply batchObjects(io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch.BatchObjectsRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getBatchObjectsMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch.BatchReferencesReply batchReferences(io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch.BatchReferencesRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getBatchReferencesMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatchDelete.BatchDeleteReply batchDelete(io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatchDelete.BatchDeleteRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getBatchDeleteMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoTenants.TenantsGetReply tenantsGet(io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoTenants.TenantsGetRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getTenantsGetMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoAggregate.AggregateReply aggregate(io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoAggregate.AggregateRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getAggregateMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch.BatchSendReply batchSend(io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch.BatchSendRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getBatchSendMethod(), getCallOptions(), request);
    }

    /**
     */
    public java.util.Iterator<io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch.BatchStreamMessage> batchStream(
        io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch.BatchStreamRequest request) {
      return io.grpc.stub.ClientCalls.blockingServerStreamingCall(
          getChannel(), getBatchStreamMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service Weaviate.
   */
  public static final class WeaviateFutureStub
      extends io.grpc.stub.AbstractFutureStub<WeaviateFutureStub> {
    private WeaviateFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected WeaviateFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new WeaviateFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoSearchGet.SearchReply> search(
        io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoSearchGet.SearchRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getSearchMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch.BatchObjectsReply> batchObjects(
        io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch.BatchObjectsRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getBatchObjectsMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch.BatchReferencesReply> batchReferences(
        io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch.BatchReferencesRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getBatchReferencesMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatchDelete.BatchDeleteReply> batchDelete(
        io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatchDelete.BatchDeleteRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getBatchDeleteMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoTenants.TenantsGetReply> tenantsGet(
        io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoTenants.TenantsGetRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getTenantsGetMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoAggregate.AggregateReply> aggregate(
        io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoAggregate.AggregateRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getAggregateMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch.BatchSendReply> batchSend(
        io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch.BatchSendRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getBatchSendMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_SEARCH = 0;
  private static final int METHODID_BATCH_OBJECTS = 1;
  private static final int METHODID_BATCH_REFERENCES = 2;
  private static final int METHODID_BATCH_DELETE = 3;
  private static final int METHODID_TENANTS_GET = 4;
  private static final int METHODID_AGGREGATE = 5;
  private static final int METHODID_BATCH_SEND = 6;
  private static final int METHODID_BATCH_STREAM = 7;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final AsyncService serviceImpl;
    private final int methodId;

    MethodHandlers(AsyncService serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_SEARCH:
          serviceImpl.search((io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoSearchGet.SearchRequest) request,
              (io.grpc.stub.StreamObserver<io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoSearchGet.SearchReply>) responseObserver);
          break;
        case METHODID_BATCH_OBJECTS:
          serviceImpl.batchObjects((io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch.BatchObjectsRequest) request,
              (io.grpc.stub.StreamObserver<io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch.BatchObjectsReply>) responseObserver);
          break;
        case METHODID_BATCH_REFERENCES:
          serviceImpl.batchReferences((io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch.BatchReferencesRequest) request,
              (io.grpc.stub.StreamObserver<io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch.BatchReferencesReply>) responseObserver);
          break;
        case METHODID_BATCH_DELETE:
          serviceImpl.batchDelete((io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatchDelete.BatchDeleteRequest) request,
              (io.grpc.stub.StreamObserver<io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatchDelete.BatchDeleteReply>) responseObserver);
          break;
        case METHODID_TENANTS_GET:
          serviceImpl.tenantsGet((io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoTenants.TenantsGetRequest) request,
              (io.grpc.stub.StreamObserver<io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoTenants.TenantsGetReply>) responseObserver);
          break;
        case METHODID_AGGREGATE:
          serviceImpl.aggregate((io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoAggregate.AggregateRequest) request,
              (io.grpc.stub.StreamObserver<io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoAggregate.AggregateReply>) responseObserver);
          break;
        case METHODID_BATCH_SEND:
          serviceImpl.batchSend((io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch.BatchSendRequest) request,
              (io.grpc.stub.StreamObserver<io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch.BatchSendReply>) responseObserver);
          break;
        case METHODID_BATCH_STREAM:
          serviceImpl.batchStream((io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch.BatchStreamRequest) request,
              (io.grpc.stub.StreamObserver<io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch.BatchStreamMessage>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  public static final io.grpc.ServerServiceDefinition bindService(AsyncService service) {
    return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
        .addMethod(
          getSearchMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoSearchGet.SearchRequest,
              io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoSearchGet.SearchReply>(
                service, METHODID_SEARCH)))
        .addMethod(
          getBatchObjectsMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch.BatchObjectsRequest,
              io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch.BatchObjectsReply>(
                service, METHODID_BATCH_OBJECTS)))
        .addMethod(
          getBatchReferencesMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch.BatchReferencesRequest,
              io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch.BatchReferencesReply>(
                service, METHODID_BATCH_REFERENCES)))
        .addMethod(
          getBatchDeleteMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatchDelete.BatchDeleteRequest,
              io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatchDelete.BatchDeleteReply>(
                service, METHODID_BATCH_DELETE)))
        .addMethod(
          getTenantsGetMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoTenants.TenantsGetRequest,
              io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoTenants.TenantsGetReply>(
                service, METHODID_TENANTS_GET)))
        .addMethod(
          getAggregateMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoAggregate.AggregateRequest,
              io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoAggregate.AggregateReply>(
                service, METHODID_AGGREGATE)))
        .addMethod(
          getBatchSendMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch.BatchSendRequest,
              io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch.BatchSendReply>(
                service, METHODID_BATCH_SEND)))
        .addMethod(
          getBatchStreamMethod(),
          io.grpc.stub.ServerCalls.asyncServerStreamingCall(
            new MethodHandlers<
              io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch.BatchStreamRequest,
              io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch.BatchStreamMessage>(
                service, METHODID_BATCH_STREAM)))
        .build();
  }

  private static abstract class WeaviateBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    WeaviateBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProto.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("Weaviate");
    }
  }

  private static final class WeaviateFileDescriptorSupplier
      extends WeaviateBaseDescriptorSupplier {
    WeaviateFileDescriptorSupplier() {}
  }

  private static final class WeaviateMethodDescriptorSupplier
      extends WeaviateBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final java.lang.String methodName;

    WeaviateMethodDescriptorSupplier(java.lang.String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (WeaviateGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new WeaviateFileDescriptorSupplier())
              .addMethod(getSearchMethod())
              .addMethod(getBatchObjectsMethod())
              .addMethod(getBatchReferencesMethod())
              .addMethod(getBatchDeleteMethod())
              .addMethod(getTenantsGetMethod())
              .addMethod(getAggregateMethod())
              .addMethod(getBatchSendMethod())
              .addMethod(getBatchStreamMethod())
              .build();
        }
      }
    }
    return result;
  }
}
