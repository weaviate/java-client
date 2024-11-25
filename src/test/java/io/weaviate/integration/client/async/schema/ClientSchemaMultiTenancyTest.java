package io.weaviate.integration.client.async.schema;

import static io.weaviate.integration.client.WeaviateTestGenerics.TENANT_1;
import static io.weaviate.integration.client.WeaviateTestGenerics.TENANT_2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.LIST;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import io.weaviate.client.Config;
import io.weaviate.client.WeaviateClient;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.async.WeaviateAsyncClient;
import io.weaviate.client.v1.schema.model.ActivityStatus;
import io.weaviate.client.v1.schema.model.Tenant;
import io.weaviate.integration.client.WeaviateDockerCompose;
import io.weaviate.integration.client.WeaviateTestGenerics;

public class ClientSchemaMultiTenancyTest {
  private WeaviateClient syncClient;
  private WeaviateTestGenerics testGenerics;

  @ClassRule
  public static WeaviateDockerCompose compose = new WeaviateDockerCompose();

  @Before
  public void before() {
    String httpHost = compose.getHttpHostAddress();
    Config config = new Config("http", httpHost);

    syncClient = new WeaviateClient(config);
    testGenerics = new WeaviateTestGenerics();
    testGenerics.createSchemaPizzaForTenants(syncClient);
  }

  @After
  public void after() {
    testGenerics.cleanupWeaviate(syncClient);
  }

  @Test
  public void shouldGetTenantsFromMTClass() throws ExecutionException, InterruptedException {
    Tenant[] tenants = new Tenant[]{TENANT_1, TENANT_2};
    String[] tenantNames = Arrays.stream(tenants).map(Tenant::getName).toArray(String[]::new);
    testGenerics.createTenantsPizza(syncClient, tenants);

    try (WeaviateAsyncClient client = syncClient.async()) {
      Result<List<Tenant>> getResult = client.schema().tenantsGetter()
        .withClassName("Pizza")
        .run().get();

      assertThat(getResult).isNotNull()
        .returns(false, Result::hasErrors)
        .extracting(Result::getResult).asInstanceOf(LIST)
        .hasSize(tenants.length);

      String[] fetchedTenants = getResult.getResult().stream()
        .map(Tenant::getName)
        .toArray(String[]::new);
      assertThat(fetchedTenants).containsExactlyInAnyOrder(tenantNames);
    }
  }

  @Test
  public void shouldAddTenantsToMTClass() throws ExecutionException, InterruptedException {
    String[] tenants = new String[]{"TenantNo1", "TenantNo2"};

    Tenant[] tenantObjs = Arrays.stream(tenants)
      .map(tenant -> Tenant.builder().name(tenant).build())
      .toArray(Tenant[]::new);

    try (WeaviateAsyncClient client = syncClient.async()) {
      Result<Boolean> addResult = client.schema().tenantsCreator()
        .withClassName("Pizza")
        .withTenants(tenantObjs)
        .run().get();

      assertThat(addResult).isNotNull()
        .returns(false, Result::hasErrors)
        .returns(true, Result::getResult);

      for (String tenant: tenants) {
        Result<Boolean> exists = client.schema().tenantsExists()
          .withClassName("Pizza")
          .withTenant(tenant)
          .run().get();

        assertThat(exists).isNotNull()
          .returns(false, Result::hasErrors)
          .returns(true, Result::getResult);
      }
    }
  }

  @Test
  public void shouldUpdateTenantsOfMTClass() throws ExecutionException, InterruptedException {
    Tenant[] tenants = new Tenant[]{TENANT_1, TENANT_2};
    testGenerics.createTenantsPizza(syncClient, tenants);

    try (WeaviateAsyncClient client = syncClient.async()) {
      Result<Boolean> updateResult = client.schema().tenantsUpdater()
        .withClassName("Pizza")
        .withTenants(Arrays.stream(tenants)
          .map(tenant -> Tenant.builder().name(tenant.getName()).activityStatus(ActivityStatus.COLD).build())
          .toArray(Tenant[]::new))
        .run().get();

      assertThat(updateResult).isNotNull()
        .returns(false, Result::hasErrors)
        .returns(true, Result::getResult);
    }
  }


  @Test
  public void shouldDeleteTenantsFromMTClass() throws ExecutionException, InterruptedException {
    Tenant[] tenants = new Tenant[]{TENANT_1, TENANT_2};
    testGenerics.createTenantsPizza(syncClient, tenants);

    try (WeaviateAsyncClient client = syncClient.async()) {
      Result<Boolean> deleteResult = client.schema().tenantsDeleter()
        .withClassName("Pizza")
        .withTenants(TENANT_1.getName(), TENANT_2.getName(), "nonExistentTenant")
        .run().get();

      assertThat(deleteResult).isNotNull()
        .returns(false, Result::hasErrors)
        .returns(true, Result::getResult);
    }
  }
}
