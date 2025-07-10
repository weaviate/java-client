package io.weaviate.integration.client.aliases;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.util.HashMap;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import io.weaviate.client.Config;
import io.weaviate.client.WeaviateClient;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.aliases.model.Alias;
import io.weaviate.client.v1.schema.model.WeaviateClass;
import io.weaviate.integration.client.WeaviateDockerCompose;

public class ClientAliasesTest {
  private WeaviateClient client;

  @ClassRule
  public static WeaviateDockerCompose compose = new WeaviateDockerCompose();

  @Before
  public void before() {
    Config config = new Config("http", compose.getHttpHostAddress());
    client = new WeaviateClient(config);
  }

  @Test
  public void shouldManageAliases() {
    // Arrange
    Result<Boolean> createdPaul = client.schema().classCreator().withClass(WeaviateClass.builder()
        .className("PaulHewson").build()).run();
    assumeTrue(createdPaul.getResult(), "created PaulHewson collection");

    Result<Boolean> createdGeorge = client.schema().classCreator().withClass(WeaviateClass.builder()
        .className("GeorgeBarnes").build()).run();
    assumeTrue(createdGeorge.getResult(), "created GeorgeBarnes collection");

    // Act: create alias
    client.alias().creator().withClassName("PaulHewson").withAlias("Bono").run();
    client.alias().creator().withClassName("GeorgeBarnes").withAlias("MachineGunKelly").run();

    // Assert: get all
    Result<Map<String, Alias>> all = client.alias().allGetter().run();

    Assertions.assertThat(all.getError()).isNull();
    Assertions.assertThat(all.getResult())
        .as("fetched all aliases")
        .containsAllEntriesOf(new HashMap<String, Alias>() {
          {
            put("Bono", new Alias("PaulHewson", "Bono"));
            put("MachineGunKelly", new Alias("GeorgeBarnes", "MachineGunKelly"));
          }
        });

    // Act: update
    Result<Boolean> createdMGK = client.schema().classCreator().withClass(WeaviateClass.builder()
        .className("ColsonBaker").build()).run();
    assumeTrue(createdMGK.getResult(), "created ColsonBaker collection");

    client.alias().updater().withAlias("MachineGunKelly").withNewClassName("ColsonBaker").run();

    // Assert: get one
    Result<Alias> mgk = client.alias().getter().withAlias("MachineGunKelly").run();

    Assertions.assertThat(mgk.getResult())
        .as("MachineGunKelly alias points to ColsonBaker")
        .returns("MachineGunKelly", Alias::getAlias)
        .returns("ColsonBaker", Alias::getClassName);

    Result<Map<String, Alias>> colsonAliases = client.alias().allGetter().withClassName("ColsonBaker").run();
    Assertions.assertThat(colsonAliases.getResult())
        .containsOnlyKeys("MachineGunKelly")
        .extracting(Map::values, InstanceOfAssertFactories.collection(Alias.class))
        .extracting(Alias::getClassName).containsOnly("ColsonBaker");

    // Act: delete
    client.alias().deleter().withAlias("Bono").run();

    // Assert
    Result<Alias> bono = client.alias().getter().withAlias("Bono").run();
    Assertions.assertThat(bono)
        .as("Bono alias deleted")
        .returns(null, Result::getResult)
        .extracting(Result::getError).isNull();
  }
}
