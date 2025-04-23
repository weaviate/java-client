package io.weaviate.integration;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.Test;

import io.weaviate.ConcurrentTest;
import io.weaviate.client6.WeaviateClient;
import io.weaviate.client6.v1.collections.Collection;
import io.weaviate.client6.v1.collections.Property;
import io.weaviate.client6.v1.collections.Reference;
import io.weaviate.containers.Container;

/**
 * Scenarios related to reference properties:
 * <ul>
 * <li>create collection with (nested) reference properties</li>
 * <li>insert objects with (nested) references</li>
 * <li>add (nested) references</li>
 * <li>search by reference (nested) properties</li>
 * </ul>
 */
public class ReferencesITest extends ConcurrentTest {
  private static final WeaviateClient client = Container.WEAVIATE.getClient();

  @Test
  public void testReferences() throws IOException {
    // Arrange: create collection with cross-references
    var nsArtists = ns("Artists");
    var nsGrammy = ns("Grammy");
    var nsOscar = ns("Oscar");

    client.collections.create(nsOscar);
    client.collections.create(nsGrammy);

    // Act: create Artists collection with hasAwards reference
    client.collections.create(nsArtists,
        col -> col
            .properties(
                Property.text("name"),
                Property.integer("age"),
                Property.reference("hasAwards", nsGrammy, nsOscar)));

    var artists = client.collections.use(nsArtists);
    var grammies = client.collections.use(nsGrammy);
    var oscars = client.collections.use(nsOscar);

    // Act: check collection configuration is correct
    var collectionArtists = artists.config.get();
    Assertions.assertThat(collectionArtists).get()
        .as("Artists: create collection")
        .extracting(Collection::properties)
        .extracting(properties -> properties.stream().filter(Property::isReference).findFirst())
        .extracting(Optional::get)
        .returns("hasAwards", Property::name)
        .extracting(Property::dataTypes)
        .asInstanceOf(InstanceOfAssertFactories.list(String.class))
        .containsOnly(nsGrammy, nsOscar);

    // Act: insert some data
    var grammy_1 = grammies.data.insert(Map.of());
    var grammy_2 = grammies.data.insert(Map.of());
    var oscar_1 = oscars.data.insert(Map.of());
    var oscar_2 = oscars.data.insert(Map.of());

    var alex = artists.data.insert(
        Map.of("name", "Alex"),
        opt -> opt
            .reference("hasAwards", Reference.uuids(
                grammy_1.metadata().id(), oscar_1.metadata().id()))
            .reference("hasAwards", Reference.objects(grammy_2, oscar_2)));

    // Act: add one more reference
    var nsMovies = ns("Movies");
    client.collections.create(nsMovies);
    artists.config.addProperty(Property.reference("featuredIn", nsMovies));

    collectionArtists = artists.config.get();
    Assertions.assertThat(collectionArtists).get()
        .as("Artists: add reference to Movies")
        .extracting(Collection::properties)
        .extracting(properties -> properties.stream()
            .filter(property -> property.name().equals("featuredIn"))
            .findFirst())
        .extracting(Optional::get)
        .returns(true, Property::isReference)
        .extracting(Property::dataTypes)
        .asInstanceOf(InstanceOfAssertFactories.list(String.class))
        .containsOnly(nsMovies);
  }
}
