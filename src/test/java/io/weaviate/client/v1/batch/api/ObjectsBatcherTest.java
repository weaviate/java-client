package io.weaviate.client.v1.batch.api;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import com.github.dockerjava.zerodep.shaded.org.apache.hc.core5.http.HttpStatus;

import io.weaviate.client.base.Result;
import io.weaviate.client.base.WeaviateError;
import io.weaviate.client.base.WeaviateErrorMessage;
import io.weaviate.client.grpc.protocol.v1.WeaviateProtoBatch;
import io.weaviate.client.v1.batch.model.ObjectGetResponse;
import io.weaviate.client.v1.data.model.WeaviateObject;

public class ObjectsBatcherTest {
  @Test
  public void test_resultFromBatchObjectsReply() {
    // Arrange
    List<WeaviateObject> batch = Arrays.asList(
        WeaviateObject.builder().id("id-0").build(),
        WeaviateObject.builder().id("id-1").build(),
        WeaviateObject.builder().id("id-2").build());
    WeaviateProtoBatch.BatchObjectsReply reply = WeaviateProtoBatch.BatchObjectsReply.newBuilder()
        .addAllErrors(Arrays.asList(
            WeaviateProtoBatch.BatchObjectsReply.BatchError.newBuilder()
                .setIndex(0).setError("error-0")
                .build(),
            WeaviateProtoBatch.BatchObjectsReply.BatchError.newBuilder()
                .setIndex(1).setError("error-1")
                .build()))
        .build();

    // Act
    Result<ObjectGetResponse[]> got = ObjectsBatcher.resultFromBatchObjectsReply(reply, batch);

    // Assert
    List<ObjectGetResponse> succeeded = Arrays.stream(got.getResult())
        .filter(result -> result.getResult().getErrors() == null)
        .collect(Collectors.toList());
    List<ObjectGetResponse> failed = Arrays.stream(got.getResult())
        .filter(result -> result.getResult().getErrors() != null)
        .collect(Collectors.toList());
    Assertions.assertThat(got.getResult()).hasSize(3);
    Assertions.assertThat(succeeded).hasSize(1);
    Assertions.assertThat(failed).hasSize(2);

    Assertions.assertThat(got.getError()).returns(HttpStatus.SC_UNPROCESSABLE_ENTITY, WeaviateError::getStatusCode);
    Assertions.assertThat(got.getError().getMessages())
        .hasSize(2)
        .extracting(WeaviateErrorMessage::getMessage)
        .contains("error-0", "error-1");

  }
}
