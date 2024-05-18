package io.weaviate.integration.client.schema;

import com.jparams.junit4.JParamsTestRunner;
import com.jparams.junit4.data.DataMethod;
import io.weaviate.client.Config;
import io.weaviate.client.WeaviateClient;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.WeaviateError;
import io.weaviate.client.base.WeaviateErrorMessage;
import io.weaviate.client.v1.schema.model.DataType;
import io.weaviate.client.v1.schema.model.Property;
import io.weaviate.client.v1.schema.model.Tokenization;
import io.weaviate.client.v1.schema.model.WeaviateClass;
import io.weaviate.integration.client.WeaviateDockerCompose;
import java.util.Collections;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JParamsTestRunner.class)
public class ClientSchemaDefaultsAndMigrationTest {
  private WeaviateClient client;

  @ClassRule
  public static WeaviateDockerCompose compose = new WeaviateDockerCompose();

  @Before
  public void before() {
    String httpHost = compose.getHttpHostAddress();
    Config config = new Config("http", httpHost);

    client = new WeaviateClient(config);
  }

  @After
  public void after() {
    Result<Boolean> deleted = client.schema().allDeleter().run();
    assertThat(deleted.hasErrors()).isFalse();
  }

  @DataMethod(source = ClientSchemaDefaultsAndMigrationTest.class, method = "provideForDataTypeAndTokenization")
  @Test
  public void shouldCreatePropertyWithDataTypeAndTokenization(String dataType, String tokenization,
                                                              String expectedDataType, String expectedTokenization) {
    WeaviateClass clazz = WeaviateClass.builder()
      .className("SomeClass")
      .description("some class description")
      .properties(Collections.singletonList(Property.builder()
        .name("someProperty")
        .description("some property description")
        .dataType(Collections.singletonList(dataType))
        .tokenization(tokenization)
        .build()
      ))
      .build();

    Result<Boolean> createStatus = client.schema().classCreator()
      .withClass(clazz)
      .run();

    assertThat(createStatus).isNotNull()
      .returns(false, Result::hasErrors)
      .returns(true, Result::getResult);

    Result<WeaviateClass> classStatus = client.schema().classGetter()
      .withClassName("SomeClass")
      .run();

    assertThat(classStatus).isNotNull()
      .returns(false, Result::hasErrors);
    assertThat(classStatus.getResult()).isNotNull()
      .extracting(WeaviateClass::getProperties).asList()
      .hasSize(1)
      .first().extracting(prop -> (Property) prop)
      .returns(Collections.singletonList(expectedDataType), Property::getDataType)
      .returns(expectedTokenization, Property::getTokenization);
  }

  public static Object[][] provideForDataTypeAndTokenization() {
    return new Object[][]{
      new Object[]{
        DataType.TEXT, null,
        DataType.TEXT, Tokenization.WORD,
      },
      new Object[]{
        DataType.TEXT, "",
        DataType.TEXT, Tokenization.WORD,
      },
      new Object[]{
        DataType.TEXT, Tokenization.WORD,
        DataType.TEXT, Tokenization.WORD,
      },
      new Object[]{
        DataType.TEXT, Tokenization.LOWERCASE,
        DataType.TEXT, Tokenization.LOWERCASE,
      },
      new Object[]{
        DataType.TEXT, Tokenization.WHITESPACE,
        DataType.TEXT, Tokenization.WHITESPACE,
      },
      new Object[]{
        DataType.TEXT, Tokenization.FIELD,
        DataType.TEXT, Tokenization.FIELD,
      },

      new Object[]{
        DataType.TEXT_ARRAY, null,
        DataType.TEXT_ARRAY, Tokenization.WORD,
      },
      new Object[]{
        DataType.TEXT_ARRAY, "",
        DataType.TEXT_ARRAY, Tokenization.WORD,
      },
      new Object[]{
        DataType.TEXT_ARRAY, Tokenization.WORD,
        DataType.TEXT_ARRAY, Tokenization.WORD,
      },
      new Object[]{
        DataType.TEXT_ARRAY, Tokenization.LOWERCASE,
        DataType.TEXT_ARRAY, Tokenization.LOWERCASE,
      },
      new Object[]{
        DataType.TEXT_ARRAY, Tokenization.WHITESPACE,
        DataType.TEXT_ARRAY, Tokenization.WHITESPACE,
      },
      new Object[]{
        DataType.TEXT_ARRAY, Tokenization.FIELD,
        DataType.TEXT_ARRAY, Tokenization.FIELD,
      },

      new Object[]{
        DataType.STRING, null,
        DataType.TEXT, Tokenization.WHITESPACE,
      },
      new Object[]{
        DataType.STRING, "",
        DataType.TEXT, Tokenization.WHITESPACE,
      },
      new Object[]{
        DataType.STRING, Tokenization.WORD,
        DataType.TEXT, Tokenization.WHITESPACE,
      },
      new Object[]{
        DataType.STRING, Tokenization.FIELD,
        DataType.TEXT, Tokenization.FIELD,
      },

      new Object[]{
        DataType.STRING_ARRAY, null,
        DataType.TEXT_ARRAY, Tokenization.WHITESPACE,
      },
      new Object[]{
        DataType.STRING_ARRAY, "",
        DataType.TEXT_ARRAY, Tokenization.WHITESPACE,
      },
      new Object[]{
        DataType.STRING_ARRAY, Tokenization.WORD,
        DataType.TEXT_ARRAY, Tokenization.WHITESPACE,
      },
      new Object[]{
        DataType.STRING_ARRAY, Tokenization.FIELD,
        DataType.TEXT_ARRAY, Tokenization.FIELD,
      },

      new Object[]{
        DataType.INT, null,
        DataType.INT, null,
      },
      new Object[]{
        DataType.INT, "",
        DataType.INT, null,
      },

      new Object[]{
        DataType.INT_ARRAY, null,
        DataType.INT_ARRAY, null,
      },
      new Object[]{
        DataType.INT_ARRAY, "",
        DataType.INT_ARRAY, null,
      },
    };
  }


