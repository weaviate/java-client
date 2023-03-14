package io.weaviate.client.v1.batch.util;

import com.jparams.junit4.JParamsTestRunner;
import com.jparams.junit4.data.DataMethod;
import org.junit.Test;
import org.junit.runner.RunWith;
import io.weaviate.client.v1.data.replication.model.ConsistencyLevel;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(JParamsTestRunner.class)
public class ReferencesPathTest {

  private final ReferencesPath referencesPath = new ReferencesPath();

  private static final ReferencesPath.Params EMPTY_PARAMS = ReferencesPath.Params.builder()
    .build();
  private static final ReferencesPath.Params ALL_PARAMS = ReferencesPath.Params.builder()
    .consistencyLevel(ConsistencyLevel.QUORUM)
    .build();


  @Test
  @DataMethod(source = ReferencesPathTest.class, method = "provideCreate")
  public void shouldBuildCreatePaths(ReferencesPath.Params params, String expectedPath) {
    assertThat(referencesPath.buildCreate(params)).isEqualTo(expectedPath);
  }

  public static Object[][] provideCreate() {
    return new Object[][]{
      {
        EMPTY_PARAMS,
        "/batch/references"
      },
      {
        ALL_PARAMS,
        "/batch/references?consistency_level=QUORUM"
      },
    };
  }
}
