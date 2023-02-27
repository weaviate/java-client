package technology.semi.weaviate.client.v1.data.util;

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
import technology.semi.weaviate.client.base.util.DbVersionSupport;
import technology.semi.weaviate.client.v1.data.replication.model.ConsistencyLevel;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(JParamsTestRunner.class)
public class ReferencesPathTest {

  private AutoCloseable openedMocks;
  @InjectMocks
  private ReferencesPath referencesPath;
  @Mock
  private DbVersionSupport dbVersionSupportMock;

  private static final ReferencesPath.Params EMPTY_PARAMS = ReferencesPath.Params.builder()
    .build();
  private static final ReferencesPath.Params CLASS_PARAMS = ReferencesPath.Params.builder()
    .className("someClass")
    .build();
  private static final ReferencesPath.Params ID_PARAMS = ReferencesPath.Params.builder()
    .id("someId")
    .build();
  private static final ReferencesPath.Params PROPERTY_PARAMS = ReferencesPath.Params.builder()
    .property("someProperty")
    .build();
  private static final ReferencesPath.Params ALL_PARAMS = ReferencesPath.Params.builder()
    .className("someClass")
    .id("someId")
    .property("someProperty")
    .consistencyLevel(ConsistencyLevel.QUORUM)
    .build();


  @Before
  public void setUp() {
    openedMocks = MockitoAnnotations.openMocks(this);
  }
  @After
  public void tearDown() throws Exception {
    openedMocks.close();
  }


  @Test
  @DataMethod(source = ReferencesPathTest.class, method = "provideCreateForSupported")
  public void shouldBuildCreatePathsWhenSupported(ReferencesPath.Params params, String expectedPath) {
    Mockito.when(dbVersionSupportMock.supportsClassNameNamespacedEndpoints()).thenReturn(true);

    assertThat(referencesPath.buildCreate(params)).isEqualTo(expectedPath);
  }

  public static Object[][] provideCreateForSupported() {
    return new Object[][]{
      {
        EMPTY_PARAMS,
        "/objects/references"
      },
      {
        CLASS_PARAMS,
        "/objects/someClass/references"
      },
      {
        ID_PARAMS,
        "/objects/someId/references"
      },
      {
        PROPERTY_PARAMS,
        "/objects/references/someProperty"
      },
      {
        ALL_PARAMS,
        "/objects/someClass/someId/references/someProperty?consistency_level=QUORUM"
      },
    };
  }

  @Test
  @DataMethod(source = ReferencesPathTest.class, method = "provideCreateForNotSupported")
  public void shouldBuildCreatePathsWhenNotSupported(ReferencesPath.Params params, String expectedPath) {
    Mockito.when(dbVersionSupportMock.supportsClassNameNamespacedEndpoints()).thenReturn(false);

    assertThat(referencesPath.buildCreate(params)).isEqualTo(expectedPath);
  }

  public static Object[][] provideCreateForNotSupported() {
    return new Object[][]{
      {
        EMPTY_PARAMS,
        "/objects/references"
      },
      {
        CLASS_PARAMS,
        "/objects/references"
      },
      {
        ID_PARAMS,
        "/objects/someId/references"
      },
      {
        PROPERTY_PARAMS,
        "/objects/references/someProperty"
      },
      {
        ALL_PARAMS,
        "/objects/someId/references/someProperty?consistency_level=QUORUM"
      },
    };
  }

  @Test
  @DataMethod(source = ReferencesPathTest.class, method = "provideDeleteForSupported")
  public void shouldBuildDeletePathsWhenSupported(ReferencesPath.Params params, String expectedPath) {
    Mockito.when(dbVersionSupportMock.supportsClassNameNamespacedEndpoints()).thenReturn(true);

    assertThat(referencesPath.buildDelete(params)).isEqualTo(expectedPath);
  }

  public static Object[][] provideDeleteForSupported() {
    return new Object[][]{
      {
        EMPTY_PARAMS,
        "/objects/references"
      },
      {
        CLASS_PARAMS,
        "/objects/someClass/references"
      },
      {
        ID_PARAMS,
        "/objects/someId/references"
      },
      {
        PROPERTY_PARAMS,
        "/objects/references/someProperty"
      },
      {
        ALL_PARAMS,
        "/objects/someClass/someId/references/someProperty?consistency_level=QUORUM"
      },
    };
  }

  @Test
  @DataMethod(source = ReferencesPathTest.class, method = "provideDeleteForNotSupported")
  public void shouldBuildDeletePathsWhenNotSupported(ReferencesPath.Params params, String expectedPath) {
    Mockito.when(dbVersionSupportMock.supportsClassNameNamespacedEndpoints()).thenReturn(false);

    assertThat(referencesPath.buildDelete(params)).isEqualTo(expectedPath);
  }

  public static Object[][] provideDeleteForNotSupported() {
    return new Object[][]{
      {
        EMPTY_PARAMS,
        "/objects/references"
      },
      {
        CLASS_PARAMS,
        "/objects/references"
      },
      {
        ID_PARAMS,
        "/objects/someId/references"
      },
      {
        PROPERTY_PARAMS,
        "/objects/references/someProperty"
      },
      {
        ALL_PARAMS,
        "/objects/someId/references/someProperty?consistency_level=QUORUM"
      },
    };
  }

  @Test
  @DataMethod(source = ReferencesPathTest.class, method = "provideReplaceForSupported")
  public void shouldBuildReplacePathsWhenSupported(ReferencesPath.Params params, String expectedPath) {
    Mockito.when(dbVersionSupportMock.supportsClassNameNamespacedEndpoints()).thenReturn(true);

    assertThat(referencesPath.buildReplace(params)).isEqualTo(expectedPath);
  }

  public static Object[][] provideReplaceForSupported() {
    return new Object[][]{
      {
        EMPTY_PARAMS,
        "/objects/references"
      },
      {
        CLASS_PARAMS,
        "/objects/someClass/references"
      },
      {
        ID_PARAMS,
        "/objects/someId/references"
      },
      {
        PROPERTY_PARAMS,
        "/objects/references/someProperty"
      },
      {
        ALL_PARAMS,
        "/objects/someClass/someId/references/someProperty?consistency_level=QUORUM"
      },
    };
  }

  @Test
  @DataMethod(source = ReferencesPathTest.class, method = "provideReplaceForNotSupported")
  public void shouldBuildReplacePathsWhenNotSupported(ReferencesPath.Params params, String expectedPath) {
    Mockito.when(dbVersionSupportMock.supportsClassNameNamespacedEndpoints()).thenReturn(false);

    assertThat(referencesPath.buildReplace(params)).isEqualTo(expectedPath);
  }

  public static Object[][] provideReplaceForNotSupported() {
    return new Object[][]{
      {
        EMPTY_PARAMS,
        "/objects/references"
      },
      {
        CLASS_PARAMS,
        "/objects/references"
      },
      {
        ID_PARAMS,
        "/objects/someId/references"
      },
      {
        PROPERTY_PARAMS,
        "/objects/references/someProperty"
      },
      {
        ALL_PARAMS,
        "/objects/someId/references/someProperty?consistency_level=QUORUM"
      },
    };
  }
}
