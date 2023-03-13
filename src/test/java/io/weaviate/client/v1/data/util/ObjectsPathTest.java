package io.weaviate.client.v1.data.util;

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
import io.weaviate.client.base.util.DbVersionSupport;
import io.weaviate.client.v1.data.replication.model.ConsistencyLevel;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(JParamsTestRunner.class)
public class ObjectsPathTest {

  private AutoCloseable openedMocks;
  @InjectMocks
  private ObjectsPath objectsPath;
  @Mock
  private DbVersionSupport dbVersionSupportMock;

  private static final ObjectsPath.Params EMPTY_PARAMS = ObjectsPath.Params.builder()
    .build();
  private static final ObjectsPath.Params CLASS_PARAMS = ObjectsPath.Params.builder()
    .className("someClass")
    .build();
  private static final ObjectsPath.Params ID_PARAMS = ObjectsPath.Params.builder()
    .id("someId")
    .build();
  private static final ObjectsPath.Params SOME_PARAMS = ObjectsPath.Params.builder()
    .className("someClass")
    .consistencyLevel(ConsistencyLevel.QUORUM)
    .limit(10)
    .build();
  private static final ObjectsPath.Params ALL_PARAMS = ObjectsPath.Params.builder()
    .className("someClass")
    .id("someId")
    .limit(100)
    .offset(0)
    .after("00000000-0000-0000-0000-000000000000")
    .additional(new String[]{"additional1", "additional2"})
    .consistencyLevel(ConsistencyLevel.QUORUM)
    .nodeName("node1")
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
  @DataMethod(source = ObjectsPathTest.class, method = "provideCreateForSupported")
  public void shouldBuildCreatePathsWhenSupported(ObjectsPath.Params params, String expectedPath) {
    Mockito.when(dbVersionSupportMock.supportsClassNameNamespacedEndpoints()).thenReturn(true);

    assertThat(objectsPath.buildCreate(params)).isEqualTo(expectedPath);
  }

  public static Object[][] provideCreateForSupported() {
    return new Object[][]{
      {
        EMPTY_PARAMS,
        "/objects"
      },
      {
        CLASS_PARAMS,
        "/objects"
      },
      {
        ID_PARAMS,
        "/objects"
      },
      {
        SOME_PARAMS,
        "/objects?consistency_level=QUORUM"
      },
      {
        ALL_PARAMS,
        "/objects?consistency_level=QUORUM"
      },
    };
  }

  @Test
  @DataMethod(source = ObjectsPathTest.class, method = "provideCreateForNotSupported")
  public void shouldBuildCreatePathsWhenNotSupported(ObjectsPath.Params params, String expectedPath) {
    Mockito.when(dbVersionSupportMock.supportsClassNameNamespacedEndpoints()).thenReturn(false);

    assertThat(objectsPath.buildCreate(params)).isEqualTo(expectedPath);
  }

  public static Object[][] provideCreateForNotSupported() {
    return new Object[][]{
      {
        EMPTY_PARAMS,
        "/objects"
      },
      {
        CLASS_PARAMS,
        "/objects"
      },
      {
        ID_PARAMS,
        "/objects"
      },
      {
        SOME_PARAMS,
        "/objects?consistency_level=QUORUM"
      },
      {
        ALL_PARAMS,
        "/objects?consistency_level=QUORUM"
      },
    };
  }

  @Test
  @DataMethod(source = ObjectsPathTest.class, method = "provideDeleteForSupported")
  public void shouldBuildDeletePathsWhenSupported(ObjectsPath.Params params, String expectedPath) {
    Mockito.when(dbVersionSupportMock.supportsClassNameNamespacedEndpoints()).thenReturn(true);

    assertThat(objectsPath.buildDelete(params)).isEqualTo(expectedPath);
  }

  public static Object[][] provideDeleteForSupported() {
    return new Object[][]{
      {
        EMPTY_PARAMS,
        "/objects"
      },
      {
        CLASS_PARAMS,
        "/objects/someClass"
      },
      {
        ID_PARAMS,
        "/objects/someId"
      },
      {
        SOME_PARAMS,
        "/objects/someClass?consistency_level=QUORUM"
      },
      {
        ALL_PARAMS,
        "/objects/someClass/someId?consistency_level=QUORUM"
      },
    };
  }

