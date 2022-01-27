package technology.semi.weaviate.client.v1.graphql.query.argument;

import junit.framework.TestCase;
import org.junit.Test;

public class AskArgumentTest extends TestCase {

  @Test
  public void testBuild() {
    // given
    String question = "What's your name?";
    // when
    String arg = AskArgument.builder().question(question).build().build();
    // then
    assertNotNull(arg);
    assertEquals("ask: {question: \"What's your name?\"}", arg);
  }

  @Test
  public void testBuildWithProperties() {
    // given
    String question = "What's your name?";
    String[] properties = new String[]{ "prop1", "prop2" };
    // when
    String arg = AskArgument.builder()
            .question(question).properties(properties)
            .build().build();
    // then
    assertNotNull(arg);
    assertEquals("ask: {question: \"What's your name?\" properties: [\"prop1\", \"prop2\"]}", arg);
  }

  @Test
  public void testBuildWithPropertiesAndCertainty() {
    // given
    String question = "What's your name?";
    String[] properties = new String[]{ "prop1", "prop2" };
    Float certainty = 0.8f;
    // when
    String arg = AskArgument.builder()
            .question(question).properties(properties).certainty(certainty)
            .build().build();
    // then
    assertNotNull(arg);
    assertEquals("ask: {question: \"What's your name?\" properties: [\"prop1\", \"prop2\"] certainty: 0.8}", arg);
  }

  @Test
  public void testBuildWithAutocorrect() {
    // given
    String question = "What's your name?";
    // when
    String arg = AskArgument.builder().question(question).autocorrect(true).build().build();
    // then
    assertNotNull(arg);
    assertEquals("ask: {question: \"What's your name?\" autocorrect: true}", arg);
  }

  @Test
  public void testBuildWithPropertiesAndCertaintyAndAutocorrect() {
    // given
    String question = "What's your name?";
    String[] properties = new String[]{ "prop1", "prop2" };
    Float certainty = 0.8f;
    // when
    String arg = AskArgument.builder()
            .question(question).properties(properties).certainty(certainty).autocorrect(false)
            .build().build();
    // then
    assertNotNull(arg);
    assertEquals("ask: {question: \"What's your name?\" properties: [\"prop1\", \"prop2\"] certainty: 0.8 autocorrect: false}", arg);
  }

  @Test
  public void testBuildWithPropertiesAndCertaintyAndAutocorrectAndRerank() {
    // given
    String question = "What's your name?";
    String[] properties = new String[]{ "prop1", "prop2" };
    Float certainty = 0.8f;
    // when
    String arg = AskArgument.builder()
            .question(question).properties(properties).certainty(certainty).autocorrect(false).rerank(true)
            .build().build();
    // then
    assertNotNull(arg);
    assertEquals("ask: {question: \"What's your name?\" properties: [\"prop1\", \"prop2\"] certainty: 0.8 autocorrect: false rerank: true}", arg);
  }

  @Test
  public void testBuildWithRerank() {
    // given
    String question = "What's your name?";
    // when
    String arg = AskArgument.builder().question(question).rerank(false).build().build();
    // then
    assertNotNull(arg);
    assertEquals("ask: {question: \"What's your name?\" rerank: false}", arg);
  }
}