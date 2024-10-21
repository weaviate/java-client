package io.weaviate.client.v1.schema.api;

import io.weaviate.client.Config;
import io.weaviate.client.base.BaseClient;
import io.weaviate.client.base.ClientResult;
import io.weaviate.client.base.Response;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.http.HttpClient;
import io.weaviate.client.base.util.DbVersionSupport;
import io.weaviate.client.base.util.UrlEncoder;
import io.weaviate.client.v1.schema.model.Tenant;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.hc.core5.http.HttpStatus;

public class TenantsUpdater extends BaseClient<Tenant[]> implements ClientResult<Boolean> {

  private final static int BATCH_SIZE = 100;
  private final DbVersionSupport dbVersionSupport;
  private String className;
  private Tenant[] tenants;

  public TenantsUpdater(HttpClient httpClient, Config config, DbVersionSupport dbVersionSupport) {
    super(httpClient, config);
    this.dbVersionSupport = dbVersionSupport;
  }

  public TenantsUpdater withClassName(String className) {
    this.className = className;
    return this;
  }

  public TenantsUpdater withTenants(Tenant... tenants) {
    this.tenants = tenants;
    return this;
  }

  @Override
  public Result<Boolean> run() {
    if (dbVersionSupport.supportsOnly100TenantsInOneRequest() && tenants != null && tenants.length > BATCH_SIZE) {
      for (List<Tenant> batch : chunkTenants(tenants, BATCH_SIZE)) {
        Result<Boolean> resp = updateTenants(batch.toArray(new Tenant[0]));
        if (resp.hasErrors()) {
          return resp;
        }
      }
      return new Result<>(200, true, null);
    }
    return updateTenants(tenants);
  }

  private Result<Boolean> updateTenants(Tenant[] tenants) {
    String path = String.format("/schema/%s/tenants", UrlEncoder.encodePathParam(className));
    Response<Tenant[]> resp = sendPutRequest(path, tenants, Tenant[].class);
    return new Result<>(resp.getStatusCode(), resp.getStatusCode() == HttpStatus.SC_OK, resp.getErrors());
  }

  private Collection<List<Tenant>> chunkTenants(Tenant[] tenants, int chunkSize) {
    AtomicInteger counter = new AtomicInteger();
    return Stream.of(tenants).collect(Collectors.groupingBy(it -> counter.getAndIncrement() / chunkSize)).values();
  }
}
