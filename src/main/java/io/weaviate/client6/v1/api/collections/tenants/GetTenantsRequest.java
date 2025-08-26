package io.weaviate.client6.v1.api.collections.tenants;

import java.util.List;

import io.weaviate.client6.v1.internal.grpc.Rpc;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateGrpc.WeaviateBlockingStub;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateGrpc.WeaviateFutureStub;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoTenants;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoTenants.TenantNames;
import io.weaviate.client6.v1.internal.orm.CollectionDescriptor;

public record GetTenantsRequest(List<String> tenants) {
  static final Rpc<GetTenantsRequest, WeaviateProtoTenants.TenantsGetRequest, List<Tenant>, WeaviateProtoTenants.TenantsGetReply> rpc(
      CollectionDescriptor<?> collection) {
    return Rpc.of(
        request -> {
          var message = WeaviateProtoTenants.TenantsGetRequest.newBuilder()
              .setCollection(collection.name());

          if (!request.tenants.isEmpty()) {
            message.setNames(TenantNames.newBuilder()
                .addAllValues(request.tenants)
                .build());
          }
          return message.build();
        },
        response -> {
          return response.getTenantsList().stream()
              .map(t -> {
                TenantStatus status;
                switch (t.getActivityStatus()) {
                  case TENANT_ACTIVITY_STATUS_ACTIVE, TENANT_ACTIVITY_STATUS_HOT:
                    status = TenantStatus.ACTIVE;
                    break;
                  case TENANT_ACTIVITY_STATUS_INACTIVE, TENANT_ACTIVITY_STATUS_COLD:
                    status = TenantStatus.INACTIVE;
                    break;
                  case TENANT_ACTIVITY_STATUS_FROZEN, TENANT_ACTIVITY_STATUS_OFFLOADED:
                    status = TenantStatus.OFFLOADED;
                    break;
                  case TENANT_ACTIVITY_STATUS_OFFLOADING, TENANT_ACTIVITY_STATUS_FREEZING:
                    status = TenantStatus.OFFLOADING;
                    break;
                  case TENANT_ACTIVITY_STATUS_ONLOADING, TENANT_ACTIVITY_STATUS_UNFREEZING:
                    status = TenantStatus.ONLOADING;
                    break;
                  default:
                    throw new RuntimeException("unknown tenant status " + t.getActivityStatus().toString());
                }
                return new Tenant(t.getName(), status);
              })
              .toList();
        },
        () -> WeaviateBlockingStub::tenantsGet,
        () -> WeaviateFutureStub::tenantsGet);
  };
}
