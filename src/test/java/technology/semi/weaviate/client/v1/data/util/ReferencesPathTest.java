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

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(JParamsTestRunner.class)
public class ReferencesPathTest {

  private AutoCloseable openedMocks;
  @InjectMocks
  private ReferencesPath referencesPath;
  @Mock
  private DbVersionSupport dbVersionSupportMock;

  private static final ReferencesPath.Params EMPTY_PARAMS = ReferencesPath.Params.builder().build();
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
  @DataMethod(source = ReferencesPathTest.class, method = "provideForSupported")
  public void shouldBuildPathsWhenSupported(ReferencesPath.Params pathParams, String expectedPath) {
    Mockito.when(dbVersionSupportMock.supportsClassNameNamespacedEndpoints()).thenReturn(true);

    assertThat(referencesPath.build(pathParams)).isEqualTo(expectedPath);
  }

  public static Object[][] provideForSupported() {
    return new Object[][]{
      {
        EMPTY_PARAMS,   // TODO should be valid?
        "/objects/references"
      },
      {
        CLASS_PARAMS,   // TODO should be valid?
        "/objects/someClass/references"
      },
      {
        ID_PARAMS,      // TODO should be valid?
        "/objects/someId/references"
      },
      {
        PROPERTY_PARAMS,    // TODO should be valid?
        "/objects/references/someProperty"
      },
      {
        ALL_PARAMS,
        "/objects/someClass/someId/references/someProperty"
      },
    };
  }

  @Test
  @DataMethod(source = ReferencesPathTest.class, method = "provideForNotSupported")
  public void shouldBuildPathsWhenNotSupported(ReferencesPath.Params pathParams, String expectedPath) {
    Mockito.when(dbVersionSupportMock.supportsClassNameNamespacedEndpoints()).thenReturn(false);

    assertThat(referencesPath.build(pathParams)).isEqualTo(expectedPath);
  }

  public static Object[][] provideForNotSupported() {
    return new Object[][]{
      {
        EMPTY_PARAMS,   // TODO should be valid?
        "/objects/references"
      },
      {
        CLASS_PARAMS,   // TODO should be valid?
        "/objects/references"
      },
      {
        ID_PARAMS,      // TODO should be valid?
        "/objects/someId/references"
      },
      {
        PROPERTY_PARAMS,    // TODO should be valid?
        "/objects/references/someProperty"
      },
      {
        ALL_PARAMS,
        "/objects/someId/references/someProperty"
      },
    };
  }
}
