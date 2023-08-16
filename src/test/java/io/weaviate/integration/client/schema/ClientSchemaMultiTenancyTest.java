package io.weaviate.integration.client.schema;

import io.weaviate.client.Config;
import io.weaviate.client.WeaviateClient;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.misc.model.MultiTenancyConfig;
import io.weaviate.client.v1.schema.model.DataType;
import io.weaviate.client.v1.schema.model.Property;
import io.weaviate.client.v1.schema.model.Tenant;
import io.weaviate.client.v1.schema.model.WeaviateClass;
import io.weaviate.integration.client.AssertMultiTenancy;
import io.weaviate.integration.client.WeaviateTestGenerics;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ClientSchemaMultiTenancyTest {

  private WeaviateClient client;
  private WeaviateTestGenerics testGenerics;
  private AssertMultiTenancy assertMT;

  @ClassRule
  public static DockerComposeContainer<?> compose = new DockerComposeContainer<>(
    new File("src/test/resources/docker-compose-test.yaml")
  ).withExposedService("weaviate_1", 8080, Wait.forHttp("/v1/.well-known/ready").forStatusCode(200));

  @Before
  public void before() {
    String host = compose.getServiceHost("weaviate_1", 8080);
    Integer port = compose.getServicePort("weaviate_1", 8080);
    Config config = new Config("http", host + ":" + port);

    client = new WeaviateClient(config);
    testGenerics = new WeaviateTestGenerics();
    assertMT = new AssertMultiTenancy(client);
  }

  @After
  public void after() {
    testGenerics.cleanupWeaviate(client);
  }


  @Test
  public void shouldCreateClassWithMultiTenancyConfig() {
    String className = "MultiTenantClass";
    WeaviateClass clazz = WeaviateClass.builder()
      .className(className)
      .multiTenancyConfig(MultiTenancyConfig.builder()
        .enabled(true)
        .build())
      .properties(Collections.singletonList(
        Property.builder()
          .name("name")
          .dataType(Collections.singletonList(DataType.TEXT))
          .build()
      ))
      .build();

    Result<Boolean> createStatus = client.schema().classCreator().withClass(clazz).run();
    assertThat(createStatus.hasErrors()).isFalse();
    assertThat(createStatus.getResult()).isTrue();

    Result<WeaviateClass> classResult = client.schema().classGetter().withClassName(className).run();
    assertThat(classResult.hasErrors()).isFalse();
    assertThat(classResult.getResult()).isNotNull()
      .extracting(WeaviateClass::getMultiTenancyConfig)
      .isNotNull()
      .returns(true, MultiTenancyConfig::getEnabled);
  }

  @Test
  public void shouldCreateClassWithMultiTenancyConfigDisabled() {
    String className = "MultiTenantClassWannabe";
    WeaviateClass clazz = WeaviateClass.builder()
      .className(className)
      .multiTenancyConfig(MultiTenancyConfig.builder()
        .enabled(false)
        .build())
      .properties(Collections.singletonList(
        Property.builder()
          .name("name")
          .dataType(Collections.singletonList(DataType.TEXT))
          .build()
      ))
      .build();

    Result<Boolean> createStatus = client.schema().classCreator().withClass(clazz).run();
    assertThat(createStatus.hasErrors()).isFalse();
    assertThat(createStatus.getResult()).isTrue();

    Result<WeaviateClass> classResult = client.schema().classGetter().withClassName(className).run();
    assertThat(classResult.hasErrors()).isFalse();
    assertThat(classResult.getResult()).isNotNull()
      .extracting(WeaviateClass::getMultiTenancyConfig)
      .isNotNull()
      .returns(false, MultiTenancyConfig::getEnabled);
  }

  @Test
  public void shouldCreateClassWithoutMultiTenancyConfig() {
    // given
    String className = "OrdinaryClass";
    WeaviateClass clazz = WeaviateClass.builder()
      .className(className)
      .properties(Collections.singletonList(
        Property.builder()
          .name("name")
          .dataType(Collections.singletonList(DataType.TEXT))
          .build()
      ))
      .build();

    // when
    Result<Boolean> createStatus = client.schema().classCreator().withClass(clazz).run();
    assertThat(createStatus.hasErrors()).isFalse();
    assertThat(createStatus.getResult()).isTrue();

    // then
    Result<WeaviateClass> classResult = client.schema().classGetter().withClassName(className).run();
    assertThat(classResult.hasErrors()).isFalse();
    assertThat(classResult.getResult()).isNotNull()
      .extracting(WeaviateClass::getMultiTenancyConfig)
      .isNotNull()
      .returns(false, MultiTenancyConfig::getEnabled);
  }

  @Test
  public void shouldAddTenantsToMTClass() {
    String[] tenants = new String[]{"TenantNo1", "TenantNo2"};
    testGenerics.createSchemaPizzaForTenants(client);

    Tenant[] tenantObjs = Arrays.stream(tenants)
      .map(tenant -> Tenant.builder().name(tenant).build())
      .toArray(Tenant[]::new);

    Result<Boolean> addResult = client.schema().tenantsCreator()
      .withClassName("Pizza")
      .withTenants(tenantObjs)
      .run();

    assertThat(addResult).isNotNull()
      .returns(false, Result::hasErrors)
      .returns(true, Result::getResult);
  }

  @Test
  public void shouldNotAddTenantsToNonMTClass() {
    String[] tenants = new String[]{"TenantNo1", "TenantNo2"};
    testGenerics.createSchemaPizza(client);

    Tenant[] tenantObjs = Arrays.stream(tenants)
      .map(tenant -> Tenant.builder().name(tenant).build())
      .toArray(Tenant[]::new);

    Result<Boolean> addResult = client.schema().tenantsCreator()
      .withClassName("Pizza")
      .withTenants(tenantObjs)
      .run();

    assertMT.error(addResult, false, 422,"multi-tenancy is not enabled for class");
  }

  @Test
  public void shouldGetTenantsFromMTClass() {
    Tenant[] tenants = new Tenant[]{
      WeaviateTestGenerics.TENANT_1,
      WeaviateTestGenerics.TENANT_2,
    };
    String[] tenantNames = Arrays.stream(tenants).map(Tenant::getName).toArray(String[]::new);
    testGenerics.createSchemaPizzaForTenants(client);
    testGenerics.createTenantsPizza(client, tenants);

    Result<List<Tenant>> getResult = client.schema().tenantsGetter()
      .withClassName("Pizza")
      .run();

    assertThat(getResult).isNotNull()
      .returns(false, Result::hasErrors)
      .extracting(Result::getResult).asList()
      .hasSize(tenants.length);

    String[] fetchedTenants = getResult.getResult().stream()
      .map(Tenant::getName)
      .toArray(String[]::new);
    assertThat(fetchedTenants).containsExactlyInAnyOrder(tenantNames);
  }

  @Test
  public void shouldNotGetTenantsFromNonMTClass() {
    testGenerics.createSchemaPizza(client);

    Result<List<Tenant>> getResult = client.schema().tenantsGetter()
      .withClassName("Pizza")
      .run();

    assertMT.error(getResult, null, 422, "multi-tenancy is not enabled for class");
  }

  @Test
  public void shouldDeleteTenantsFromMTClass() {
    Tenant[] tenants = new Tenant[]{
      WeaviateTestGenerics.TENANT_1,
      WeaviateTestGenerics.TENANT_2,
    };
    String[] tenantNames = Arrays.stream(tenants).map(Tenant::getName).toArray(String[]::new);
    testGenerics.createSchemaPizzaForTenants(client);
    testGenerics.createTenantsPizza(client, tenants);

    Result<Boolean> deleteResult = client.schema().tenantsDeleter()
      .withClassName("Pizza")
      .withTenants(tenantNames)
      .run();

    assertThat(deleteResult).isNotNull()
      .returns(false, Result::hasErrors)
      .returns(true, Result::getResult);
  }

  @Test
  public void shouldNotDeleteTenantsFromNonMTClass() {
    String[] tenants = new String[]{"TenantNo1", "TenantNo2"};
    testGenerics.createSchemaPizza(client);

    Result<Boolean> deleteResult = client.schema().tenantsDeleter()
      .withClassName("Pizza")
      .withTenants(tenants)
      .run();

    assertMT.error(deleteResult, false, 422, "multi-tenancy is not enabled for class");
  }
}
