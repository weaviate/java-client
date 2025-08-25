package io.weaviate.integration;

import java.io.IOException;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import io.weaviate.ConcurrentTest;
import io.weaviate.client6.v1.api.WeaviateClient;
import io.weaviate.client6.v1.api.alias.Alias;
import io.weaviate.containers.Container;

public class AliasITest extends ConcurrentTest {
  private static WeaviateClient client = Container.WEAVIATE.getClient();

  @Test
  public void test_aliasLifecycle() throws IOException {
    // Arrange
    var nsPaulHewson = ns("PaulHewson");
    var nsGeorgeBarnes = ns("GeorgeBarnes");
    var nsColsonBaker = ns("ColsonBaker");

    for (var collection : List.of(nsPaulHewson, nsGeorgeBarnes, nsColsonBaker)) {
      client.collections.create(collection);
    }

    // Act: create aliases
    client.alias.create(nsPaulHewson, "Bono");
    client.alias.create(nsGeorgeBarnes, "MachineGunKelly");

    // Assert: list all
    var aliases = client.alias.list();
    Assertions.assertThat(aliases).hasSize(2);
    Assertions.assertThat(aliases)
        .as("created Bono and MachineGunKelly aliases")
        .contains(
            new Alias(nsPaulHewson, "Bono"),
            new Alias(nsGeorgeBarnes, "MachineGunKelly"));

    // Act: update aliases
    client.alias.update("MachineGunKelly", nsColsonBaker);

    // Assert: check MGK points to another collection
    var mgk = client.alias.get("MachineGunKelly");
    Assertions.assertThat(mgk).get()
        .as("updated MachineGunKelly alias")
        .returns(nsColsonBaker, Alias::collection);

    // Act: delete Bono alias
    client.alias.delete("Bono");

    // Assert
    var paulHewsonAliases = client.alias.list(all -> all.collection(nsPaulHewson));
    Assertions.assertThat(paulHewsonAliases)
        .as("no aliases once Bono is deleted")
        .isEmpty();
  }
}
