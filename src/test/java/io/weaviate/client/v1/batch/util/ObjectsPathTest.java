package io.weaviate.client.v1.batch.util;

import com.jparams.junit4.JParamsTestRunner;
import com.jparams.junit4.data.DataMethod;
import org.junit.Test;
import org.junit.runner.RunWith;
import io.weaviate.client.v1.data.replication.model.ConsistencyLevel;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(JParamsTestRunner.class)
public class ObjectsPathTest {

  private final ObjectsPath objectsPath = new ObjectsPath();

  private static final ObjectsPath.Params EMPTY_PARAMS = ObjectsPath.Params.builder()
    .build();
  private static final ObjectsPath.Params ALL_PARAMS = ObjectsPath.Params.builder()
    .consistencyLevel(ConsistencyLevel.QUORUM)
    .build();


  @Test
  @DataMethod(source = ObjectsPathTest.class, method = "provideCreate")
  public void shouldBuildCreatePaths(ObjectsPath.Params params, String expectedPath) {
    assertThat(objectsPath.buildCreate(params)).isEqualTo(expectedPath);
  }

  public static Object[][] provideCreate() {
    return new Object[][]{
      {
        EMPTY_PARAMS,
        "/batch/objects"
      },
      {
        ALL_PARAMS,
        "/batch/objects?consistency_level=QUORUM"
      },
    };
  }

  @Test
  @DataMethod(source = ObjectsPathTest.class, method = "provideDelete")
  public void shouldBuildDeletePaths(ObjectsPath.Params params, String expectedPath) {
    assertThat(objectsPath.buildDelete(params)).isEqualTo(expectedPath);
  }

  public static Object[][] provideDelete() {
    return new Object[][]{
      {
        EMPTY_PARAMS,
        "/batch/objects"
      },
      {
        ALL_PARAMS,
        "/batch/objects?consistency_level=QUORUM"
      },
    };
  }
}
