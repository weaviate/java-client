package io.weaviate.client.v1.graphql.query.fields;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FieldTest {

  @Test
  public void testBuild() {
    // given
    String expected = "_additional{certainty}";
    Field field = Field.builder()
            .name("_additional")
            .fields(new Field[]{ Field.builder().name("certainty").build() })
            .build();
    // when
    String fieldString = field.build();
    // then
    assertEquals(expected, fieldString);
  }

  @Test
  public void testBuild2() {
    // given
    String expected = "_additional{classification{basedOn classifiedFields completed id scope}}";
    Field _additional = Field.builder()
            .name("_additional")
            .fields(new Field[]{
                    Field.builder()
                            .name("classification")
                            .fields(new Field[]{
                                    Field.builder().name("basedOn").build(),
                                    Field.builder().name("classifiedFields").build(),
                                    Field.builder().name("completed").build(),
                                    Field.builder().name("id").build(),
                                    Field.builder().name("scope").build()
                            }).build()
            }).build();
    // when
    String fieldString = _additional.build();
    // then
    assertEquals(expected, fieldString);
  }

  @Test
  public void testBuild3() {
    // given
    String expected = "inPublication{... on Publication{name}}";
    Field field = Field.builder()
            .name("inPublication")
            .fields(new Field[]{
                    Field.builder()
                            .name("... on Publication")
                            .fields(new Field[]{
                                    Field.builder().name("name").build()
                            }).build()
            }).build();
    // when
    String fieldString = field.build();
    // then
    assertEquals(expected, fieldString);
  }

  @Test
  public void testBuild4() {
    // given
    String expected = "_additional{distance}";
    Field field = Field.builder()
            .name("_additional")
            .fields(new Field[]{ Field.builder().name("distance").build() })
            .build();
    // when
    String fieldString = field.build();
    // then
    assertEquals(expected, fieldString);
  }
}
