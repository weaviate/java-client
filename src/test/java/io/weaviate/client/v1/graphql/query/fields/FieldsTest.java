package io.weaviate.client.v1.graphql.query.fields;

import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;

public class FieldsTest extends TestCase {

  @Test
  public void testBuild() {
    // given
    String expected = "a b c";
    Field a = Field.builder().name("a").build();
    Field b = Field.builder().name("b").build();
    Field c = Field.builder().name("c").build();
    Fields fields = Fields.builder().fields(new Field[]{ a, b, c }).build();
    // when
    String fieldsParameter = fields.build();
    // then
    Assert.assertEquals(expected, fieldsParameter);
  }

  @Test
  public void testBuildNested() {
    // given
    String expected = "a{b} c{d{e}}";
    Field b = Field.builder().name("b").build();
    Field a = Field.builder()
            .name("a")
            .fields(new Field[]{b})
            .build();
    Field c = Field.builder()
            .name("c")
            .fields(new Field[]{Field.builder()
                    .name("d")
                    .fields(new Field[]{Field.builder().name("e").build()})
                    .build()}).build();
    Fields fields = Fields.builder().fields(new Field[]{ a, c }).build();
    // when
    String fieldsParameter = fields.build();
    // then
    Assert.assertEquals(expected, fieldsParameter);
  }
}
