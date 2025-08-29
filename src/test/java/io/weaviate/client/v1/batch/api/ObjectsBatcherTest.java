package io.weaviate.client.v1.batch.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.http.HttpStatus;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.jparams.junit4.JParamsTestRunner;
import com.jparams.junit4.data.DataMethod;

import io.weaviate.client.base.Result;
import io.weaviate.client.base.WeaviateError;
import io.weaviate.client.base.WeaviateErrorMessage;
import io.weaviate.client.grpc.protocol.v1.WeaviateProtoBatch;
import io.weaviate.client.v1.batch.model.ObjectGetResponse;
import io.weaviate.client.v1.data.model.WeaviateObject;

@RunWith(JParamsTestRunner.class)
public class ObjectsBatcherTest {
  public static Object[][] batchReplyTestCases() {
    return new Object[][] {
        { 1, 2 },
        { 0, 3 },
        { 3, 0 },
        { 2, 2 },
    };
  }

  @DataMethod(source = ObjectsBatcherTest.class, method = "batchReplyTestCases")
  @Test
  public void test_resultFromBatchObjectsReply(int wantSucceed, int wantFail) {
    // Arrange
    List<WeaviateObject> batch = new ArrayList<>();
    int total = wantSucceed + wantFail;
    for (int i = 0; i < total; i++) {
      batch.add(WeaviateObject.builder().id("id-" + i).build());
    }

    WeaviateProtoBatch.BatchObjectsReply.Builder reply = WeaviateProtoBatch.BatchObjectsReply.newBuilder();
    for (int i = 0; i < wantFail; i++) {
      reply.addErrors(WeaviateProtoBatch.BatchObjectsReply.BatchError.newBuilder()
          .setIndex(i).setError("error-" + i)
          .build());
    }

    Result<ObjectGetResponse[]> got = ObjectsBatcher.resultFromBatchObjectsReply(reply.build(), batch);

    // Assert
    List<ObjectGetResponse> succeeded = Arrays.stream(got.getResult())
        .filter(result -> result.getResult().getErrors() == null)
        .collect(Collectors.toList());
    List<ObjectGetResponse> failed = Arrays.stream(got.getResult())
        .filter(result -> result.getResult().getErrors() != null)
        .collect(Collectors.toList());
    Assertions.assertThat(got.getResult()).hasSize(total);
    Assertions.assertThat(failed).hasSize(wantFail);
    Assertions.assertThat(succeeded).hasSize(wantSucceed);

    if (wantFail == 0) {
      Assertions.assertThat(got.getError()).isNull();
      return;
    }

    String[] wantErrors = new String[wantFail];
    for (int i = 0; i < failed.size(); i++) {
      wantErrors[i] = failed.get(i).getResult().getErrors().getError().get(0).getMessage();
    }
    Assertions.assertThat(got.getError()).returns(HttpStatus.SC_UNPROCESSABLE_ENTITY, WeaviateError::getStatusCode);
    Assertions.assertThat(got.getError().getMessages())
        .hasSize(wantFail)
        .extracting(WeaviateErrorMessage::getMessage)
        .contains(wantErrors);

  }
}
