package io.weaviate.client6.v1;

import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.Test;

public class ObjectMetadataTest {

  @Test
  public final void testMetadata_id() {
    var metadata = new ObjectMetadata(m -> m.id("object-1"));
    Assertions.assertThat(metadata.id)
        .as("object id").isEqualTo("object-1");
  }

  @Test
  public final void testVectorsMetadata_unnamed() {
    Float[] vector = { 1f, 2f, 3f };
    var metadata = new ObjectMetadata(m -> m.vectors(Vectors.unnamed(vector)));

    Assertions.assertThat(metadata.vectors)
        .as("unnamed vector").isNotNull()
        .returns(Optional.of(vector), Vectors::getUnnamed)
        .returns(Optional.empty(), Vectors::getSingle);
  }

  @Test
  public final void testVectorsMetadata_default() {
    Float[] vector = { 1f, 2f, 3f };
    var metadata = new ObjectMetadata(m -> m.vectors(vector));

    Assertions.assertThat(metadata.vectors)
        .as("default vector").isNotNull()
        .returns(vector, Vectors::getDefaultSingle)
        .returns(Optional.of(vector), Vectors::getSingle)
        .returns(Optional.empty(), Vectors::getUnnamed);
  }

  @Test
  public final void testVectorsMetadata_default_2d() {
    Float[][] vector = { { 1f, 2f, 3f }, { 1f, 2f, 3f } };
    var metadata = new ObjectMetadata(m -> m.vectors(vector));

    Assertions.assertThat(metadata.vectors)
        .as("default 2d vector").isNotNull()
        .returns(vector, Vectors::getDefaultMulti)
        .returns(Optional.of(vector), Vectors::getMulti)
        .returns(Optional.empty(), Vectors::getUnnamed);
  }

  @Test
  public final void testVectorsMetadata_named() {
    Float[] vector = { 1f, 2f, 3f };
    var metadata = new ObjectMetadata(m -> m.vectors("vector-1", vector));

    Assertions.assertThat(metadata.vectors)
        .as("named vector").isNotNull()
        .returns(vector, v -> v.getSingle("vector-1"))
        .returns(Optional.of(vector), Vectors::getSingle)
        .returns(null, Vectors::getDefaultSingle);
  }

  @Test
  public final void testVectorsMetadata_named_2d() {
    Float[][] vector = { { 1f, 2f, 3f }, { 1f, 2f, 3f } };
    var metadata = new ObjectMetadata(m -> m.vectors("vector-1", vector));

    Assertions.assertThat(metadata.vectors)
        .as("named 2d vector").isNotNull()
        .returns(vector, v -> v.getMulti("vector-1"))
        .returns(Optional.of(vector), Vectors::getMulti)
        .returns(null, Vectors::getDefaultMulti);
  }

  @Test
  public final void testVectorsMetadata_multiple_named() {
    Float[][] vector_1 = { { 1f, 2f, 3f }, { 1f, 2f, 3f } };
    Float[] vector_2 = { 4f, 5f, 6f };
    var metadata = new ObjectMetadata(m -> m.vectors(
        named -> named
            .vector("vector-1", vector_1)
            .vector("vector-2", vector_2)));

    Assertions.assertThat(metadata.vectors)
        .as("multiple named vectors").isNotNull()
        .returns(vector_1, v -> v.getMulti("vector-1"))
        .returns(vector_2, v -> v.getSingle("vector-2"))
        .returns(Optional.empty(), Vectors::getMulti)
        .returns(Optional.empty(), Vectors::getSingle)
        .returns(null, Vectors::getDefaultMulti);
  }
}