  @DataMethod(source = ClientSchemaDefaultsAndMigrationTest.class, method = "provideInvalidForDataTypeAndTokenization")
  @Test
  public void shouldNotCreatePropertyWithDataTypeAndTokenization(String dataType, String tokenization) {
    WeaviateClass clazz = WeaviateClass.builder()
      .className("SomeClass")
      .description("some class description")
      .properties(Collections.singletonList(Property.builder()
        .name("someProperty")
        .description("some property description")
        .dataType(Collections.singletonList(dataType))
        .tokenization(tokenization)
        .build()
      ))
      .build();

    Result<Boolean> createStatus = client.schema().classCreator()
      .withClass(clazz)
      .run();

    assertThat(createStatus).isNotNull()
      .returns(true, Result::hasErrors)
      .extracting(Result::getError)
      .returns(422, WeaviateError::getStatusCode)
      .extracting(WeaviateError::getMessages).asList()
      .hasSizeGreaterThanOrEqualTo(1)
      .first().extracting(msg -> ((WeaviateErrorMessage) msg).getMessage()).asString()
      .contains("is not allowed for data type");
  }

  public static Object[][] provideInvalidForDataTypeAndTokenization() {
    return new Object[][]{
      new Object[]{
        DataType.STRING, Tokenization.LOWERCASE,
      },
      new Object[]{
        DataType.STRING, Tokenization.WHITESPACE,
      },

      new Object[]{
        DataType.STRING_ARRAY, Tokenization.LOWERCASE,
      },
      new Object[]{
        DataType.STRING_ARRAY, Tokenization.WHITESPACE,
      },

      new Object[]{
        DataType.INT, Tokenization.WORD,
      },
      new Object[]{
        DataType.INT, Tokenization.LOWERCASE,
      },
      new Object[]{
        DataType.INT, Tokenization.WHITESPACE,
      },
      new Object[]{
        DataType.INT, Tokenization.FIELD,
      },

      new Object[]{
        DataType.INT_ARRAY, Tokenization.WORD,
      },
      new Object[]{
        DataType.INT_ARRAY, Tokenization.LOWERCASE,
      },
      new Object[]{
        DataType.INT_ARRAY, Tokenization.WHITESPACE,
      },
      new Object[]{
        DataType.INT_ARRAY, Tokenization.FIELD,
      },
    };
  }


