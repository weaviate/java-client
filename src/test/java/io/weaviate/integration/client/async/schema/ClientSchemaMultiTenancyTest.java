package io.weaviate.integration.client.async.schema;

import io.weaviate.client.Config;
import io.weaviate.client.WeaviateClient;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.async.WeaviateAsyncClient;
import io.weaviate.client.v1.schema.model.ActivityStatus;
import io.weaviate.client.v1.schema.model.Tenant;
import io.weaviate.integration.client.AssertMultiTenancy;
import io.weaviate.integration.client.WeaviateDockerCompose;
import io.weaviate.integration.client.WeaviateTestGenerics;
import static io.weaviate.integration.client.WeaviateTestGenerics.TENANT_1;
import static io.weaviate.integration.client.WeaviateTestGenerics.TENANT_2;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

public class ClientSchemaMultiTenancyTest {
  private WeaviateClient client;
  private WeaviateTestGenerics testGenerics;
  private AssertMultiTenancy assertMT;

  @ClassRule
  public static WeaviateDockerCompose compose = new WeaviateDockerCompose();

  @Before
  public void before() {
    String httpHost = compose.getHttpHostAddress();
    Config config = new Config("http", httpHost);

    client = new WeaviateClient(config);
    testGenerics = new WeaviateTestGenerics();
    assertMT = new AssertMultiTenancy(client);
  }

  @After
  public void after() {
    testGenerics.cleanupWeaviate(client);
  }

  @Test
  public void shouldUpdateTenantsOfMTClass() throws ExecutionException, InterruptedException {
    Tenant[] tenants = new Tenant[]{TENANT_1, TENANT_2};
    testGenerics.createSchemaPizzaForTenants(client);
    testGenerics.createTenantsPizza(client, tenants);

    try (WeaviateAsyncClient asyncClient = client.async()) {
      Result<Boolean> updateResult = asyncClient.schema().tenantsUpdater()
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
}
