package technology.semi.weaviate.client.base.util;

import com.jparams.junit4.JParamsTestRunner;
import com.jparams.junit4.data.DataMethod;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(JParamsTestRunner.class)
public class BeaconPathTest {

  private static final BeaconPath.Params EMPTY_PARAMS = BeaconPath.Params.builder().build();
  private static final BeaconPath.Params CLASS_PARAMS = BeaconPath.Params.builder()
    .className("someClass")
    .build();
  private static final BeaconPath.Params ID_PARAMS = BeaconPath.Params.builder()
    .id("someId")
    .build();
  private static final BeaconPath.Params PROPERTY_PARAMS = BeaconPath.Params.builder()
    .property("someProperty")
    .build();
  private static final BeaconPath.Params ALL_PARAMS = BeaconPath.Params.builder()
    .className("someClass")
    .id("someId")
    .property("someProperty")
    .build();
  private AutoCloseable openedMocks;
  @InjectMocks
  private BeaconPath beaconPath;
  @Mock
  private DbVersionSupport dbVersionSupportMock;

  public static Object[][] provideForSingleSupported() {
    return new Object[][]{
      {
        EMPTY_PARAMS,   // TODO should be valid?
        "weaviate://localhost"
      },
      {
        CLASS_PARAMS,   // TODO should be valid?
        "weaviate://localhost/someClass"
      },
      {
        ID_PARAMS,      // TODO should be valid?
        "weaviate://localhost/someId"
      },
      {
        PROPERTY_PARAMS,    // TODO should be valid?
        "weaviate://localhost"
      },
      {
        ALL_PARAMS,
        "weaviate://localhost/someClass/someId"
      },
    };
  }

  public static Object[][] provideSingleForNotSupported() {
    return new Object[][]{
      {
        EMPTY_PARAMS,   // TODO should be valid?
        "weaviate://localhost"
      },
      {
        CLASS_PARAMS,   // TODO should be valid?
        "weaviate://localhost"
      },
      {
        ID_PARAMS,      // TODO should be valid?
        "weaviate://localhost/someId"
      },
      {
        PROPERTY_PARAMS,    // TODO should be valid?
        "weaviate://localhost"
      },
      {
        ALL_PARAMS,
        "weaviate://localhost/someId"
      },
    };
  }

  public static Object[][] provideForBatchFromSupported() {
    return new Object[][]{
      {
        EMPTY_PARAMS,   // TODO should be valid?
        "weaviate://localhost"
      },
      {
        CLASS_PARAMS,   // TODO should be valid?
        "weaviate://localhost/someClass"
      },
      {
        ID_PARAMS,      // TODO should be valid?
        "weaviate://localhost/someId"
      },
      {
        PROPERTY_PARAMS,    // TODO should be valid?
        "weaviate://localhost/someProperty"
      },
      {
        ALL_PARAMS,
        "weaviate://localhost/someClass/someId/someProperty"
      },
    };
  }

  public static Object[][] provideBatchFromForNotSupported() {
    return new Object[][]{
      {
        EMPTY_PARAMS,   // TODO should be valid?
        "weaviate://localhost"
      },
      {
        CLASS_PARAMS,   // TODO should be valid?
        "weaviate://localhost/someClass"
      },
      {
        ID_PARAMS,      // TODO should be valid?
        "weaviate://localhost/someId"
      },
      {
        PROPERTY_PARAMS,    // TODO should be valid?
        "weaviate://localhost/someProperty"
      },
      {
        ALL_PARAMS,
        "weaviate://localhost/someClass/someId/someProperty"
      },
    };
  }

  public static Object[][] provideForBatchToSupported() {
    return new Object[][]{
      {
        EMPTY_PARAMS,   // TODO should be valid?
        "weaviate://localhost"
      },
      {
        CLASS_PARAMS,   // TODO should be valid?
        "weaviate://localhost/someClass"
      },
      {
        ID_PARAMS,      // TODO should be valid?
        "weaviate://localhost/someId"
      },
      {
        PROPERTY_PARAMS,    // TODO should be valid?
        "weaviate://localhost"
      },
      {
        ALL_PARAMS,
        "weaviate://localhost/someClass/someId"
      },
    };
  }

  public static Object[][] provideBatchToForNotSupported() {
    return new Object[][]{
      {
        EMPTY_PARAMS,   // TODO should be valid?
        "weaviate://localhost"
      },
      {
        CLASS_PARAMS,   // TODO should be valid?
        "weaviate://localhost"
      },
      {
        ID_PARAMS,      // TODO should be valid?
        "weaviate://localhost/someId"
      },
      {
        PROPERTY_PARAMS,    // TODO should be valid?
        "weaviate://localhost"
      },
      {
        ALL_PARAMS,
        "weaviate://localhost/someId"
      },
    };
  }

  @Before
  public void setUp() {
    openedMocks = MockitoAnnotations.openMocks(this);
  }

  @After
  public void tearDown() throws Exception {
    openedMocks.close();
  }

  @Test
  @DataMethod(source = BeaconPathTest.class, method = "provideForSingleSupported")
  public void shouldBuildSinglePathsWhenSupported(BeaconPath.Params pathParams, String expectedPath) {
    Mockito.when(dbVersionSupportMock.supportsClassNameNamespacedEndpoints()).thenReturn(true);

    assertThat(beaconPath.buildSingle(pathParams)).isEqualTo(expectedPath);
  }

  @Test
  @DataMethod(source = BeaconPathTest.class, method = "provideSingleForNotSupported")
  public void shouldBuildSinglePathsWhenNotSupported(BeaconPath.Params pathParams, String expectedPath) {
    Mockito.when(dbVersionSupportMock.supportsClassNameNamespacedEndpoints()).thenReturn(false);

    assertThat(beaconPath.buildSingle(pathParams)).isEqualTo(expectedPath);
  }

  @Test
  @DataMethod(source = BeaconPathTest.class, method = "provideForBatchFromSupported")
  public void shouldBuildBatchFromPathsWhenSupported(BeaconPath.Params pathParams, String expectedPath) {
    Mockito.when(dbVersionSupportMock.supportsClassNameNamespacedEndpoints()).thenReturn(true);

    assertThat(beaconPath.buildBatchFrom(pathParams)).isEqualTo(expectedPath);
  }

  @Test
  @DataMethod(source = BeaconPathTest.class, method = "provideBatchFromForNotSupported")
  public void shouldBuildBatchFromPathsWhenNotSupported(BeaconPath.Params pathParams, String expectedPath) {
    Mockito.when(dbVersionSupportMock.supportsClassNameNamespacedEndpoints()).thenReturn(false);

    assertThat(beaconPath.buildBatchFrom(pathParams)).isEqualTo(expectedPath);
  }

  @Test
  @DataMethod(source = BeaconPathTest.class, method = "provideForBatchToSupported")
  public void shouldBuildBatchToPathsWhenSupported(BeaconPath.Params pathParams, String expectedPath) {
    Mockito.when(dbVersionSupportMock.supportsClassNameNamespacedEndpoints()).thenReturn(true);

    assertThat(beaconPath.buildBatchTo(pathParams)).isEqualTo(expectedPath);
  }

  @Test
  @DataMethod(source = BeaconPathTest.class, method = "provideBatchToForNotSupported")
  public void shouldBuildBatchToPathsWhenNotSupported(BeaconPath.Params pathParams, String expectedPath) {
    Mockito.when(dbVersionSupportMock.supportsClassNameNamespacedEndpoints()).thenReturn(false);

    assertThat(beaconPath.buildBatchTo(pathParams)).isEqualTo(expectedPath);
  }
}
