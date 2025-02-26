package io.weaviate.client.grpc.protocol.v1;

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
  private static volatile io.grpc.MethodDescriptor<io.weaviate.client.grpc.protocol.v1.WeaviateProtoSearchGet.SearchRequest,
      io.weaviate.client.grpc.protocol.v1.WeaviateProtoSearchGet.SearchReply> getSearchMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "Search",
      requestType = io.weaviate.client.grpc.protocol.v1.WeaviateProtoSearchGet.SearchRequest.class,
      responseType = io.weaviate.client.grpc.protocol.v1.WeaviateProtoSearchGet.SearchReply.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.weaviate.client.grpc.protocol.v1.WeaviateProtoSearchGet.SearchRequest,
      io.weaviate.client.grpc.protocol.v1.WeaviateProtoSearchGet.SearchReply> getSearchMethod() {
    io.grpc.MethodDescriptor<io.weaviate.client.grpc.protocol.v1.WeaviateProtoSearchGet.SearchRequest, io.weaviate.client.grpc.protocol.v1.WeaviateProtoSearchGet.SearchReply> getSearchMethod;
    if ((getSearchMethod = WeaviateGrpc.getSearchMethod) == null) {
      synchronized (WeaviateGrpc.class) {
        if ((getSearchMethod = WeaviateGrpc.getSearchMethod) == null) {
          WeaviateGrpc.getSearchMethod = getSearchMethod =
              io.grpc.MethodDescriptor.<io.weaviate.client.grpc.protocol.v1.WeaviateProtoSearchGet.SearchRequest, io.weaviate.client.grpc.protocol.v1.WeaviateProtoSearchGet.SearchReply>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "Search"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.weaviate.client.grpc.protocol.v1.WeaviateProtoSearchGet.SearchRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.weaviate.client.grpc.protocol.v1.WeaviateProtoSearchGet.SearchReply.getDefaultInstance()))
              .setSchemaDescriptor(new WeaviateMethodDescriptorSupplier("Search"))
              .build();
        }
      }
    }
    return getSearchMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.weaviate.client.grpc.protocol.v1.WeaviateProtoBatch.BatchObjectsRequest,
      io.weaviate.client.grpc.protocol.v1.WeaviateProtoBatch.BatchObjectsReply> getBatchObjectsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "BatchObjects",
      requestType = io.weaviate.client.grpc.protocol.v1.WeaviateProtoBatch.BatchObjectsRequest.class,
      responseType = io.weaviate.client.grpc.protocol.v1.WeaviateProtoBatch.BatchObjectsReply.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.weaviate.client.grpc.protocol.v1.WeaviateProtoBatch.BatchObjectsRequest,
      io.weaviate.client.grpc.protocol.v1.WeaviateProtoBatch.BatchObjectsReply> getBatchObjectsMethod() {
    io.grpc.MethodDescriptor<io.weaviate.client.grpc.protocol.v1.WeaviateProtoBatch.BatchObjectsRequest, io.weaviate.client.grpc.protocol.v1.WeaviateProtoBatch.BatchObjectsReply> getBatchObjectsMethod;
    if ((getBatchObjectsMethod = WeaviateGrpc.getBatchObjectsMethod) == null) {
      synchronized (WeaviateGrpc.class) {
        if ((getBatchObjectsMethod = WeaviateGrpc.getBatchObjectsMethod) == null) {
          WeaviateGrpc.getBatchObjectsMethod = getBatchObjectsMethod =
              io.grpc.MethodDescriptor.<io.weaviate.client.grpc.protocol.v1.WeaviateProtoBatch.BatchObjectsRequest, io.weaviate.client.grpc.protocol.v1.WeaviateProtoBatch.BatchObjectsReply>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "BatchObjects"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.weaviate.client.grpc.protocol.v1.WeaviateProtoBatch.BatchObjectsRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.weaviate.client.grpc.protocol.v1.WeaviateProtoBatch.BatchObjectsReply.getDefaultInstance()))
              .setSchemaDescriptor(new WeaviateMethodDescriptorSupplier("BatchObjects"))
              .build();
        }
      }
    }
    return getBatchObjectsMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.weaviate.client.grpc.protocol.v1.WeaviateProtoBatchDelete.BatchDeleteRequest,
      io.weaviate.client.grpc.protocol.v1.WeaviateProtoBatchDelete.BatchDeleteReply> getBatchDeleteMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "BatchDelete",
      requestType = io.weaviate.client.grpc.protocol.v1.WeaviateProtoBatchDelete.BatchDeleteRequest.class,
      responseType = io.weaviate.client.grpc.protocol.v1.WeaviateProtoBatchDelete.BatchDeleteReply.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.weaviate.client.grpc.protocol.v1.WeaviateProtoBatchDelete.BatchDeleteRequest,
      io.weaviate.client.grpc.protocol.v1.WeaviateProtoBatchDelete.BatchDeleteReply> getBatchDeleteMethod() {
    io.grpc.MethodDescriptor<io.weaviate.client.grpc.protocol.v1.WeaviateProtoBatchDelete.BatchDeleteRequest, io.weaviate.client.grpc.protocol.v1.WeaviateProtoBatchDelete.BatchDeleteReply> getBatchDeleteMethod;
    if ((getBatchDeleteMethod = WeaviateGrpc.getBatchDeleteMethod) == null) {
      synchronized (WeaviateGrpc.class) {
        if ((getBatchDeleteMethod = WeaviateGrpc.getBatchDeleteMethod) == null) {
          WeaviateGrpc.getBatchDeleteMethod = getBatchDeleteMethod =
              io.grpc.MethodDescriptor.<io.weaviate.client.grpc.protocol.v1.WeaviateProtoBatchDelete.BatchDeleteRequest, io.weaviate.client.grpc.protocol.v1.WeaviateProtoBatchDelete.BatchDeleteReply>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "BatchDelete"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.weaviate.client.grpc.protocol.v1.WeaviateProtoBatchDelete.BatchDeleteRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.weaviate.client.grpc.protocol.v1.WeaviateProtoBatchDelete.BatchDeleteReply.getDefaultInstance()))
              .setSchemaDescriptor(new WeaviateMethodDescriptorSupplier("BatchDelete"))
              .build();
        }
      }
    }
    return getBatchDeleteMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.weaviate.client.grpc.protocol.v1.WeaviateProtoTenants.TenantsGetRequest,
      io.weaviate.client.grpc.protocol.v1.WeaviateProtoTenants.TenantsGetReply> getTenantsGetMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "TenantsGet",
      requestType = io.weaviate.client.grpc.protocol.v1.WeaviateProtoTenants.TenantsGetRequest.class,
      responseType = io.weaviate.client.grpc.protocol.v1.WeaviateProtoTenants.TenantsGetReply.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.weaviate.client.grpc.protocol.v1.WeaviateProtoTenants.TenantsGetRequest,
      io.weaviate.client.grpc.protocol.v1.WeaviateProtoTenants.TenantsGetReply> getTenantsGetMethod() {
    io.grpc.MethodDescriptor<io.weaviate.client.grpc.protocol.v1.WeaviateProtoTenants.TenantsGetRequest, io.weaviate.client.grpc.protocol.v1.WeaviateProtoTenants.TenantsGetReply> getTenantsGetMethod;
    if ((getTenantsGetMethod = WeaviateGrpc.getTenantsGetMethod) == null) {
      synchronized (WeaviateGrpc.class) {
        if ((getTenantsGetMethod = WeaviateGrpc.getTenantsGetMethod) == null) {
          WeaviateGrpc.getTenantsGetMethod = getTenantsGetMethod =
              io.grpc.MethodDescriptor.<io.weaviate.client.grpc.protocol.v1.WeaviateProtoTenants.TenantsGetRequest, io.weaviate.client.grpc.protocol.v1.WeaviateProtoTenants.TenantsGetReply>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "TenantsGet"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.weaviate.client.grpc.protocol.v1.WeaviateProtoTenants.TenantsGetRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.weaviate.client.grpc.protocol.v1.WeaviateProtoTenants.TenantsGetReply.getDefaultInstance()))
              .setSchemaDescriptor(new WeaviateMethodDescriptorSupplier("TenantsGet"))
              .build();
        }
      }
    }
    return getTenantsGetMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.weaviate.client.grpc.protocol.v1.WeaviateProtoAggregate.AggregateRequest,
      io.weaviate.client.grpc.protocol.v1.WeaviateProtoAggregate.AggregateReply> getAggregateMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "Aggregate",
      requestType = io.weaviate.client.grpc.protocol.v1.WeaviateProtoAggregate.AggregateRequest.class,
      responseType = io.weaviate.client.grpc.protocol.v1.WeaviateProtoAggregate.AggregateReply.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.weaviate.client.grpc.protocol.v1.WeaviateProtoAggregate.AggregateRequest,
      io.weaviate.client.grpc.protocol.v1.WeaviateProtoAggregate.AggregateReply> getAggregateMethod() {
    io.grpc.MethodDescriptor<io.weaviate.client.grpc.protocol.v1.WeaviateProtoAggregate.AggregateRequest, io.weaviate.client.grpc.protocol.v1.WeaviateProtoAggregate.AggregateReply> getAggregateMethod;
    if ((getAggregateMethod = WeaviateGrpc.getAggregateMethod) == null) {
      synchronized (WeaviateGrpc.class) {
        if ((getAggregateMethod = WeaviateGrpc.getAggregateMethod) == null) {
          WeaviateGrpc.getAggregateMethod = getAggregateMethod =
              io.grpc.MethodDescriptor.<io.weaviate.client.grpc.protocol.v1.WeaviateProtoAggregate.AggregateRequest, io.weaviate.client.grpc.protocol.v1.WeaviateProtoAggregate.AggregateReply>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "Aggregate"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.weaviate.client.grpc.protocol.v1.WeaviateProtoAggregate.AggregateRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.weaviate.client.grpc.protocol.v1.WeaviateProtoAggregate.AggregateReply.getDefaultInstance()))
              .setSchemaDescriptor(new WeaviateMethodDescriptorSupplier("Aggregate"))
              .build();
        }
      }
    }
    return getAggregateMethod;
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
    default void search(io.weaviate.client.grpc.protocol.v1.WeaviateProtoSearchGet.SearchRequest request,
        io.grpc.stub.StreamObserver<io.weaviate.client.grpc.protocol.v1.WeaviateProtoSearchGet.SearchReply> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getSearchMethod(), responseObserver);
    }

    /**
     */
    default void batchObjects(io.weaviate.client.grpc.protocol.v1.WeaviateProtoBatch.BatchObjectsRequest request,
        io.grpc.stub.StreamObserver<io.weaviate.client.grpc.protocol.v1.WeaviateProtoBatch.BatchObjectsReply> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getBatchObjectsMethod(), responseObserver);
    }

    /**
     */
    default void batchDelete(io.weaviate.client.grpc.protocol.v1.WeaviateProtoBatchDelete.BatchDeleteRequest request,
        io.grpc.stub.StreamObserver<io.weaviate.client.grpc.protocol.v1.WeaviateProtoBatchDelete.BatchDeleteReply> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getBatchDeleteMethod(), responseObserver);
    }

    /**
     */
    default void tenantsGet(io.weaviate.client.grpc.protocol.v1.WeaviateProtoTenants.TenantsGetRequest request,
        io.grpc.stub.StreamObserver<io.weaviate.client.grpc.protocol.v1.WeaviateProtoTenants.TenantsGetReply> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getTenantsGetMethod(), responseObserver);
    }

    /**
     */
    default void aggregate(io.weaviate.client.grpc.protocol.v1.WeaviateProtoAggregate.AggregateRequest request,
        io.grpc.stub.StreamObserver<io.weaviate.client.grpc.protocol.v1.WeaviateProtoAggregate.AggregateReply> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getAggregateMethod(), responseObserver);
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
    public void search(io.weaviate.client.grpc.protocol.v1.WeaviateProtoSearchGet.SearchRequest request,
        io.grpc.stub.StreamObserver<io.weaviate.client.grpc.protocol.v1.WeaviateProtoSearchGet.SearchReply> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getSearchMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void batchObjects(io.weaviate.client.grpc.protocol.v1.WeaviateProtoBatch.BatchObjectsRequest request,
        io.grpc.stub.StreamObserver<io.weaviate.client.grpc.protocol.v1.WeaviateProtoBatch.BatchObjectsReply> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getBatchObjectsMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void batchDelete(io.weaviate.client.grpc.protocol.v1.WeaviateProtoBatchDelete.BatchDeleteRequest request,
        io.grpc.stub.StreamObserver<io.weaviate.client.grpc.protocol.v1.WeaviateProtoBatchDelete.BatchDeleteReply> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getBatchDeleteMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void tenantsGet(io.weaviate.client.grpc.protocol.v1.WeaviateProtoTenants.TenantsGetRequest request,
        io.grpc.stub.StreamObserver<io.weaviate.client.grpc.protocol.v1.WeaviateProtoTenants.TenantsGetReply> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getTenantsGetMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void aggregate(io.weaviate.client.grpc.protocol.v1.WeaviateProtoAggregate.AggregateRequest request,
        io.grpc.stub.StreamObserver<io.weaviate.client.grpc.protocol.v1.WeaviateProtoAggregate.AggregateReply> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getAggregateMethod(), getCallOptions()), request, responseObserver);
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
    public io.weaviate.client.grpc.protocol.v1.WeaviateProtoSearchGet.SearchReply search(io.weaviate.client.grpc.protocol.v1.WeaviateProtoSearchGet.SearchRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getSearchMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.weaviate.client.grpc.protocol.v1.WeaviateProtoBatch.BatchObjectsReply batchObjects(io.weaviate.client.grpc.protocol.v1.WeaviateProtoBatch.BatchObjectsRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getBatchObjectsMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.weaviate.client.grpc.protocol.v1.WeaviateProtoBatchDelete.BatchDeleteReply batchDelete(io.weaviate.client.grpc.protocol.v1.WeaviateProtoBatchDelete.BatchDeleteRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getBatchDeleteMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.weaviate.client.grpc.protocol.v1.WeaviateProtoTenants.TenantsGetReply tenantsGet(io.weaviate.client.grpc.protocol.v1.WeaviateProtoTenants.TenantsGetRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getTenantsGetMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.weaviate.client.grpc.protocol.v1.WeaviateProtoAggregate.AggregateReply aggregate(io.weaviate.client.grpc.protocol.v1.WeaviateProtoAggregate.AggregateRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getAggregateMethod(), getCallOptions(), request);
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
    public com.google.common.util.concurrent.ListenableFuture<io.weaviate.client.grpc.protocol.v1.WeaviateProtoSearchGet.SearchReply> search(
        io.weaviate.client.grpc.protocol.v1.WeaviateProtoSearchGet.SearchRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getSearchMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.weaviate.client.grpc.protocol.v1.WeaviateProtoBatch.BatchObjectsReply> batchObjects(
        io.weaviate.client.grpc.protocol.v1.WeaviateProtoBatch.BatchObjectsRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getBatchObjectsMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.weaviate.client.grpc.protocol.v1.WeaviateProtoBatchDelete.BatchDeleteReply> batchDelete(
        io.weaviate.client.grpc.protocol.v1.WeaviateProtoBatchDelete.BatchDeleteRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getBatchDeleteMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.weaviate.client.grpc.protocol.v1.WeaviateProtoTenants.TenantsGetReply> tenantsGet(
        io.weaviate.client.grpc.protocol.v1.WeaviateProtoTenants.TenantsGetRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getTenantsGetMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.weaviate.client.grpc.protocol.v1.WeaviateProtoAggregate.AggregateReply> aggregate(
        io.weaviate.client.grpc.protocol.v1.WeaviateProtoAggregate.AggregateRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getAggregateMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_SEARCH = 0;
  private static final int METHODID_BATCH_OBJECTS = 1;
  private static final int METHODID_BATCH_DELETE = 2;
  private static final int METHODID_TENANTS_GET = 3;
  private static final int METHODID_AGGREGATE = 4;

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
          serviceImpl.search((io.weaviate.client.grpc.protocol.v1.WeaviateProtoSearchGet.SearchRequest) request,
              (io.grpc.stub.StreamObserver<io.weaviate.client.grpc.protocol.v1.WeaviateProtoSearchGet.SearchReply>) responseObserver);
          break;
        case METHODID_BATCH_OBJECTS:
          serviceImpl.batchObjects((io.weaviate.client.grpc.protocol.v1.WeaviateProtoBatch.BatchObjectsRequest) request,
              (io.grpc.stub.StreamObserver<io.weaviate.client.grpc.protocol.v1.WeaviateProtoBatch.BatchObjectsReply>) responseObserver);
          break;
        case METHODID_BATCH_DELETE:
          serviceImpl.batchDelete((io.weaviate.client.grpc.protocol.v1.WeaviateProtoBatchDelete.BatchDeleteRequest) request,
              (io.grpc.stub.StreamObserver<io.weaviate.client.grpc.protocol.v1.WeaviateProtoBatchDelete.BatchDeleteReply>) responseObserver);
          break;
        case METHODID_TENANTS_GET:
          serviceImpl.tenantsGet((io.weaviate.client.grpc.protocol.v1.WeaviateProtoTenants.TenantsGetRequest) request,
              (io.grpc.stub.StreamObserver<io.weaviate.client.grpc.protocol.v1.WeaviateProtoTenants.TenantsGetReply>) responseObserver);
          break;
        case METHODID_AGGREGATE:
          serviceImpl.aggregate((io.weaviate.client.grpc.protocol.v1.WeaviateProtoAggregate.AggregateRequest) request,
              (io.grpc.stub.StreamObserver<io.weaviate.client.grpc.protocol.v1.WeaviateProtoAggregate.AggregateReply>) responseObserver);
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
              io.weaviate.client.grpc.protocol.v1.WeaviateProtoSearchGet.SearchRequest,
              io.weaviate.client.grpc.protocol.v1.WeaviateProtoSearchGet.SearchReply>(
                service, METHODID_SEARCH)))
        .addMethod(
          getBatchObjectsMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.weaviate.client.grpc.protocol.v1.WeaviateProtoBatch.BatchObjectsRequest,
              io.weaviate.client.grpc.protocol.v1.WeaviateProtoBatch.BatchObjectsReply>(
                service, METHODID_BATCH_OBJECTS)))
        .addMethod(
          getBatchDeleteMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.weaviate.client.grpc.protocol.v1.WeaviateProtoBatchDelete.BatchDeleteRequest,
              io.weaviate.client.grpc.protocol.v1.WeaviateProtoBatchDelete.BatchDeleteReply>(
                service, METHODID_BATCH_DELETE)))
        .addMethod(
          getTenantsGetMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.weaviate.client.grpc.protocol.v1.WeaviateProtoTenants.TenantsGetRequest,
              io.weaviate.client.grpc.protocol.v1.WeaviateProtoTenants.TenantsGetReply>(
                service, METHODID_TENANTS_GET)))
        .addMethod(
          getAggregateMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.weaviate.client.grpc.protocol.v1.WeaviateProtoAggregate.AggregateRequest,
              io.weaviate.client.grpc.protocol.v1.WeaviateProtoAggregate.AggregateReply>(
                service, METHODID_AGGREGATE)))
        .build();
  }

  private static abstract class WeaviateBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    WeaviateBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return io.weaviate.client.grpc.protocol.v1.WeaviateProto.getDescriptor();
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
              .addMethod(getBatchDeleteMethod())
              .addMethod(getTenantsGetMethod())
              .addMethod(getAggregateMethod())
              .build();
        }
      }
    }
    return result;
  }
}
