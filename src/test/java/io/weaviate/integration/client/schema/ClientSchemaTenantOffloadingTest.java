package io.weaviate.integration.client.schema;

import io.weaviate.client.Config;
import io.weaviate.client.WeaviateClient;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.data.model.WeaviateObject;
import io.weaviate.client.v1.schema.model.ActivityStatus;
import io.weaviate.client.v1.schema.model.Tenant;
import io.weaviate.integration.client.WeaviateDockerCompose;
import io.weaviate.integration.client.WeaviateTestGenerics;
import io.weaviate.integration.client.WeaviateVersion;
import java.util.Arrays;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

public class ClientSchemaTenantOffloadingTest {

  private WeaviateClient client;
  private WeaviateTestGenerics testGenerics;

  @ClassRule
  public static WeaviateDockerCompose compose = new WeaviateDockerCompose(WeaviateVersion.WEAVIATE_IMAGE, true);

  @Before
  public void before() {
    String httpHost = compose.getHttpHostAddress();
    String grpcHost = compose.getGrpcHostAddress();
    Config config = new Config("http", httpHost);
    config.setGRPCSecured(false);
    config.setGRPCHost(grpcHost);

    client = new WeaviateClient(config);
    testGenerics = new WeaviateTestGenerics();
  }

  @After
  public void after() {
    testGenerics.cleanupWeaviate(client);
  }

  @Test
  public void shouldOffloadTenants() throws InterruptedException {
    // create tenants and class
    String className = "Pizza";
    String[] tenants = new String[]{"Tenant1", "Tenant2", "Tenant3"};
    Tenant[] tenantObjs = Arrays.stream(tenants)
      .map(tenant -> Tenant.builder().name(tenant).build())
      .toArray(Tenant[]::new);
    testGenerics.createSchemaPizzaForTenants(client);
    testGenerics.createTenantsPizza(client, tenantObjs);
    // verify tenants existence
    Result<List<Tenant>> getResult = client.schema().tenantsGetter()
      .withClassName(className)
      .run();
    assertThat(getResult).isNotNull()
      .returns(false, Result::hasErrors)
      .extracting(Result::getResult).asList()
      .hasSize(tenants.length);
    // insert data
    testGenerics.createDataPizzaForTenants(client, tenants);
    // verify data existence
    for (String tenant : tenants) {
      Result<List<WeaviateObject>> result = client.data().objectsGetter().withClassName(className).withTenant(tenant).run();
      assertThat(result).isNotNull()
        .returns(false, Result::hasErrors)
        .extracting(Result::getResult).asList().hasSize(4);
    }
    // verify tenant status HOT
    verifyEventuallyTenantStatus(className, ActivityStatus.HOT);
    // update tenants to FROZEN
    updateTenantStatus(className, tenants, ActivityStatus.FROZEN);
    // verify tenant status FREEZING
    verifyEventuallyTenantStatus(className, ActivityStatus.FROZEN);
    // verify tenants does not exist
    for (String tenant : tenants) {
      Result<List<WeaviateObject>> result = client.data().objectsGetter().withClassName(className).withTenant(tenant).run();
      assertThat(result).isNotNull()
        .returns(true, Result::hasErrors)
        .extracting(Result::getResult).isNull();
    }
    // verify tenant status FROZEN
    verifyEventuallyTenantStatus(className, ActivityStatus.FROZEN);
    // updating tenant status to HOT
    updateTenantStatus(className, tenants, ActivityStatus.HOT);
    // verify tenant status HOT
    verifyEventuallyTenantStatus(className, ActivityStatus.HOT);
    // verify object creation
    for (String tenant : tenants) {
      Result<List<WeaviateObject>> result = client.data().objectsGetter().withClassName(className).withTenant(tenant).run();
      assertThat(result).isNotNull()
        .returns(false, Result::hasErrors)
        .extracting(Result::getResult).asList().hasSize(4);
    }
  }

  private void updateTenantStatus(String className, String[] tenants, String activityStatus) {
    Tenant[] tenantsWithStatus = Arrays.stream(tenants)
      .map(tenant -> Tenant.builder().name(tenant).activityStatus(activityStatus).build())
      .toArray(Tenant[]::new);
    Result<Boolean> exists = client.schema().tenantsUpdater()
      .withClassName(className)
      .withTenants(tenantsWithStatus)
      .run();
    assertThat(exists).isNotNull()
      .returns(false, Result::hasErrors)
      .returns(true, Result::getResult);
  }

  private void verifyEventuallyTenantStatus(String className, String activityStatus) throws  InterruptedException {
    boolean statusOK = false;
    int hardBreak = 5*60;
    while(hardBreak > 0) {
      if (verifyTenantStatus(className, activityStatus)) {
        statusOK = true;
        break;
      }
      Thread.sleep(1000);
      hardBreak--;
    }
    assertThat(statusOK).isTrue();
  }

  private boolean verifyTenantStatus(String className, String activityStatus) {
    Result<List<Tenant>>  getResult = client.schema().tenantsGetter()
      .withClassName(className)
      .run();
    assertThat(getResult).isNotNull()
      .returns(false, Result::hasErrors)
      .extracting(Result::getResult).isNotNull();
    for (Tenant tenant : getResult.getResult()) {
      if (!tenant.getActivityStatus().equals(activityStatus)) {
        return false;
      }
    }
    return true;
  }
}
