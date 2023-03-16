package io.weaviate.client.v1.graphql.query.argument;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class Bm25ArgumentTest {

  @Test
  public void shouldCreateArgument() {
    Bm25Argument bm25 = Bm25Argument.builder()
      .query("I'm a simple string")
      .build();

    String str = bm25.build();

    assertThat(str).isEqualTo("bm25:{query:\"I'm a simple string\"}");
  }

  @Test
  public void shouldCreateArgumentWithProperties() {
    Bm25Argument bm25 = Bm25Argument.builder()
      .query("I'm a simple string")
      .properties(new String[]{"prop1", "prop2"})
      .build();

    String str = bm25.build();

    assertThat(str).isEqualTo("bm25:{query:\"I'm a simple string\" " +
      "properties:[\"prop1\",\"prop2\"]}");
  }

  @Test
  public void shouldCreateArgumentWithChars() {
    Bm25Argument bm25 = Bm25Argument.builder()
      .query("\"I'm a complex string\" says the {'`:string:`'}")
      .properties(new String[]{"prop:\"'`{0}`'\""})
      .build();

    String str = bm25.build();

    assertThat(str).isEqualTo("bm25:{query:\"\\\"I'm a complex string\\\" says the {'`:string:`'}\" " +
      "properties:[\"prop:\\\"'`{0}`'\\\"\"]}");
  }
}