  @DataMethod(source = ClientSchemaDefaultsAndMigrationTest.class, method = "provideForDataTypeAndIndexing")
  @Test
  public void shouldCreatePropertyWithDataTypeAndIndexing(String dataType, Boolean inverted, Boolean filterable,
                                                          Boolean searchable, Boolean expectedInverted,
                                                          Boolean expectedFilterable, Boolean expectedSearchable) {
    WeaviateClass clazz = WeaviateClass.builder()
      .className("SomeClass")
      .description("some class description")
      .properties(Collections.singletonList(Property.builder()
        .name("someProperty")
        .description("some property description")
        .dataType(Collections.singletonList(dataType))
        .indexInverted(inverted)
        .indexFilterable(filterable)
        .indexSearchable(searchable)
        .build()
      ))
      .build();

    Result<Boolean> createStatus = client.schema().classCreator()
      .withClass(clazz)
      .run();

    assertThat(createStatus).isNotNull()
      .returns(false, Result::hasErrors)
      .returns(true, Result::getResult);

    Result<WeaviateClass> classStatus = client.schema().classGetter()
      .withClassName("SomeClass")
      .run();

    assertThat(classStatus).isNotNull()
      .returns(false, Result::hasErrors);
    assertThat(classStatus.getResult()).isNotNull()
      .extracting(WeaviateClass::getProperties).asList()
      .hasSize(1)
      .first().extracting(prop -> (Property) prop)
      .returns(expectedInverted, Property::getIndexInverted)
      .returns(expectedFilterable, Property::getIndexFilterable)
      .returns(expectedSearchable, Property::getIndexSearchable);
  }

  public static Object[][] provideForDataTypeAndIndexing() {
    return new Object[][]{
      new Object[]{
        DataType.TEXT,
        null, null, null,
        null, Boolean.TRUE, Boolean.TRUE,
      },
      new Object[]{
        DataType.TEXT,
        null, null, Boolean.FALSE,
        null, Boolean.TRUE, Boolean.FALSE,
      },
      new Object[]{
        DataType.TEXT,
        null, null, Boolean.TRUE,
        null, Boolean.TRUE, Boolean.TRUE,
      },
      new Object[]{
        DataType.TEXT,
        null, Boolean.FALSE, null,
        null, Boolean.FALSE, Boolean.TRUE,
      },
      new Object[]{
        DataType.TEXT,
        null, Boolean.FALSE, Boolean.FALSE,
        null, Boolean.FALSE, Boolean.FALSE,
      },
      new Object[]{
        DataType.TEXT,
        null, Boolean.FALSE, Boolean.TRUE,
        null, Boolean.FALSE, Boolean.TRUE,
      },
      new Object[]{
        DataType.TEXT,
        null, Boolean.TRUE, null,
        null, Boolean.TRUE, Boolean.TRUE,
      },
      new Object[]{
        DataType.TEXT,
        null, Boolean.TRUE, Boolean.FALSE,
        null, Boolean.TRUE, Boolean.FALSE,
      },
      new Object[]{
        DataType.TEXT,
        null, Boolean.TRUE, Boolean.TRUE,
        null, Boolean.TRUE, Boolean.TRUE,
      },
      new Object[]{
        DataType.TEXT,
        Boolean.FALSE, null, null,
        null, Boolean.FALSE, Boolean.FALSE,
      },
      new Object[]{
        DataType.TEXT,
        Boolean.TRUE, null, null,
        null, Boolean.TRUE, Boolean.TRUE,
      },

      new Object[]{
        DataType.INT,
        null, null, null,
        null, Boolean.TRUE, Boolean.FALSE,
      },
      new Object[]{
        DataType.INT,
        null, null, Boolean.FALSE,
        null, Boolean.TRUE, Boolean.FALSE,
      },
      new Object[]{
        DataType.INT,
        null, Boolean.FALSE, null,
        null, Boolean.FALSE, Boolean.FALSE,
      },
      new Object[]{
        DataType.INT,
        null, Boolean.FALSE, Boolean.FALSE,
        null, Boolean.FALSE, Boolean.FALSE,
      },
      new Object[]{
        DataType.INT,
        null, Boolean.TRUE, null,
        null, Boolean.TRUE, Boolean.FALSE,
      },
      new Object[]{
        DataType.INT,
        null, Boolean.TRUE, Boolean.FALSE,
        null, Boolean.TRUE, Boolean.FALSE,
      },
      new Object[]{
        DataType.INT,
        Boolean.FALSE, null, null,
        null, Boolean.FALSE, Boolean.FALSE,
      },
      new Object[]{
        DataType.INT,
        Boolean.TRUE, null, null,
        null, Boolean.TRUE, Boolean.FALSE,
      },
    };
  }


