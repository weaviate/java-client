package io.weaviate.client.grpc.protocol.v1;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.58.0)",
    comments = "Source: v1/file_replication.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class FileReplicationServiceGrpc {

  private FileReplicationServiceGrpc() {}

  public static final java.lang.String SERVICE_NAME = "weaviate.v1.FileReplicationService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<io.weaviate.client.grpc.protocol.v1.WeaviateProtoReplicate.PauseFileActivityRequest,
      io.weaviate.client.grpc.protocol.v1.WeaviateProtoReplicate.PauseFileActivityResponse> getPauseFileActivityMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "PauseFileActivity",
      requestType = io.weaviate.client.grpc.protocol.v1.WeaviateProtoReplicate.PauseFileActivityRequest.class,
      responseType = io.weaviate.client.grpc.protocol.v1.WeaviateProtoReplicate.PauseFileActivityResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.weaviate.client.grpc.protocol.v1.WeaviateProtoReplicate.PauseFileActivityRequest,
      io.weaviate.client.grpc.protocol.v1.WeaviateProtoReplicate.PauseFileActivityResponse> getPauseFileActivityMethod() {
    io.grpc.MethodDescriptor<io.weaviate.client.grpc.protocol.v1.WeaviateProtoReplicate.PauseFileActivityRequest, io.weaviate.client.grpc.protocol.v1.WeaviateProtoReplicate.PauseFileActivityResponse> getPauseFileActivityMethod;
    if ((getPauseFileActivityMethod = FileReplicationServiceGrpc.getPauseFileActivityMethod) == null) {
      synchronized (FileReplicationServiceGrpc.class) {
        if ((getPauseFileActivityMethod = FileReplicationServiceGrpc.getPauseFileActivityMethod) == null) {
          FileReplicationServiceGrpc.getPauseFileActivityMethod = getPauseFileActivityMethod =
              io.grpc.MethodDescriptor.<io.weaviate.client.grpc.protocol.v1.WeaviateProtoReplicate.PauseFileActivityRequest, io.weaviate.client.grpc.protocol.v1.WeaviateProtoReplicate.PauseFileActivityResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "PauseFileActivity"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.weaviate.client.grpc.protocol.v1.WeaviateProtoReplicate.PauseFileActivityRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.weaviate.client.grpc.protocol.v1.WeaviateProtoReplicate.PauseFileActivityResponse.getDefaultInstance()))
              .setSchemaDescriptor(new FileReplicationServiceMethodDescriptorSupplier("PauseFileActivity"))
              .build();
        }
      }
    }
    return getPauseFileActivityMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.weaviate.client.grpc.protocol.v1.WeaviateProtoReplicate.ResumeFileActivityRequest,
      io.weaviate.client.grpc.protocol.v1.WeaviateProtoReplicate.ResumeFileActivityResponse> getResumeFileActivityMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ResumeFileActivity",
      requestType = io.weaviate.client.grpc.protocol.v1.WeaviateProtoReplicate.ResumeFileActivityRequest.class,
      responseType = io.weaviate.client.grpc.protocol.v1.WeaviateProtoReplicate.ResumeFileActivityResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.weaviate.client.grpc.protocol.v1.WeaviateProtoReplicate.ResumeFileActivityRequest,
      io.weaviate.client.grpc.protocol.v1.WeaviateProtoReplicate.ResumeFileActivityResponse> getResumeFileActivityMethod() {
    io.grpc.MethodDescriptor<io.weaviate.client.grpc.protocol.v1.WeaviateProtoReplicate.ResumeFileActivityRequest, io.weaviate.client.grpc.protocol.v1.WeaviateProtoReplicate.ResumeFileActivityResponse> getResumeFileActivityMethod;
    if ((getResumeFileActivityMethod = FileReplicationServiceGrpc.getResumeFileActivityMethod) == null) {
      synchronized (FileReplicationServiceGrpc.class) {
        if ((getResumeFileActivityMethod = FileReplicationServiceGrpc.getResumeFileActivityMethod) == null) {
          FileReplicationServiceGrpc.getResumeFileActivityMethod = getResumeFileActivityMethod =
              io.grpc.MethodDescriptor.<io.weaviate.client.grpc.protocol.v1.WeaviateProtoReplicate.ResumeFileActivityRequest, io.weaviate.client.grpc.protocol.v1.WeaviateProtoReplicate.ResumeFileActivityResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "ResumeFileActivity"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.weaviate.client.grpc.protocol.v1.WeaviateProtoReplicate.ResumeFileActivityRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.weaviate.client.grpc.protocol.v1.WeaviateProtoReplicate.ResumeFileActivityResponse.getDefaultInstance()))
              .setSchemaDescriptor(new FileReplicationServiceMethodDescriptorSupplier("ResumeFileActivity"))
              .build();
        }
      }
    }
    return getResumeFileActivityMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.weaviate.client.grpc.protocol.v1.WeaviateProtoReplicate.ListFilesRequest,
      io.weaviate.client.grpc.protocol.v1.WeaviateProtoReplicate.ListFilesResponse> getListFilesMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ListFiles",
      requestType = io.weaviate.client.grpc.protocol.v1.WeaviateProtoReplicate.ListFilesRequest.class,
      responseType = io.weaviate.client.grpc.protocol.v1.WeaviateProtoReplicate.ListFilesResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.weaviate.client.grpc.protocol.v1.WeaviateProtoReplicate.ListFilesRequest,
      io.weaviate.client.grpc.protocol.v1.WeaviateProtoReplicate.ListFilesResponse> getListFilesMethod() {
    io.grpc.MethodDescriptor<io.weaviate.client.grpc.protocol.v1.WeaviateProtoReplicate.ListFilesRequest, io.weaviate.client.grpc.protocol.v1.WeaviateProtoReplicate.ListFilesResponse> getListFilesMethod;
    if ((getListFilesMethod = FileReplicationServiceGrpc.getListFilesMethod) == null) {
      synchronized (FileReplicationServiceGrpc.class) {
        if ((getListFilesMethod = FileReplicationServiceGrpc.getListFilesMethod) == null) {
          FileReplicationServiceGrpc.getListFilesMethod = getListFilesMethod =
              io.grpc.MethodDescriptor.<io.weaviate.client.grpc.protocol.v1.WeaviateProtoReplicate.ListFilesRequest, io.weaviate.client.grpc.protocol.v1.WeaviateProtoReplicate.ListFilesResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "ListFiles"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.weaviate.client.grpc.protocol.v1.WeaviateProtoReplicate.ListFilesRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.weaviate.client.grpc.protocol.v1.WeaviateProtoReplicate.ListFilesResponse.getDefaultInstance()))
              .setSchemaDescriptor(new FileReplicationServiceMethodDescriptorSupplier("ListFiles"))
              .build();
        }
      }
    }
    return getListFilesMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.weaviate.client.grpc.protocol.v1.WeaviateProtoReplicate.GetFileMetadataRequest,
      io.weaviate.client.grpc.protocol.v1.WeaviateProtoReplicate.FileMetadata> getGetFileMetadataMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetFileMetadata",
      requestType = io.weaviate.client.grpc.protocol.v1.WeaviateProtoReplicate.GetFileMetadataRequest.class,
      responseType = io.weaviate.client.grpc.protocol.v1.WeaviateProtoReplicate.FileMetadata.class,
      methodType = io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
  public static io.grpc.MethodDescriptor<io.weaviate.client.grpc.protocol.v1.WeaviateProtoReplicate.GetFileMetadataRequest,
      io.weaviate.client.grpc.protocol.v1.WeaviateProtoReplicate.FileMetadata> getGetFileMetadataMethod() {
    io.grpc.MethodDescriptor<io.weaviate.client.grpc.protocol.v1.WeaviateProtoReplicate.GetFileMetadataRequest, io.weaviate.client.grpc.protocol.v1.WeaviateProtoReplicate.FileMetadata> getGetFileMetadataMethod;
    if ((getGetFileMetadataMethod = FileReplicationServiceGrpc.getGetFileMetadataMethod) == null) {
      synchronized (FileReplicationServiceGrpc.class) {
        if ((getGetFileMetadataMethod = FileReplicationServiceGrpc.getGetFileMetadataMethod) == null) {
          FileReplicationServiceGrpc.getGetFileMetadataMethod = getGetFileMetadataMethod =
              io.grpc.MethodDescriptor.<io.weaviate.client.grpc.protocol.v1.WeaviateProtoReplicate.GetFileMetadataRequest, io.weaviate.client.grpc.protocol.v1.WeaviateProtoReplicate.FileMetadata>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetFileMetadata"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.weaviate.client.grpc.protocol.v1.WeaviateProtoReplicate.GetFileMetadataRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.weaviate.client.grpc.protocol.v1.WeaviateProtoReplicate.FileMetadata.getDefaultInstance()))
              .setSchemaDescriptor(new FileReplicationServiceMethodDescriptorSupplier("GetFileMetadata"))
              .build();
        }
      }
    }
    return getGetFileMetadataMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.weaviate.client.grpc.protocol.v1.WeaviateProtoReplicate.GetFileRequest,
      io.weaviate.client.grpc.protocol.v1.WeaviateProtoReplicate.FileChunk> getGetFileMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetFile",
      requestType = io.weaviate.client.grpc.protocol.v1.WeaviateProtoReplicate.GetFileRequest.class,
      responseType = io.weaviate.client.grpc.protocol.v1.WeaviateProtoReplicate.FileChunk.class,
      methodType = io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
  public static io.grpc.MethodDescriptor<io.weaviate.client.grpc.protocol.v1.WeaviateProtoReplicate.GetFileRequest,
      io.weaviate.client.grpc.protocol.v1.WeaviateProtoReplicate.FileChunk> getGetFileMethod() {
    io.grpc.MethodDescriptor<io.weaviate.client.grpc.protocol.v1.WeaviateProtoReplicate.GetFileRequest, io.weaviate.client.grpc.protocol.v1.WeaviateProtoReplicate.FileChunk> getGetFileMethod;
    if ((getGetFileMethod = FileReplicationServiceGrpc.getGetFileMethod) == null) {
      synchronized (FileReplicationServiceGrpc.class) {
        if ((getGetFileMethod = FileReplicationServiceGrpc.getGetFileMethod) == null) {
          FileReplicationServiceGrpc.getGetFileMethod = getGetFileMethod =
              io.grpc.MethodDescriptor.<io.weaviate.client.grpc.protocol.v1.WeaviateProtoReplicate.GetFileRequest, io.weaviate.client.grpc.protocol.v1.WeaviateProtoReplicate.FileChunk>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetFile"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.weaviate.client.grpc.protocol.v1.WeaviateProtoReplicate.GetFileRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.weaviate.client.grpc.protocol.v1.WeaviateProtoReplicate.FileChunk.getDefaultInstance()))
              .setSchemaDescriptor(new FileReplicationServiceMethodDescriptorSupplier("GetFile"))
              .build();
        }
      }
    }
    return getGetFileMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static FileReplicationServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<FileReplicationServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<FileReplicationServiceStub>() {
        @java.lang.Override
        public FileReplicationServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new FileReplicationServiceStub(channel, callOptions);
        }
      };
    return FileReplicationServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static FileReplicationServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<FileReplicationServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<FileReplicationServiceBlockingStub>() {
        @java.lang.Override
        public FileReplicationServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new FileReplicationServiceBlockingStub(channel, callOptions);
        }
      };
    return FileReplicationServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static FileReplicationServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<FileReplicationServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<FileReplicationServiceFutureStub>() {
        @java.lang.Override
        public FileReplicationServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new FileReplicationServiceFutureStub(channel, callOptions);
        }
      };
    return FileReplicationServiceFutureStub.newStub(factory, channel);
  }

  /**
   */
  public interface AsyncService {

    /**
     */
    default void pauseFileActivity(io.weaviate.client.grpc.protocol.v1.WeaviateProtoReplicate.PauseFileActivityRequest request,
        io.grpc.stub.StreamObserver<io.weaviate.client.grpc.protocol.v1.WeaviateProtoReplicate.PauseFileActivityResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getPauseFileActivityMethod(), responseObserver);
    }

    /**
     */
    default void resumeFileActivity(io.weaviate.client.grpc.protocol.v1.WeaviateProtoReplicate.ResumeFileActivityRequest request,
        io.grpc.stub.StreamObserver<io.weaviate.client.grpc.protocol.v1.WeaviateProtoReplicate.ResumeFileActivityResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getResumeFileActivityMethod(), responseObserver);
    }

    /**
     */
    default void listFiles(io.weaviate.client.grpc.protocol.v1.WeaviateProtoReplicate.ListFilesRequest request,
        io.grpc.stub.StreamObserver<io.weaviate.client.grpc.protocol.v1.WeaviateProtoReplicate.ListFilesResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getListFilesMethod(), responseObserver);
    }

    /**
     */
    default io.grpc.stub.StreamObserver<io.weaviate.client.grpc.protocol.v1.WeaviateProtoReplicate.GetFileMetadataRequest> getFileMetadata(
        io.grpc.stub.StreamObserver<io.weaviate.client.grpc.protocol.v1.WeaviateProtoReplicate.FileMetadata> responseObserver) {
      return io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall(getGetFileMetadataMethod(), responseObserver);
    }

    /**
     */
    default io.grpc.stub.StreamObserver<io.weaviate.client.grpc.protocol.v1.WeaviateProtoReplicate.GetFileRequest> getFile(
        io.grpc.stub.StreamObserver<io.weaviate.client.grpc.protocol.v1.WeaviateProtoReplicate.FileChunk> responseObserver) {
      return io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall(getGetFileMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service FileReplicationService.
   */
  public static abstract class FileReplicationServiceImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return FileReplicationServiceGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service FileReplicationService.
   */
  public static final class FileReplicationServiceStub
      extends io.grpc.stub.AbstractAsyncStub<FileReplicationServiceStub> {
    private FileReplicationServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected FileReplicationServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new FileReplicationServiceStub(channel, callOptions);
    }

    /**
     */
    public void pauseFileActivity(io.weaviate.client.grpc.protocol.v1.WeaviateProtoReplicate.PauseFileActivityRequest request,
        io.grpc.stub.StreamObserver<io.weaviate.client.grpc.protocol.v1.WeaviateProtoReplicate.PauseFileActivityResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getPauseFileActivityMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void resumeFileActivity(io.weaviate.client.grpc.protocol.v1.WeaviateProtoReplicate.ResumeFileActivityRequest request,
        io.grpc.stub.StreamObserver<io.weaviate.client.grpc.protocol.v1.WeaviateProtoReplicate.ResumeFileActivityResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getResumeFileActivityMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void listFiles(io.weaviate.client.grpc.protocol.v1.WeaviateProtoReplicate.ListFilesRequest request,
        io.grpc.stub.StreamObserver<io.weaviate.client.grpc.protocol.v1.WeaviateProtoReplicate.ListFilesResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getListFilesMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public io.grpc.stub.StreamObserver<io.weaviate.client.grpc.protocol.v1.WeaviateProtoReplicate.GetFileMetadataRequest> getFileMetadata(
        io.grpc.stub.StreamObserver<io.weaviate.client.grpc.protocol.v1.WeaviateProtoReplicate.FileMetadata> responseObserver) {
      return io.grpc.stub.ClientCalls.asyncBidiStreamingCall(
          getChannel().newCall(getGetFileMetadataMethod(), getCallOptions()), responseObserver);
    }

    /**
     */
    public io.grpc.stub.StreamObserver<io.weaviate.client.grpc.protocol.v1.WeaviateProtoReplicate.GetFileRequest> getFile(
        io.grpc.stub.StreamObserver<io.weaviate.client.grpc.protocol.v1.WeaviateProtoReplicate.FileChunk> responseObserver) {
      return io.grpc.stub.ClientCalls.asyncBidiStreamingCall(
          getChannel().newCall(getGetFileMethod(), getCallOptions()), responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service FileReplicationService.
   */
  public static final class FileReplicationServiceBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<FileReplicationServiceBlockingStub> {
    private FileReplicationServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected FileReplicationServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new FileReplicationServiceBlockingStub(channel, callOptions);
    }

    /**
     */
    public io.weaviate.client.grpc.protocol.v1.WeaviateProtoReplicate.PauseFileActivityResponse pauseFileActivity(io.weaviate.client.grpc.protocol.v1.WeaviateProtoReplicate.PauseFileActivityRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getPauseFileActivityMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.weaviate.client.grpc.protocol.v1.WeaviateProtoReplicate.ResumeFileActivityResponse resumeFileActivity(io.weaviate.client.grpc.protocol.v1.WeaviateProtoReplicate.ResumeFileActivityRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getResumeFileActivityMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.weaviate.client.grpc.protocol.v1.WeaviateProtoReplicate.ListFilesResponse listFiles(io.weaviate.client.grpc.protocol.v1.WeaviateProtoReplicate.ListFilesRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getListFilesMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service FileReplicationService.
   */
  public static final class FileReplicationServiceFutureStub
      extends io.grpc.stub.AbstractFutureStub<FileReplicationServiceFutureStub> {
    private FileReplicationServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected FileReplicationServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new FileReplicationServiceFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.weaviate.client.grpc.protocol.v1.WeaviateProtoReplicate.PauseFileActivityResponse> pauseFileActivity(
        io.weaviate.client.grpc.protocol.v1.WeaviateProtoReplicate.PauseFileActivityRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getPauseFileActivityMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.weaviate.client.grpc.protocol.v1.WeaviateProtoReplicate.ResumeFileActivityResponse> resumeFileActivity(
        io.weaviate.client.grpc.protocol.v1.WeaviateProtoReplicate.ResumeFileActivityRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getResumeFileActivityMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.weaviate.client.grpc.protocol.v1.WeaviateProtoReplicate.ListFilesResponse> listFiles(
        io.weaviate.client.grpc.protocol.v1.WeaviateProtoReplicate.ListFilesRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getListFilesMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_PAUSE_FILE_ACTIVITY = 0;
  private static final int METHODID_RESUME_FILE_ACTIVITY = 1;
  private static final int METHODID_LIST_FILES = 2;
  private static final int METHODID_GET_FILE_METADATA = 3;
  private static final int METHODID_GET_FILE = 4;

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
        case METHODID_PAUSE_FILE_ACTIVITY:
          serviceImpl.pauseFileActivity((io.weaviate.client.grpc.protocol.v1.WeaviateProtoReplicate.PauseFileActivityRequest) request,
              (io.grpc.stub.StreamObserver<io.weaviate.client.grpc.protocol.v1.WeaviateProtoReplicate.PauseFileActivityResponse>) responseObserver);
          break;
        case METHODID_RESUME_FILE_ACTIVITY:
          serviceImpl.resumeFileActivity((io.weaviate.client.grpc.protocol.v1.WeaviateProtoReplicate.ResumeFileActivityRequest) request,
              (io.grpc.stub.StreamObserver<io.weaviate.client.grpc.protocol.v1.WeaviateProtoReplicate.ResumeFileActivityResponse>) responseObserver);
          break;
        case METHODID_LIST_FILES:
          serviceImpl.listFiles((io.weaviate.client.grpc.protocol.v1.WeaviateProtoReplicate.ListFilesRequest) request,
              (io.grpc.stub.StreamObserver<io.weaviate.client.grpc.protocol.v1.WeaviateProtoReplicate.ListFilesResponse>) responseObserver);
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
        case METHODID_GET_FILE_METADATA:
          return (io.grpc.stub.StreamObserver<Req>) serviceImpl.getFileMetadata(
              (io.grpc.stub.StreamObserver<io.weaviate.client.grpc.protocol.v1.WeaviateProtoReplicate.FileMetadata>) responseObserver);
        case METHODID_GET_FILE:
          return (io.grpc.stub.StreamObserver<Req>) serviceImpl.getFile(
              (io.grpc.stub.StreamObserver<io.weaviate.client.grpc.protocol.v1.WeaviateProtoReplicate.FileChunk>) responseObserver);
        default:
          throw new AssertionError();
      }
    }
  }

  public static final io.grpc.ServerServiceDefinition bindService(AsyncService service) {
    return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
        .addMethod(
          getPauseFileActivityMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.weaviate.client.grpc.protocol.v1.WeaviateProtoReplicate.PauseFileActivityRequest,
              io.weaviate.client.grpc.protocol.v1.WeaviateProtoReplicate.PauseFileActivityResponse>(
                service, METHODID_PAUSE_FILE_ACTIVITY)))
        .addMethod(
          getResumeFileActivityMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.weaviate.client.grpc.protocol.v1.WeaviateProtoReplicate.ResumeFileActivityRequest,
              io.weaviate.client.grpc.protocol.v1.WeaviateProtoReplicate.ResumeFileActivityResponse>(
                service, METHODID_RESUME_FILE_ACTIVITY)))
        .addMethod(
          getListFilesMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.weaviate.client.grpc.protocol.v1.WeaviateProtoReplicate.ListFilesRequest,
              io.weaviate.client.grpc.protocol.v1.WeaviateProtoReplicate.ListFilesResponse>(
                service, METHODID_LIST_FILES)))
        .addMethod(
          getGetFileMetadataMethod(),
          io.grpc.stub.ServerCalls.asyncBidiStreamingCall(
            new MethodHandlers<
              io.weaviate.client.grpc.protocol.v1.WeaviateProtoReplicate.GetFileMetadataRequest,
              io.weaviate.client.grpc.protocol.v1.WeaviateProtoReplicate.FileMetadata>(
                service, METHODID_GET_FILE_METADATA)))
        .addMethod(
          getGetFileMethod(),
          io.grpc.stub.ServerCalls.asyncBidiStreamingCall(
            new MethodHandlers<
              io.weaviate.client.grpc.protocol.v1.WeaviateProtoReplicate.GetFileRequest,
              io.weaviate.client.grpc.protocol.v1.WeaviateProtoReplicate.FileChunk>(
                service, METHODID_GET_FILE)))
        .build();
  }

  private static abstract class FileReplicationServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    FileReplicationServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return io.weaviate.client.grpc.protocol.v1.WeaviateProtoReplicate.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("FileReplicationService");
    }
  }

  private static final class FileReplicationServiceFileDescriptorSupplier
      extends FileReplicationServiceBaseDescriptorSupplier {
    FileReplicationServiceFileDescriptorSupplier() {}
  }

  private static final class FileReplicationServiceMethodDescriptorSupplier
      extends FileReplicationServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final java.lang.String methodName;

    FileReplicationServiceMethodDescriptorSupplier(java.lang.String methodName) {
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
      synchronized (FileReplicationServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new FileReplicationServiceFileDescriptorSupplier())
              .addMethod(getPauseFileActivityMethod())
              .addMethod(getResumeFileActivityMethod())
              .addMethod(getListFilesMethod())
              .addMethod(getGetFileMetadataMethod())
              .addMethod(getGetFileMethod())
              .build();
        }
      }
    }
    return result;
  }
}
