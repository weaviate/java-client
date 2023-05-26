package io.weaviate.client.v1.graphql.query.fields;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class GenerativeSearchBuilderTest {

  @Test
  public void shouldBuildEmptyField() {
    GenerativeSearchBuilder generativeSearchBuilder = GenerativeSearchBuilder.builder()
      .build();

    Field generate = generativeSearchBuilder.build();

    assertThat(generate.getName()).isBlank();
    assertThat(generate.getFields()).isNull();
  }

  @Test
  public void shouldBuildSingleResultPromptField() {
    GenerativeSearchBuilder generativeSearchBuilder = GenerativeSearchBuilder.builder()
      .singleResultPrompt("What is the meaning of life?")
      .build();

    Field generate = generativeSearchBuilder.build();

    assertThat(generate.getName()).isEqualTo("generate(" +
      "singleResult:{prompt:\"\"\"What is the meaning of life?\"\"\"}" +
      ")");
    assertThat(generate.getFields()).extracting(Field::getName)
      .containsExactly("singleResult", "error");
  }

  @Test
  public void shouldBuildGroupedResultTaskField() {
    GenerativeSearchBuilder generativeSearchBuilder = GenerativeSearchBuilder.builder()
      .groupedResultTask("Explain why these magazines or newspapers are about finance")
      .build();

    Field generate = generativeSearchBuilder.build();

    assertThat(generate.getName()).isEqualTo("generate(" +
      "groupedResult:{task:\"\"\"Explain why these magazines or newspapers are about finance\"\"\"}" +
      ")");
    assertThat(generate.getFields()).extracting(Field::getName)
      .containsExactly("groupedResult", "error");
  }

  @Test
  public void shouldBuildBothSingleResultPromptAndGroupedResultTaskField() {
    GenerativeSearchBuilder generativeSearchBuilder = GenerativeSearchBuilder.builder()
      .singleResultPrompt("What is the meaning of life?")
      .groupedResultTask("Explain why these magazines or newspapers are about finance")
      .build();

    Field generate = generativeSearchBuilder.build();

    assertThat(generate.getName()).isEqualTo("generate(" +
      "singleResult:{prompt:\"\"\"What is the meaning of life?\"\"\"} " +
      "groupedResult:{task:\"\"\"Explain why these magazines or newspapers are about finance\"\"\"}" +
      ")");
    assertThat(generate.getFields()).extracting(Field::getName)
      .containsExactly("singleResult", "groupedResult", "error");
  }

  @Test
  public void shouldBuildBothSingleResultPromptAndGroupedResultTaskFieldWithChars() {
    GenerativeSearchBuilder generativeSearchBuilder = GenerativeSearchBuilder.builder()
      .singleResultPrompt("\"I'm a complex string\" says the {'`:string:`'}")
      .groupedResultTask("\"I'm a complex string\" says the {'`:string:`'}")
      .build();

    Field generate = generativeSearchBuilder.build();

    assertThat(generate.getName()).isEqualTo("generate(" +
      "singleResult:{prompt:\"\"\"\\\"I'm a complex string\\\" says the {'`:string:`'}\"\"\"} " +
      "groupedResult:{task:\"\"\"\\\"I'm a complex string\\\" says the {'`:string:`'}\"\"\"}" +
      ")");
    assertThat(generate.getFields()).extracting(Field::getName)
      .containsExactly("singleResult", "groupedResult", "error");
  }

  @Test
  public void shouldBuildGroupedResultTaskFieldAndProperties() {
    GenerativeSearchBuilder generativeSearchBuilder = GenerativeSearchBuilder.builder()
      .groupedResultTask("Explain why these magazines or newspapers are about finance")
      .groupedResultProperties(new String[]{"property1", "property2"})
      .build();

    Field generate = generativeSearchBuilder.build();

    assertThat(generate.getName()).isEqualTo("generate(" +
      "groupedResult:{task:\"\"\"Explain why these magazines or newspapers are about finance\"\"\" properties:[\"property1\",\"property2\"]}" +
      ")");
    assertThat(generate.getFields()).extracting(Field::getName)
      .containsExactly("groupedResult", "error");
  }

  @Test
  public void shouldBuildBothSingleResultPromptAndGroupedResultTaskFieldWithCharsAndProperties() {
    GenerativeSearchBuilder generativeSearchBuilder = GenerativeSearchBuilder.builder()
      .singleResultPrompt("\"I'm a complex string\" says the {'`:string:`'}")
      .groupedResultTask("\"I'm a complex string\" says the {'`:string:`'}")
      .groupedResultProperties(new String[]{"content"})
      .build();

    Field generate = generativeSearchBuilder.build();

    assertThat(generate.getName()).isEqualTo("generate(" +
      "singleResult:{prompt:\"\"\"\\\"I'm a complex string\\\" says the {'`:string:`'}\"\"\"} " +
      "groupedResult:{task:\"\"\"\\\"I'm a complex string\\\" says the {'`:string:`'}\"\"\" properties:[\"content\"]}" +
      ")");
    assertThat(generate.getFields()).extracting(Field::getName)
      .containsExactly("singleResult", "groupedResult", "error");
  }
}