  @DataMethod(source = ClientSchemaDefaultsAndMigrationTest.class, method = "provideInvalidForDataTypeAndIndexing")
  @Test
  public void shouldNotCreatePropertyWithDataTypeAndIndexing(String dataType, Boolean inverted, Boolean filterable,
                                                             Boolean searchable, String expectedErrMsg) {
    WeaviateClass clazz = WeaviateClass.builder()
      .className("SomeClass")
      .description("some class description")
      .properties(Collections.singletonList(Property.builder()
        .name("someProperty")
        .description("some property description")
        .dataType(Collections.singletonList(dataType))
        .indexInverted(inverted)
        .indexFilterable(filterable)
        .indexSearchable(searchable)
        .build()
      ))
      .build();

    Result<Boolean> createStatus = client.schema().classCreator()
      .withClass(clazz)
      .run();

    assertThat(createStatus).isNotNull()
      .returns(true, Result::hasErrors)
      .extracting(Result::getError)
      .returns(422, WeaviateError::getStatusCode)
      .extracting(WeaviateError::getMessages).asList()
      .hasSizeGreaterThanOrEqualTo(1)
      .first().extracting(msg -> ((WeaviateErrorMessage) msg).getMessage()).asString()
      .contains(expectedErrMsg);
  }

  public static Object[][] provideInvalidForDataTypeAndIndexing() {
    return new Object[][]{
      new Object[]{
        DataType.TEXT,
        Boolean.FALSE, null, Boolean.FALSE,
        "`indexInverted` is deprecated and can not be set together with `indexFilterable` or `indexSearchable`",
      },
      new Object[]{
        DataType.TEXT,
        Boolean.FALSE, null, Boolean.TRUE,
        "`indexInverted` is deprecated and can not be set together with `indexFilterable` or `indexSearchable`",
      },
      new Object[]{
        DataType.TEXT,
        Boolean.FALSE, Boolean.FALSE, null,
        "`indexInverted` is deprecated and can not be set together with `indexFilterable` or `indexSearchable`",
      },
      new Object[]{
        DataType.TEXT,
        Boolean.FALSE, Boolean.FALSE, Boolean.FALSE,
        "`indexInverted` is deprecated and can not be set together with `indexFilterable` or `indexSearchable`",
      },
      new Object[]{
        DataType.TEXT,
        Boolean.FALSE, Boolean.FALSE, Boolean.TRUE,
        "`indexInverted` is deprecated and can not be set together with `indexFilterable` or `indexSearchable`",
      },
      new Object[]{
        DataType.TEXT,
        Boolean.FALSE, Boolean.TRUE, null,
        "`indexInverted` is deprecated and can not be set together with `indexFilterable` or `indexSearchable`",
      },
      new Object[]{
        DataType.TEXT,
        Boolean.FALSE, Boolean.TRUE, Boolean.FALSE,
        "`indexInverted` is deprecated and can not be set together with `indexFilterable` or `indexSearchable`",
      },
      new Object[]{
        DataType.TEXT,
        Boolean.FALSE, Boolean.TRUE, Boolean.TRUE,
        "`indexInverted` is deprecated and can not be set together with `indexFilterable` or `indexSearchable`",
      },
      new Object[]{
        DataType.TEXT,
        Boolean.TRUE, null, Boolean.FALSE,
        "`indexInverted` is deprecated and can not be set together with `indexFilterable` or `indexSearchable`",
      },
      new Object[]{
        DataType.TEXT,
        Boolean.TRUE, null, Boolean.TRUE,
        "`indexInverted` is deprecated and can not be set together with `indexFilterable` or `indexSearchable`",
      },
      new Object[]{
        DataType.TEXT,
        Boolean.TRUE, Boolean.FALSE, null,
        "`indexInverted` is deprecated and can not be set together with `indexFilterable` or `indexSearchable`",
      },
      new Object[]{
        DataType.TEXT,
        Boolean.TRUE, Boolean.FALSE, Boolean.FALSE,
        "`indexInverted` is deprecated and can not be set together with `indexFilterable` or `indexSearchable`",
      },
      new Object[]{
        DataType.TEXT,
        Boolean.TRUE, Boolean.FALSE, Boolean.TRUE,
        "`indexInverted` is deprecated and can not be set together with `indexFilterable` or `indexSearchable`",
      },
      new Object[]{
        DataType.TEXT,
        Boolean.TRUE, Boolean.TRUE, null,
        "`indexInverted` is deprecated and can not be set together with `indexFilterable` or `indexSearchable`",
      },
      new Object[]{
        DataType.TEXT,
        Boolean.TRUE, Boolean.TRUE, Boolean.FALSE,
        "`indexInverted` is deprecated and can not be set together with `indexFilterable` or `indexSearchable`",
      },
      new Object[]{
        DataType.TEXT,
        Boolean.TRUE, Boolean.TRUE, Boolean.TRUE,
        "`indexInverted` is deprecated and can not be set together with `indexFilterable` or `indexSearchable`",
      },

      new Object[]{
        DataType.INT,
        Boolean.FALSE, null, Boolean.FALSE,
        "`indexInverted` is deprecated and can not be set together with `indexFilterable` or `indexSearchable`",
      },
      new Object[]{
        DataType.INT,
        Boolean.FALSE, null, Boolean.TRUE,
        "`indexInverted` is deprecated and can not be set together with `indexFilterable` or `indexSearchable`",
      },
      new Object[]{
        DataType.INT,
        Boolean.FALSE, Boolean.FALSE, null,
        "`indexInverted` is deprecated and can not be set together with `indexFilterable` or `indexSearchable`",
      },
      new Object[]{
        DataType.INT,
        Boolean.FALSE, Boolean.FALSE, Boolean.FALSE,
        "`indexInverted` is deprecated and can not be set together with `indexFilterable` or `indexSearchable`",
      },
      new Object[]{
        DataType.INT,
        Boolean.FALSE, Boolean.FALSE, Boolean.TRUE,
        "`indexInverted` is deprecated and can not be set together with `indexFilterable` or `indexSearchable`",
      },
      new Object[]{
        DataType.INT,
        Boolean.FALSE, Boolean.TRUE, null,
        "`indexInverted` is deprecated and can not be set together with `indexFilterable` or `indexSearchable`",
      },
      new Object[]{
        DataType.INT,
        Boolean.FALSE, Boolean.TRUE, Boolean.FALSE,
        "`indexInverted` is deprecated and can not be set together with `indexFilterable` or `indexSearchable`",
      },
      new Object[]{
        DataType.INT,
        Boolean.FALSE, Boolean.TRUE, Boolean.TRUE,
        "`indexInverted` is deprecated and can not be set together with `indexFilterable` or `indexSearchable`",
      },
      new Object[]{
        DataType.INT,
        Boolean.TRUE, null, Boolean.FALSE,
        "`indexInverted` is deprecated and can not be set together with `indexFilterable` or `indexSearchable`",
      },
      new Object[]{
        DataType.INT,
        Boolean.TRUE, null, Boolean.TRUE,
        "`indexInverted` is deprecated and can not be set together with `indexFilterable` or `indexSearchable`",
      },
      new Object[]{
        DataType.INT,
        Boolean.TRUE, Boolean.FALSE, null,
        "`indexInverted` is deprecated and can not be set together with `indexFilterable` or `indexSearchable`",
      },
      new Object[]{
        DataType.INT,
        Boolean.TRUE, Boolean.FALSE, Boolean.FALSE,
        "`indexInverted` is deprecated and can not be set together with `indexFilterable` or `indexSearchable`",
      },
      new Object[]{
        DataType.INT,
        Boolean.TRUE, Boolean.FALSE, Boolean.TRUE,
        "`indexInverted` is deprecated and can not be set together with `indexFilterable` or `indexSearchable`",
      },
      new Object[]{
        DataType.INT,
        Boolean.TRUE, Boolean.TRUE, null,
        "`indexInverted` is deprecated and can not be set together with `indexFilterable` or `indexSearchable`",
      },
      new Object[]{
        DataType.INT,
        Boolean.TRUE, Boolean.TRUE, Boolean.FALSE,
        "`indexInverted` is deprecated and can not be set together with `indexFilterable` or `indexSearchable`",
      },
      new Object[]{
        DataType.INT,
        Boolean.TRUE, Boolean.TRUE, Boolean.TRUE,
        "`indexInverted` is deprecated and can not be set together with `indexFilterable` or `indexSearchable`",
      },

      new Object[]{
        DataType.INT,
        null, null, Boolean.TRUE,
        "`indexSearchable` is allowed only for text/text[] data types. For other data types set false or leave empty",
      },
      new Object[]{
        DataType.INT,
        null, Boolean.FALSE, Boolean.TRUE,
        "`indexSearchable` is allowed only for text/text[] data types. For other data types set false or leave empty",
      },
      new Object[]{
        DataType.INT,
        null, Boolean.TRUE, Boolean.TRUE,
        "`indexSearchable` is allowed only for text/text[] data types. For other data types set false or leave empty",
      },
    };
  }
}
