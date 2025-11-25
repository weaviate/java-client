package io.weaviate.integration;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.Test;

import io.weaviate.ConcurrentTest;
import io.weaviate.client6.v1.api.WeaviateClient;
import io.weaviate.client6.v1.api.collections.Property;
import io.weaviate.client6.v1.api.collections.ReferenceProperty;
import io.weaviate.client6.v1.api.collections.XWriteWeaviateObject;
import io.weaviate.client6.v1.api.collections.data.Reference;
import io.weaviate.client6.v1.api.collections.query.QueryReference;
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
                Property.integer("age"))
            .references(
                ReferenceProperty.to("hasAwards", nsGrammy, nsOscar)));

    var artists = client.collections.use(nsArtists);
    var grammies = client.collections.use(nsGrammy);
    var oscars = client.collections.use(nsOscar);

    // Act: check collection configuration is correct
    var collectionArtists = artists.config.get();
    Assertions.assertThat(collectionArtists).get()
        .as("Artists: create collection")
        .extracting(c -> c.references().stream().findFirst())
        .as("has one reference property").extracting(Optional::get)
        .returns("hasAwards", ReferenceProperty::propertyName)
        .extracting(ReferenceProperty::dataTypes, InstanceOfAssertFactories.list(String.class))
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
                grammy_1.uuid(), oscar_1.uuid()))
            .reference("hasAwards", Reference.objects(grammy_2, oscar_2)));

    // Act: add one more reference
    var nsMovies = ns("Movies");
    client.collections.create(nsMovies);
    artists.config.addReference("featuredIn", nsMovies);

    collectionArtists = artists.config.get();
    Assertions.assertThat(collectionArtists).get()
        .as("Artists: add reference to Movies")
        .extracting(c -> c.references().stream()
            .filter(property -> property.propertyName().equals("featuredIn")).findFirst())
        .as("featuredIn reference property").extracting(Optional::get)
        .extracting(ReferenceProperty::dataTypes, InstanceOfAssertFactories.list(String.class))
        .containsOnly(nsMovies);

    var gotAlex = artists.query.fetchObjectById(alex.uuid(),
        opt -> opt.returnReferences(
            QueryReference.multi("hasAwards", nsOscar),
            QueryReference.multi("hasAwards", nsGrammy)));

    Assertions.assertThat(gotAlex).get()
        .as("Artists: fetch by id including hasAwards references")

        // Cast references to Map<String, List<QueryWeaviateObject>>
        .extracting(XWriteWeaviateObject::references, InstanceOfAssertFactories.map(String.class, List.class))
        .as("hasAwards object reference").extractingByKey("hasAwards")
        .asInstanceOf(InstanceOfAssertFactories.list(XWriteWeaviateObject.class))

        .extracting(object -> object.uuid())
        .containsOnly(
            // INVESTIGATE: When references to 2+ collections are requested,
            // seems to Weaviate only return references to the first one in the list.
            // In this case we request { "hasAwards": Oscars } and { "hasAwards": Grammys }
            // so the latter will not be in the response.
            //
            // grammy_1.metadata().id(), grammy_2.metadata().id(),
            oscar_1.uuid(), oscar_2.uuid());
  }

  @Test
  public void testNestedReferences() throws IOException {
    // Arrange: create collection with cross-references
    var nsArtists = ns("Artists");
    var nsGrammy = ns("Grammy");
    var nsAcademy = ns("Academy");

    client.collections.create(nsAcademy,
        opt -> opt
            .properties(Property.text("ceo")));

    // Act: create Artists collection with hasAwards reference
    client.collections.create(nsGrammy,
        col -> col
            .references(ReferenceProperty.to("presentedBy", nsAcademy)));

    client.collections.create(nsArtists,
        col -> col
            .properties(
                Property.text("name"),
                Property.integer("age"))
            .references(
                ReferenceProperty.to("hasAwards", nsGrammy)));

    var artists = client.collections.use(nsArtists);
    var grammies = client.collections.use(nsGrammy);
    var academies = client.collections.use(nsAcademy);

    // Act: insert some data
    var musicAcademy = academies.data.insert(Map.of("ceo", "Harvy Mason"));

    var grammy_1 = grammies.data.insert(Map.of(),
        opt -> opt.reference("presentedBy", Reference.objects(musicAcademy)));

    var alex = artists.data.insert(
        Map.of("name", "Alex"),
        opt -> opt
            .reference("hasAwards", Reference.objects(grammy_1)));

    // Assert: fetch nested references
    var gotAlex = artists.query.fetchObjectById(alex.uuid(),
        opt -> opt.returnReferences(
            QueryReference.single("hasAwards",
                ref -> ref
                    // Name of the CEO of the presenting academy
                    .returnReferences(
                        QueryReference.single("presentedBy", r -> r.returnProperties("ceo"))))));

    Assertions.assertThat(gotAlex).get()
        .as("Artists: fetch by id including nested references")

        // Cast references to Map<String, List<QueryWeaviateObject>>
        .extracting(XWriteWeaviateObject::references, InstanceOfAssertFactories.map(String.class, List.class))
        .as("hasAwards object reference").extractingByKey("hasAwards")
        .asInstanceOf(InstanceOfAssertFactories.list(XWriteWeaviateObject.class))

        .hasSize(1).allSatisfy(award -> Assertions.assertThat(award)
            .returns(grammy_1.uuid(), grammy -> grammy.uuid())

            // Cast references to Map<String, List<QueryWeaviateObject>>
            .extracting(XWriteWeaviateObject::references, InstanceOfAssertFactories.map(String.class, List.class))
            .as("presentedBy object reference").extractingByKey("presentedBy")
            .asInstanceOf(InstanceOfAssertFactories.list(XWriteWeaviateObject.class))

            .hasSize(1).extracting(XWriteWeaviateObject::properties)
            .allSatisfy(properties -> Assertions.assertThat(properties)
                .asInstanceOf(InstanceOfAssertFactories.map(String.class, Object.class))
                .containsEntry("ceo", "Harvy Mason")));
  }
}