  @Test
  @DataMethod(source = ObjectsPathTest.class, method = "provideDeleteForNotSupported")
  public void shouldBuildDeletePathsWhenNotSupported(ObjectsPath.Params params, String expectedPath) {
    Mockito.when(dbVersionSupportMock.supportsClassNameNamespacedEndpoints()).thenReturn(false);

    assertThat(objectsPath.buildDelete(params)).isEqualTo(expectedPath);
  }

  public static Object[][] provideDeleteForNotSupported() {
    return new Object[][]{
      {
        EMPTY_PARAMS,
        "/objects"
      },
      {
        CLASS_PARAMS,
        "/objects"
      },
      {
        ID_PARAMS,
        "/objects/someId"
      },
      {
        SOME_PARAMS,
        "/objects?consistency_level=QUORUM"
      },
      {
        ALL_PARAMS,
        "/objects/someId?consistency_level=QUORUM"
      },
    };
  }

  @Test
  @DataMethod(source = ObjectsPathTest.class, method = "provideCheckForSupported")
  public void shouldBuildCheckPathsWhenSupported(ObjectsPath.Params params, String expectedPath) {
    Mockito.when(dbVersionSupportMock.supportsClassNameNamespacedEndpoints()).thenReturn(true);

    assertThat(objectsPath.buildCheck(params)).isEqualTo(expectedPath);
  }

  public static Object[][] provideCheckForSupported() {
    return new Object[][]{
      {
        EMPTY_PARAMS,
        "/objects"
      },
      {
        CLASS_PARAMS,
        "/objects/someClass"
      },
      {
        ID_PARAMS,
        "/objects/someId"
      },
      {
        SOME_PARAMS,
        "/objects/someClass"
      },
      {
        ALL_PARAMS,
        "/objects/someClass/someId"
      },
    };
  }

  @Test
  @DataMethod(source = ObjectsPathTest.class, method = "provideCheckForNotSupported")
  public void shouldBuildCheckPathsWhenNotSupported(ObjectsPath.Params params, String expectedPath) {
    Mockito.when(dbVersionSupportMock.supportsClassNameNamespacedEndpoints()).thenReturn(false);

    assertThat(objectsPath.buildCheck(params)).isEqualTo(expectedPath);
  }

  public static Object[][] provideCheckForNotSupported() {
    return new Object[][]{
      {
        EMPTY_PARAMS,
        "/objects"
      },
      {
        CLASS_PARAMS,
        "/objects"
      },
      {
        ID_PARAMS,
        "/objects/someId"
      },
      {
        SOME_PARAMS,
        "/objects"
      },
      {
        ALL_PARAMS,
        "/objects/someId"
      },
    };
  }

  @Test
  @DataMethod(source = ObjectsPathTest.class, method = "provideGetOneForSupported")
  public void shouldBuildGetOnePathsWhenSupported(ObjectsPath.Params params, String expectedPath) {
    Mockito.when(dbVersionSupportMock.supportsClassNameNamespacedEndpoints()).thenReturn(true);

    assertThat(objectsPath.buildGetOne(params)).isEqualTo(expectedPath);
  }

  public static Object[][] provideGetOneForSupported() {
    return new Object[][]{
      {
        EMPTY_PARAMS,
        "/objects"
      },
      {
        CLASS_PARAMS,
        "/objects/someClass"
      },
      {
        ID_PARAMS,
        "/objects/someId"
      },
      {
        SOME_PARAMS,
        "/objects/someClass?consistency_level=QUORUM"
      },
      {
        ALL_PARAMS,
        "/objects/someClass/someId?include=additional1,additional2&consistency_level=QUORUM&node_name=node1"
      },
    };
  }

  @Test
  @DataMethod(source = ObjectsPathTest.class, method = "provideGetOneForNotSupported")
  public void shouldBuildGetOnePathsWhenNotSupported(ObjectsPath.Params params, String expectedPath) {
    Mockito.when(dbVersionSupportMock.supportsClassNameNamespacedEndpoints()).thenReturn(false);

    assertThat(objectsPath.buildGetOne(params)).isEqualTo(expectedPath);
  }

