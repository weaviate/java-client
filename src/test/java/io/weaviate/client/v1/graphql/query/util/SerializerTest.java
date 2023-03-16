package io.weaviate.client.v1.graphql.query.util;

import com.jparams.junit4.JParamsTestRunner;
import com.jparams.junit4.data.DataMethod;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(JParamsTestRunner.class)
public class SerializerTest {

  @Test
  @DataMethod(source = SerializerTest.class, method = "provideForEscape")
  public void shouldEscapeString(String input, String expected) {
    String escaped = Serializer.escape(input);

    assertThat(escaped).isEqualTo(expected);
  }

  public static Object[][] provideForEscape() {
    return new Object[][] {
      new Object[] {
        "I'm a string with double \"quotes\"",
        "I'm a string with double \\\"quotes\\\"",
      },
      new Object[] {
        "I'm a string with curly {brackets} and :colons:",
        "I'm a string with curly {brackets} and :colons:",
      },
      new Object[] {
        "I'm a string with single 'quotes' and `backticks`",
        "I'm a string with single 'quotes' and `backticks`",
      },
      new Object[] {
        "",
        "",
      },
      new Object[] {
        null,
        "",
      },
    };
  }

  @Test
  @DataMethod(source = SerializerTest.class, method = "provideForQuote")
  public void shouldQuoteString(String input, String expected) {
    String quoted = Serializer.quote(input);

    assertThat(quoted).isEqualTo(expected);
  }

  public static Object[][] provideForQuote() {
    return new Object[][] {
      new Object[] {
        "I'm a string with double \"quotes\"",
        "\"I'm a string with double \\\"quotes\\\"\"",
      },
      new Object[] {
        "I'm a string with curly {brackets} and :colons:",
        "\"I'm a string with curly {brackets} and :colons:\"",
      },
      new Object[] {
        "I'm a string with single 'quotes' and `backticks`",
        "\"I'm a string with single 'quotes' and `backticks`\"",
      },
      new Object[] {
        "",
        "\"\"",
      },
      new Object[] {
        null,
        "",
      },
    };
  }

  @Test
  @DataMethod(source = SerializerTest.class, method = "provideForArrayQuotes")
  public void shouldBuildArrayWithQuotes(String[] input, String expected) {
    String arrayQuotes = Serializer.arrayWithQuotes(input);

    assertThat(arrayQuotes).isEqualTo(expected);
  }

  public static Object[][] provideForArrayQuotes() {
    return new Object[][] {
      new Object[]{
        new String[]{"some string", "other string"},
        "[\"some string\",\"other string\"]",
      },
      new Object[]{
        new String[]{
          "I'm a string with double \"quotes\"",
          "I'm a string with curly {brackets} and :colons:",
          "I'm a string with single 'quotes' and `backticks`",
        },
        "[\"I'm a string with double \\\"quotes\\\"\"," +
          "\"I'm a string with curly {brackets} and :colons:\"," +
          "\"I'm a string with single 'quotes' and `backticks`\"]"
      },
      new Object[] {
        new String[]{},
        "[]",
      },
      new Object[] {
        null,
        "[]",
      },
    };
  }

  @Test
  @DataMethod(source = SerializerTest.class, method = "provideForArray")
  public void shouldBuildArray(Object[] input, String expected) {
    String array = Serializer.array(input);

    assertThat(array).isEqualTo(expected);
  }

  public static Object[][] provideForArray() {
    return new Object[][] {
      new Object[]{
        new Float[]{ .1f, .2f, .3f },
        "[0.1,0.2,0.3]",
      },
      new Object[]{
        new Float[]{},
        "[]",
      },
      new Object[]{
        null,
        "[]",
      }
    };
  }
}
