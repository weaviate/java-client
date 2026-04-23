package io.weaviate.integration;

import org.assertj.core.api.Assertions;
import org.junit.BeforeClass;
import org.junit.Test;

import io.weaviate.ConcurrentTest;
import io.weaviate.client6.v1.api.WeaviateClient;
import io.weaviate.client6.v1.api.collections.Property;
import io.weaviate.client6.v1.api.collections.Tokenization;
import io.weaviate.containers.Container;
import io.weaviate.containers.Weaviate;

public class TokenizeITest extends ConcurrentTest {
  private static final WeaviateClient client = Container.WEAVIATE.getClient();

  @BeforeClass
  public static void __() {
    Weaviate.Version.V137.orSkip();
  }

  @Test
  public void testTokenize() throws Exception {
    var nsWords = ns("Words");
    client.collections.create(nsWords,
        c -> c.properties(Property.text("sentence",
            p -> p.tokenization(Tokenization.TRIGRAM))));

    var sentence = "hello world";

    // Act
    var custom = client.tokenize.text(sentence,
        tok -> tok.tokenization(Tokenization.TRIGRAM));

    var existing = client.tokenize.forProperty(sentence,
        nsWords, "sentence");

    // Assert
    Assertions.assertThat(existing).isEqualTo(custom);
  }
}
