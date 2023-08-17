package io.weaviate.integration.client;

import io.weaviate.client.WeaviateClient;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.WeaviateError;
import io.weaviate.client.base.WeaviateErrorMessage;
import io.weaviate.client.v1.data.model.WeaviateObject;
import io.weaviate.client.v1.schema.model.ActivityStatus;
import io.weaviate.client.v1.schema.model.Tenant;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.STRING;

public class AssertMultiTenancy {

  private WeaviateClient client;

  public AssertMultiTenancy(WeaviateClient client) {
    this.client = client;
  }

  public void objectExists(String className, String id, String tenant) {
    Result<Boolean> checkResult = client.data().checker()
      .withClassName(className)
      .withID(id)
      .withTenant(tenant)
      .run();

    assertThat(checkResult).isNotNull()
      .returns(false, Result::hasErrors)
      .returns(true, Result::getResult);
  }

  public void objectDoesNotExist(String className, String id, String tenant) {
    Result<Boolean> checkResult = client.data().checker()
      .withClassName(className)
      .withID(id)
      .withTenant(tenant)
      .run();

    assertThat(checkResult).isNotNull()
      .returns(false, Result::hasErrors)
      .returns(false, Result::getResult);
  }

  public void countObjects(String tenant, int expectedCount) {
    countObjects(null, tenant, expectedCount);
  }

  public void countObjects(String className, String tenant, int expectedCount) {
    Result<List<WeaviateObject>> getResultByClass = client.data().objectsGetter()
      .withTenant(tenant)
      .withClassName(className)
      .run();

    assertThat(getResultByClass).isNotNull()
      .returns(false, Result::hasErrors)
      .extracting(Result::getResult).asList()
      .hasSize(expectedCount);
  }

  public <T> void error(Result<T> result, T expectedValue, int expectedStatusCode, String... expectedContains) {
    assertThat(result).isNotNull()
      .returns(expectedValue, Result::getResult)
      .returns(true, Result::hasErrors)
      .extracting(Result::getError)
      .returns(expectedStatusCode, WeaviateError::getStatusCode)
      .extracting(WeaviateError::getMessages).asList()
      .first()
      .extracting(m -> ((WeaviateErrorMessage) m).getMessage()).asInstanceOf(STRING)
      .contains(expectedContains);
  }

  public void tenantActive(String className, String tenantName) {
    tenantStatus(className, tenantName, ActivityStatus.HOT);
  }

  public void tenantInactive(String className, String tenantName) {
    tenantStatus(className, tenantName, ActivityStatus.COLD);
  }

  private void tenantStatus(String className, String tenantName, String activityStatus) {
    Result<List<Tenant>> result = client.schema().tenantsGetter()
      .withClassName(className)
      .run();

    assertThat(result).isNotNull()
      .returns(false, Result::hasErrors)
      .extracting(Result::getResult).isNotNull();

    Optional<Tenant> maybeTenant = result.getResult().stream()
      .filter(Objects::nonNull)
      .filter(tenant -> tenantName.equals(tenant.getName()))
      .filter(tenant -> activityStatus.equals(tenant.getActivityStatus()))
      .findFirst();

    assertThat(maybeTenant).isNotEmpty();
  }

  public void tenantActiveGetsObjects(String className, String tenantName, int objectsCount) {
    Result<List<WeaviateObject>> result = client.data().objectsGetter()
      .withClassName(className)
      .withTenant(tenantName)
      .run();

    assertThat(result).isNotNull()
      .returns(false, Result::hasErrors)
      .extracting(Result::getResult).asList()
      .hasSize(objectsCount);
  }

  public void tenantInactiveGetsNoObjects(String className, String tenantName) {
    Result<List<WeaviateObject>> result = client.data().objectsGetter()
      .withClassName(className)
      .withTenant(tenantName)
      .run();

    error(result, null, 422, "tenant not active");
  }
}