  public static Object[][] provideGetOneForNotSupported() {
    return new Object[][]{
      {
        EMPTY_PARAMS,
        "/objects"
      },
      {
        CLASS_PARAMS,
        "/objects"
      },
      {
        ID_PARAMS,
        "/objects/someId"
      },
      {
        SOME_PARAMS,
        "/objects?consistency_level=QUORUM"
      },
      {
        ALL_PARAMS,
        "/objects/someId?include=additional1,additional2&consistency_level=QUORUM&node_name=node1"
      },
    };
  }

  @Test
  @DataMethod(source = ObjectsPathTest.class, method = "provideGetForSupported")
  public void shouldBuildGetPathsWhenSupported(ObjectsPath.Params params, String expectedPath) {
    Mockito.when(dbVersionSupportMock.supportsClassNameNamespacedEndpoints()).thenReturn(true);

    assertThat(objectsPath.buildGet(params)).isEqualTo(expectedPath);
  }

  public static Object[][] provideGetForSupported() {
    return new Object[][]{
      {
        EMPTY_PARAMS,
        "/objects"
      },
      {
        CLASS_PARAMS,
        "/objects?class=someClass"
      },
      {
        ID_PARAMS,
        "/objects"
      },
      {
        SOME_PARAMS,
        "/objects?class=someClass&limit=10"
      },
      {
        ALL_PARAMS,
        "/objects?include=additional1,additional2&limit=100&offset=0&after=00000000-0000-0000-0000-000000000000"
      },
    };
  }

  @Test
  @DataMethod(source = ObjectsPathTest.class, method = "provideGetForNotSupported")
  public void shouldBuildGetPathsWhenNotSupported(ObjectsPath.Params params, String expectedPath) {
    Mockito.when(dbVersionSupportMock.supportsClassNameNamespacedEndpoints()).thenReturn(false);

    assertThat(objectsPath.buildGet(params)).isEqualTo(expectedPath);
  }

  public static Object[][] provideGetForNotSupported() {
    return new Object[][]{
      {
        EMPTY_PARAMS,
        "/objects"
      },
      {
        CLASS_PARAMS,
        "/objects"
      },
      {
        ID_PARAMS,
        "/objects"
      },
      {
        SOME_PARAMS,
        "/objects?limit=10"
      },
      {
        ALL_PARAMS,
        "/objects?include=additional1,additional2&limit=100&offset=0&after=00000000-0000-0000-0000-000000000000"
      },
    };
  }

  @Test
  @DataMethod(source = ObjectsPathTest.class, method = "provideUpdateForSupported")
  public void shouldBuildUpdatePathsWhenSupported(ObjectsPath.Params params, String expectedPath) {
    Mockito.when(dbVersionSupportMock.supportsClassNameNamespacedEndpoints()).thenReturn(true);

    assertThat(objectsPath.buildUpdate(params)).isEqualTo(expectedPath);
  }

  public static Object[][] provideUpdateForSupported() {
    return new Object[][]{
      {
        EMPTY_PARAMS,
        "/objects"
      },
      {
        CLASS_PARAMS,
        "/objects/someClass"
      },
      {
        ID_PARAMS,
        "/objects/someId"
      },
      {
        SOME_PARAMS,
        "/objects/someClass?consistency_level=QUORUM"
      },
      {
        ALL_PARAMS,
        "/objects/someClass/someId?consistency_level=QUORUM"
      },
    };
  }

  @Test
  @DataMethod(source = ObjectsPathTest.class, method = "provideUpdateForNotSupported")
  public void shouldBuildUpdatePathsWhenNotSupported(ObjectsPath.Params params, String expectedPath) {
    Mockito.when(dbVersionSupportMock.supportsClassNameNamespacedEndpoints()).thenReturn(false);

    assertThat(objectsPath.buildUpdate(params)).isEqualTo(expectedPath);
  }

  public static Object[][] provideUpdateForNotSupported() {
    return new Object[][]{
      {
        EMPTY_PARAMS,
        "/objects"
      },
      {
        CLASS_PARAMS,
        "/objects"
      },
      {
        ID_PARAMS,
        "/objects/someId"
      },
      {
        SOME_PARAMS,
        "/objects?consistency_level=QUORUM"
      },
      {
        ALL_PARAMS,
        "/objects/someId?consistency_level=QUORUM"
      },
    };
  }
}
