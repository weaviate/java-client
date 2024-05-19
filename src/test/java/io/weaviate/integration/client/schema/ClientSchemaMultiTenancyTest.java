package io.weaviate.integration.client.schema;

import io.weaviate.client.Config;
import io.weaviate.client.WeaviateClient;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.misc.model.MultiTenancyConfig;
import io.weaviate.client.v1.schema.model.ActivityStatus;
import io.weaviate.client.v1.schema.model.DataType;
import io.weaviate.client.v1.schema.model.Property;
import io.weaviate.client.v1.schema.model.Tenant;
import io.weaviate.client.v1.schema.model.WeaviateClass;
import io.weaviate.integration.client.AssertMultiTenancy;
import io.weaviate.integration.client.WeaviateDockerCompose;
import io.weaviate.integration.client.WeaviateTestGenerics;
import static io.weaviate.integration.client.WeaviateTestGenerics.TENANT_1;
import static io.weaviate.integration.client.WeaviateTestGenerics.TENANT_2;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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

    for (String tenant: tenants) {
      Result<Boolean> exists = client.schema().tenantsExists()
        .withClassName("Pizza")
        .withTenant(tenant)
        .run();

      assertThat(exists).isNotNull()
        .returns(false, Result::hasErrors)
        .returns(true, Result::getResult);
    }
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

    assertMT.error(addResult, false, 422, "multi-tenancy is not enabled for class");
  }

  @Test
  public void shouldGetTenantsFromMTClass() {
    Tenant[] tenants = new Tenant[]{TENANT_1, TENANT_2};
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
  public void shouldUpdateTenantsOfMTClass() {
    Tenant[] tenants = new Tenant[]{TENANT_1, TENANT_2};
    testGenerics.createSchemaPizzaForTenants(client);
    testGenerics.createTenantsPizza(client, tenants);

    Result<Boolean> updateResult = client.schema().tenantsUpdater()
      .withClassName("Pizza")
      .withTenants(Arrays.stream(tenants)
        .map(tenant -> Tenant.builder().name(tenant.getName()).activityStatus(ActivityStatus.COLD).build())
        .toArray(Tenant[]::new))
      .run();

    assertThat(updateResult).isNotNull()
      .returns(false, Result::hasErrors)
      .returns(true, Result::getResult);
  }

  @Test
  public void shouldNotUpdateNonExistentTenantsOfMTClass() {
    Tenant[] tenants = new Tenant[]{TENANT_1, TENANT_2};
    testGenerics.createSchemaPizzaForTenants(client);
    testGenerics.createTenantsPizza(client, tenants);

    Result<Boolean> updateResult = client.schema().tenantsUpdater()
      .withClassName("Pizza")
      .withTenants(Tenant.builder().name("nonExistentTenant").activityStatus(ActivityStatus.COLD).build())
      .run();

    assertMT.error(updateResult, false, 422, "nonExistentTenant", "not found");
  }

  @Test
  public void shouldNotUpdateTenantsOfNonMTClass() {
    testGenerics.createSchemaPizza(client);

    Result<Boolean> updateResult = client.schema().tenantsUpdater()
      .withClassName("Pizza")
      .withTenants(
        Tenant.builder().name(TENANT_1.getName()).activityStatus(ActivityStatus.COLD).build(),
        Tenant.builder().name(TENANT_2.getName()).activityStatus(ActivityStatus.COLD).build())
      .run();

    assertMT.error(updateResult, false, 422, "multi-tenancy is not enabled for class");
  }

  @Test
  public void shouldDeleteTenantsFromMTClass() {
    Tenant[] tenants = new Tenant[]{TENANT_1, TENANT_2};
    testGenerics.createSchemaPizzaForTenants(client);
    testGenerics.createTenantsPizza(client, tenants);

    Result<Boolean> deleteResult = client.schema().tenantsDeleter()
      .withClassName("Pizza")
      .withTenants(TENANT_1.getName(), TENANT_2.getName(), "nonExistentTenant")
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

  @Test
  public void shouldActivateDeactivateTenants() {
    Tenant[] tenants = new Tenant[]{
      Tenant.builder().name("TenantNo1").build(), // default activity status (HOT)
      Tenant.builder().name("TenantNo2").activityStatus(ActivityStatus.HOT).build(),
      Tenant.builder().name("TenantNo3").activityStatus(ActivityStatus.COLD).build(),
    };

    String classPizza = "Pizza";
    int pizzaSize = WeaviateTestGenerics.IDS_BY_CLASS.get(classPizza).size();

    // create tenants (1,2,3)
    testGenerics.createSchemaPizzaForTenants(client);
    testGenerics.createTenantsPizza(client, tenants);

    // populate active tenants (1,2)
    testGenerics.createDataPizzaForTenants(client, tenants[0].getName(), tenants[1].getName());

    assertMT.tenantActive(classPizza, tenants[0].getName());
    assertMT.tenantActiveGetsObjects(classPizza, tenants[0].getName(), pizzaSize);
    assertMT.tenantActive(classPizza, tenants[1].getName());
    assertMT.tenantActiveGetsObjects(classPizza, tenants[1].getName(), pizzaSize);
    assertMT.tenantInactive(classPizza, tenants[2].getName());
    assertMT.tenantInactiveGetsNoObjects(classPizza, tenants[2].getName());

    // deactivate tenant (1)
    Result<Boolean> result = client.schema().tenantsUpdater()
      .withClassName(classPizza)
      .withTenants(Tenant.builder().name(tenants[0].getName()).activityStatus(ActivityStatus.COLD).build())
      .run();
    assertThat(result).isNotNull()
      .returns(false, Result::hasErrors)
      .returns(true, Result::getResult);

    assertMT.tenantInactive(classPizza, tenants[0].getName());
    assertMT.tenantInactiveGetsNoObjects(classPizza, tenants[0].getName());
    assertMT.tenantActive(classPizza, tenants[1].getName());
    assertMT.tenantActiveGetsObjects(classPizza, tenants[1].getName(), pizzaSize);
    assertMT.tenantInactive(classPizza, tenants[2].getName());
    assertMT.tenantInactiveGetsNoObjects(classPizza, tenants[2].getName());

    // activate tenant (3)
    Result<Boolean> result2 = client.schema().tenantsUpdater()
      .withClassName(classPizza)
      .withTenants(Tenant.builder().name(tenants[2].getName()).activityStatus(ActivityStatus.HOT).build())
      .run();
    assertThat(result2).isNotNull()
      .returns(false, Result::hasErrors)
      .returns(true, Result::getResult);

    // populate active tenant (3)
    testGenerics.createDataPizzaForTenants(client, tenants[2].getName());

    assertMT.tenantInactive(classPizza, tenants[0].getName());
    assertMT.tenantInactiveGetsNoObjects(classPizza, tenants[0].getName());
    assertMT.tenantActive(classPizza, tenants[1].getName());
    assertMT.tenantActiveGetsObjects(classPizza, tenants[1].getName(), pizzaSize);
    assertMT.tenantActive(classPizza, tenants[2].getName());
    assertMT.tenantActiveGetsObjects(classPizza, tenants[2].getName(), pizzaSize);

    // activate tenant (1)
    Result<Boolean> result3 = client.schema().tenantsUpdater()
      .withClassName(classPizza)
      .withTenants(Tenant.builder().name(tenants[0].getName()).activityStatus(ActivityStatus.HOT).build())
      .run();
    assertThat(result3).isNotNull()
      .returns(false, Result::hasErrors)
      .returns(true, Result::getResult);

    assertMT.tenantActive(classPizza, tenants[0].getName());
    assertMT.tenantActiveGetsObjects(classPizza, tenants[0].getName(), pizzaSize);
    assertMT.tenantActive(classPizza, tenants[1].getName());
    assertMT.tenantActiveGetsObjects(classPizza, tenants[1].getName(), pizzaSize);
    assertMT.tenantActive(classPizza, tenants[2].getName());
    assertMT.tenantActiveGetsObjects(classPizza, tenants[2].getName(), pizzaSize);

    // deactivate tenant (2)
    Result<Boolean> result4 = client.schema().tenantsUpdater()
      .withClassName(classPizza)
      .withTenants(Tenant.builder().name(tenants[1].getName()).activityStatus(ActivityStatus.COLD).build())
      .run();
    assertThat(result4).isNotNull()
      .returns(false, Result::hasErrors)
      .returns(true, Result::getResult);

    assertMT.tenantActive(classPizza, tenants[0].getName());
    assertMT.tenantActiveGetsObjects(classPizza, tenants[0].getName(), pizzaSize);
    assertMT.tenantInactive(classPizza, tenants[1].getName());
    assertMT.tenantInactiveGetsNoObjects(classPizza, tenants[1].getName());
    assertMT.tenantActive(classPizza, tenants[2].getName());
    assertMT.tenantActiveGetsObjects(classPizza, tenants[2].getName(), pizzaSize);

    // delete tenants
    Result<Boolean> result5 = client.schema().tenantsDeleter()
      .withClassName(classPizza)
      .withTenants(Arrays.stream(tenants).map(Tenant::getName).toArray(String[]::new))
      .run();
    assertThat(result5).isNotNull()
      .returns(false, Result::hasErrors)
      .returns(true, Result::getResult);
  }
}
