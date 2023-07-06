package io.weaviate.integration.client;

import io.weaviate.client.WeaviateClient;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.WeaviateError;
import io.weaviate.client.base.WeaviateErrorMessage;
import io.weaviate.client.v1.data.model.WeaviateObject;

import java.util.List;

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
}
