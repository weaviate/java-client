package technology.semi.weaviate.client.v1.data.util;

import com.jparams.junit4.JParamsTestRunner;
import org.junit.runner.RunWith;
import technology.semi.weaviate.client.v1.data.replication.model.ConsistencyLevel;

@RunWith(JParamsTestRunner.class)
public class ObjectsPathTest {

  private static final ObjectsPath.Params EMPTY_PARAMS = ObjectsPath.Params.builder().build();
  private static final ObjectsPath.Params CLASS_PARAMS = ObjectsPath.Params.builder()
    .className("someClass")
    .build();
  private static final ObjectsPath.Params ID_PARAMS = ObjectsPath.Params.builder()
    .id("someId")
    .build();
  private static final ObjectsPath.Params CLASS_QUERY_PARAMS = ObjectsPath.Params.builder()
    .className("someClass")
    .limit(10)
    .build();
  private static final ObjectsPath.Params ALL_PARAMS = ObjectsPath.Params.builder()
    .className("someClass")
    .id("someId")
    .limit(100)
    .additional(new String[]{"additional1", "additional2"})
    .build();
  private static final ObjectsPath.Params CONSISTENCY_LEVEL_CLASS_ID_PARAMS = ObjectsPath.Params.builder()
    .className("someClass")
    .id("someId")
    .consistencyLevel(ConsistencyLevel.QUORUM)
    .build();
  private static final ObjectsPath.Params NODE_NAME_CLASS_ID_PARAMS = ObjectsPath.Params.builder()
    .className("someClass")
    .id("someId")
    .nodeName("node1")
    .build();
  private static final ObjectsPath.Params CONSISTENCY_LEVEL_ALL_PARAMS = ObjectsPath.Params.builder()
    .className("someClass")
    .id("someId")
    .additional(new String[]{"additional1", "additional2"})
    .consistencyLevel(ConsistencyLevel.QUORUM)
    .build();
  private static final ObjectsPath.Params NODE_NAME_ALL_PARAMS = ObjectsPath.Params.builder()
    .className("someClass")
    .id("someId")
    .additional(new String[]{"additional1", "additional2"})
    .nodeName("node1")
    .build();

  public static Object[][] provideCreateForSupported() {
    return new Object[][]{
      {
        EMPTY_PARAMS,
        "/objects"
      },
      {
        ALL_PARAMS,
        "/objects"
      },
    };
  }

  public static Object[][] provideCreateForNotSupported() {
    return new Object[][]{
      {
        EMPTY_PARAMS,
        "/objects"
      },
      {
        ALL_PARAMS,
        "/objects"
      },
    };
  }

  public static Object[][] provideDeleteForSupported() {
    return new Object[][]{
      {
        EMPTY_PARAMS,   // TODO should be valid?
        "/objects"
      },
      {
        CLASS_PARAMS,   // TODO should be valid?
        "/objects/someClass"
      },
      {
        ID_PARAMS,      // TODO should be valid?
        "/objects/someId"
      },
      {
        ALL_PARAMS,
        "/objects/someClass/someId"
      },
    };
  }

  public static Object[][] provideDeleteForNotSupported() {
    return new Object[][]{
      {
        EMPTY_PARAMS,   // TODO should be valid?
        "/objects"
      },
      {
        CLASS_PARAMS,   // TODO should be valid?
        "/objects"
      },
      {
        ID_PARAMS,      // TODO should be valid?
        "/objects/someId"
      },
      {
        ALL_PARAMS,
        "/objects/someId"
      },
    };
  }

  public static Object[][] provideCheckForSupported() {
    return new Object[][]{
      {
        EMPTY_PARAMS,   // TODO should be valid?
        "/objects"
      },
      {
        CLASS_PARAMS,   // TODO should be valid?
        "/objects/someClass"
      },
      {
        ID_PARAMS,      // TODO should be valid?
        "/objects/someId"
      },
      {
        ALL_PARAMS,
        "/objects/someClass/someId"
      },
    };
  }

  public static Object[][] provideCheckForNotSupported() {
    return new Object[][]{
      {
        EMPTY_PARAMS,   // TODO should be valid?
        "/objects"
      },
      {
        CLASS_PARAMS,   // TODO should be valid?
        "/objects"
      },
      {
        ID_PARAMS,      // TODO should be valid?
        "/objects/someId"
      },
      {
        ALL_PARAMS,
        "/objects/someId"
      },
    };
  }

  public static Object[][] provideGetOneForSupported() {
    return new Object[][]{
      {
        EMPTY_PARAMS,   // TODO should be valid?
        "/objects"
      },
      {
        CLASS_PARAMS,   // TODO should be valid?
        "/objects/someClass"
      },
      {
        ID_PARAMS,      // TODO should be valid?
        "/objects/someId"
      },
      {
        ALL_PARAMS,
        "/objects/someClass/someId?include=additional1,additional2"
      },
      {
        CONSISTENCY_LEVEL_CLASS_ID_PARAMS,
        "/objects/someClass/someId?consistency_level=QUORUM"
      },
      {
        NODE_NAME_CLASS_ID_PARAMS,
        "/objects/someClass/someId?node_name=node1"
      },
      {
        CONSISTENCY_LEVEL_ALL_PARAMS,
        "/objects/someClass/someId?include=additional1,additional2&consistency_level=QUORUM"
      },
      {
        NODE_NAME_ALL_PARAMS,
        "/objects/someClass/someId?include=additional1,additional2&node_name=node1"
      }
    };
  }

  public static Object[][] provideGetOneForNotSupported() {
    return new Object[][]{
      {
        EMPTY_PARAMS,   // TODO should be valid?
        "/objects"
      },
      {
        CLASS_PARAMS,   // TODO should be valid?
        "/objects"
      },
      {
        ID_PARAMS,      // TODO should be valid?
        "/objects/someId"
      },
      {
        ALL_PARAMS,
        "/objects/someId?include=additional1,additional2"
      },
    };
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
        ALL_PARAMS,
        "/objects?include=additional1,additional2&limit=100"
      },
      {
        CLASS_QUERY_PARAMS,
        "/objects?limit=10&class=someClass"
      }
    };
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
        ALL_PARAMS,
        "/objects?include=additional1,additional2&limit=100"
      },
      {
        CLASS_QUERY_PARAMS,
        "/objects?limit=10"
      }
    };
  }

  public static Object[][] provideUpdateForSupported() {
    return new Object[][]{
      {
        EMPTY_PARAMS,   // TODO should be valid?
        "/objects"
      },
      {
        CLASS_PARAMS,   // TODO should be valid?
        "/objects/someClass"
      },
      {
        ID_PARAMS,      // TODO should be valid?
        "/objects/someId"
      },
      {
        ALL_PARAMS,
        "/objects/someClass/someId"
      },
    };
  }

  public static Object[][] provideUpdateForNotSupported() {
    return new Object[][]{
      {
        EMPTY_PARAMS,   // TODO should be valid?
        "/objects"
      },
      {
        CLASS_PARAMS,   // TODO should be valid?
        "/objects"
      },
      {
        ID_PARAMS,      // TODO should be valid?
        "/objects/someId"
      },
      {
        ALL_PARAMS,
        "/objects/someId"
      },
    };
  }
}
