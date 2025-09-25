# Weaviate Java client  <img alt='Weaviate logo' src='https://raw.githubusercontent.com/weaviate/weaviate/19de0956c69b66c5552447e84d016f4fe29d12c9/docs/assets/weaviate-logo.png' width='180' align='right' />

[![Build Status](https://github.com/weaviate/java-client/actions/workflows/.github/workflows/test.yaml/badge.svg?branch=main)](https://github.com/weaviate/java-client/actions/workflows/.github/workflows/test.yaml)

Official Weaviate Java Client.

> [!IMPORTANT]
> `client6` does not support many of the legacy features supported in other clients. Ensure your instance is running at least v1.32 to avoid compatibility issues.

## Usage

To start using Weaviate Java Client add the dependency to `pom.xml`:

```xml
<dependency>
    <groupId>io.weaviate</groupId>
    <artifactId>client6</artifactId>
    <version>6.0.0-beta4</version>
</dependency>
```

### Uber JAR🫙

If you're building a uber-JAR with something like `maven-assembly-plugin`, use a shaded version with classifier `all`.
This ensures that all dynamically-loaded dependecies of `io.grpc` are resolved correctly.

### SNAPSHOT releases

The latest development version of `client6` is released after every merged pull request. To include it in you project set the version to `6.0.0-SNAPSHOT` and [configure your `<repositories>` section accordingly](https://central.sonatype.org/publish/publish-portal-snapshots/#consuming-snapshot-releases-for-your-project).
Please be mindful of the fact that this is not a stable release and breaking changes may be introduced.

Snapshot releases overwrite each other, so no two releases are alike. If you find a bug in one of the `SNAPSHOT` versions that you'd like to report, please include the output of `Debug.printBuildInfo()` in the ticket's description.

```java
import io.weaviate.client6.v1.internal.Debug;

public class App {
    public static void main(String[] args) {
        Debug.printBuildInfo();

        // ...the rest of your application code...
    }
}
```

### Gson and reflective access to internal JDK classes

The client uses Google's [`gson`](https://github.com/google/gson) for JSON de-/serialization which does reflection on internal `java.lang` classes. This is _not allowed by default_ in Java 9 and above.

To work around this, it's necessary to add this JVM command line argument:

```
--add-opens=java.base/java.lang=ALL-UNNAMED
```

If you're using Gradle, you can add this instead to your `application` block in your `build.gradle.kts` file:

```kotlin
applicationDefaultJvmArgs += listOf(
    "--add-opens=java.base/java.lang=ALL-UNNAMED",
)
```

## Supported APIs

### Tucked Builder

Tucked Builder is an iteration of the Builder pattern that reduces boilerplate and leverages static typing and autocompletion to help API discovery.
It is well-worth getting familiar with Tucked Builders before diving into the next sections, as the library makes intensive use of this pattern across its API surface.

If you've worked with Elasticserch Java API Client before, you'll recognize this pattern as [Builder lamba expressions](https://www.elastic.co/docs/reference/elasticsearch/clients/java/api-conventions/building-objects#_builder_lambda_expressions).

Most operations in Weaviate have multiple optional parameters and Builder patter is a common way to implement that. For example, here's what a nearVector query _could_ look like:

```java
import io.weaviate.client6.v1.api.collections.query.Hybrid;
import io.weaviate.client6.v1.api.collections.query.NearText;
import io.weaviate.client6.v1.api.collections.query.NearText.Move;

Move moveTo = Move.builder()
    .force(.5f)
    .concepts("lullaby")
    .build();
NearText nearText = NearText.builder()
  .concepts("sunshine", "butterflies")
  .distance(.4f)
  .moveTo(moveTo)
  .build();
Hybrid hybridQuery = Hybrid.builder()
  .concepts("rainbow")
  .nearText(nearText)
  .queryProperties("title", "lyrics")
  .returnProperties("album", "author")
  .build();

songs.query.hybrid(hybridQuery);
```

The Tucked Builder pattern replaces repetitive `.builder() [...] .build()` with a **lambda expression** which accepts the pre-instantiated builder object as its only argument.
If that's a mouthful, take a look at what the query above looks like in `client6`. After all, seeing is believing:

```java
import io.weaviate.client6.v1.api.collections.query.NearText;

songs.query.hybrid(
  "rainbow",
  /* Hybrid.Builder */ h -> h
    .nearText(NearText.of(
      List.of("sunshine", "butterflies"),
      /* NearText.Builder */ nt -> nt
        .distance(.4f)
        .moveTo(.5f, /* NearText.Move.Builder */ to -> to.concepts("lullaby"))
    )
    .queryProperties("title", "lyrics")
    .returnProperties("album", "author")
);
```

Notice how the type of each lambda argument can be automatically deduced from the methods' signatures. This allows the autocomplete to correctly suggest possible arguments, guiding you through the query API.The builder itself is "tucked" in the method's internals, so you needn't remember how to access or import it. What's more, the code reads a lot more like a query thanks to improved [locality](https://htmx.org/essays/locality-of-behaviour/). As you'll see in the examples below, you can also get creative with naming the lambda argument to act as hint for future readers.

In real-world programs there will be cases where you need to inject some control-flow statements in the query builder code. Consider an example of limiting the number of query results based on some external value, such as a URL query paramter. Lambda expressions are fully-fledged functions, so you could add a if-statement right in the middle of it:

```java
songs.query.hybrid("rainbow", h -> {
  if (limitURL != null) {
    h.limit(limitURL);
  }
  return h;
});
```

This may get out of hand quickly if complex logic is involved. Or you may simply prefer the standard Builder pattern. Whichever's the case, `client6` has got you covered, as "tucked" builders are public members of the classes they build, and can be used directly:

```java
Hybrid.Builder builder = new Hybrid.Builder("rainbow");
if (limitURL != null) {
  builder.limit(limitURL)
}

// more complex steps...

songs.query.hybrid(/* Hybrid */ builder.build());
```

Finally, if you need to separate "query definition" from "performing the query", most objects provide two static factories: one with required arguments and one with required aruments and a tucked builder.

```java
Hybrid requiredOnly = Hybrid.of("rainbow");
Hybrid withOptional = Hybrid.of("rainbow", opt -> opt.limit(10));

songs.query.hybrid(withOptional);
```

### Connecting to a Weaviate instance

```java
WeaviateClient client = WeaviateClient.connectToCustom(
  conn -> conn
    .scheme("http")
    .httpPort(8080).httpHost("localhost")
    .grpcPort(50051).grpcHost("localhost")
    .setHeader("X-Custom-Header", "Readme")
);
```

Shorthand methods for connecting to a local instance and a Weaviate Cloud cluster are available too:

```java
// Defaults to scheme=http, host=localhost, port=8080, grpcHost=locahost, grpcPort=50051
WeaviateClient local = WeaviateClient.connectToLocal(local -> local.port(9090));

// Always uses httpPort=443, grpcPort=443, httpHost == gprcHost == <clusterUrl>, and API Key authentication
WeaviateClient wcd = WeaviateClient.connectToWeaviateCloud("my-cluster-url.io", "my-api-key");
```

The client holds a number of resources (HTTP connection pools, gRPC channel) which must be disposed of correclty then they are no longer needed.
If the client's lifecycle is tied to that of your app, closing the client via `client.close()` is a good way to do that.

Otherwise, it is a good idea to use the client inside a [try-with-resources](https://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html) statement:

```java
try (final var client = new WeaviateClient(config)) {
  // Do something with the client
}
```
WeaviateClient will be automatically closed when execution exits the block.

#### Authentication

Weaviate supports several authentication methods:

| Method  | Client API reference                                                            |
| ------- | ------------------------------------------------------------------------------- |
| API Key                 | `Authentication.apiKey("my-api-key")`                           |
| Resource owner password | `Authentication.resourceOwnerPassword("username", "password")`  |
| Client credentials      | `Authentication.clientCredentials("clientKey", "clientSecret")` |
| Existing bearer token   | `Authentication.apiKey("access-token", "refresh-token", 900)`   |

Follow the [documentation](https://docs.weaviate.io/deploy/configuration/authentication) for a detailed discussion.

### Collection management

```java
client.collections.create(
  "Songs",
  collection -> collection
    .properties(
      Property.text("title"),
      Property.text("lyrics", p -> p.tokenization(Tokenization.WORD)),
      Property.integer("yearReleased"),
      Property.blob("albumCover"),
      Property.bool("isSingle")
    )
    .references(
      ReferenceProperty.to("hasAwards", "GrammyAwards", "BillboardMusicAwards")
    )
    .vectorConfig(
      VectorConfig.text2vecWeaviate("title_vec", t2v -> t2v.sourceProperties("title")),
      VectorConfig.text2vecWeaviate("lyrics_vec", t2v -> t2v.sourceProperties("lyrics")),
      VectorConfig.img2vecNeural("cover_vec", i2v -> i2v.imageFields("albumCover"))
    )
);

assert client.collections.exists("Songs");

client.collections.delete("Songs");
assert !client.collections.exists("Songs");
```

Other methods in `collections` namespace include:

- `getConfig(String collection)` to fetch collection configuration;
- `list()` to fetch collection configurations for all existing collections
- `deleteAll()` to drop all collections and their data

#### Using a Collection Handle

Once a collection is created, you can obtain another client object (a "handle") that's scoped to that collection.

```java
CollectionHandle<Map<String,Object>> songs = client.collections.use("Songs");
```

Using the handle, we can ingest new data into the collection and query it, as well as modify the configuration.
The handle object is thread safe and, although lightweight, is best created once and shared across threads / callers.

```java
// Bad: creates a new CollectionHandle object for each iteration, strains the GC unnecessarily.
for (var song : mySongs) {
  client.collections.use("Songs").data.insert(song);
}

// Good: the same CollectionHandle is reused across multiple iterations / processes.
var songs = client.collections.use("Songs");
Thread.run(() -> rapSongs.forEach(song -> songs.data.insert(song)));
Thread.run(() -> popSongs.forEach(song -> songs.data.insert(song)));
```

For the rest of the document, assume `songs` is handle for the "Songs" collection defined elsewhere.

#### Generic `PropertiesT`

Weaviate client lets you insert object properties in different "shapes". The compile-time type in which the properties must be passed is determied by a generic paramter in CollectionHandle object.
By defalt, the value for this parameter is `Map<String, Object>`. That allows you to think of your data as JSON objects with some additional metadata (vector embedding, UUID, certainty score, etc.).

In practice this means that whenever data needs to be inserted, you need to pass an instance of `Map<String, Object>` and whenever it is queried, the properties are deserialized into a `Map<String, Object>`.
If you prefer stricter typing, you can leverage our built-in ORM to work with properties as custom Java types. We will return to this in  the **ORM** section later. Assume for now that properties are being passed around as an "untyped" map.

### Ingesting data

Data operations are concentrated behind the `.data` namespace.

#### Insert single object

```java
var yellowSubmarine = songs.data.insert(Map.of("title", "Yellow Submarine", "lyrics", "...", "year", 1969);
System.out.println("Inserted new song at "+ yellowSubmarine.metadata().createdAt());
System.out.println("Yellow Submarine uuid="+ yellowSubmarine.uuid());
```

You can supply your own UUID and vector embedding:

```java
songs.data.insert(Map.of(...), obj -> obj.uuid("valid-custom-uuid").vectors(Vectors.of("title_vec", new float[]{...})));
```

Weaviate supports 1-dimensional and multi-dimensional vector embeddings, thanks to ColBERT-family modules. The associated vector can be `float[] | float[][]`.
Because Java does not support unions of primitive types, we define an abstraction called `Vectors` which is a container type for object's vector embeddings.

Creating a new vector object is simple:

- `Vectors.of(new float[]{...})`: default 1-d vector
- `Vectors.of("custom_1d", new float[]{...})`: 1-d vector with a custom name
- `Vectors.of(new float[][]{...})`: default 2-d vector
- `Vectors.of("custom_2d", new float[][]{...})`: 2-d vector with a custom name
- `Vectors.of(Vectors.of(...), Vectors.of(...))`: Multiple vectors, all must define a custom name

Here's how you can retrieve the actual vector associated with the object:

```java
Vectors vectors = yellowSubmarine.vectors();
float[] v = vectors.getDefaultSingle(); // default 1-dimensional vector
float[] v = vectors.getSingle("custom_1d"); // 1-d vector with a custom name
float[][] v = vectors.getDefaultMulti(); // default 2-dimensional vector
float[][] v = vectors.getMulti("custom_2d"); // 2-d vector with a custom name
```

#### Batch insert

> [!NOTE]
> Support for Dynamic Batching in `client6` will be added once Server-Side Batching becomes GA in Weaviate (est. v1.34)

```java
InsertManyResponse response = songs.data.insertMany(
    Map.of("title", "High-Speed Dirt", "artist", "Megadeth"),
    Map.of("title", "Rattlehead", "artist", "Megadeth")
);

if (!response.errors().isEmpty()) {
  throw new RuntimeException(String.join(", ", response.errors()));
}
System.out.println("Inserted %d objects, took: %.2fs".formatted(response.reponses().size(), response.took()));
```

To supply your own UUID and vector embedding when inserting multiple objects wrap each obejct in `WeaviateObject.of(...)`:

```java
songs.data.insertMany(
  WeaviateObject.of(map1,  obj -> obj.uuid(uuid1)),
  WeaviateObject.of(map2, obj -> obj.uuid(uuid2))
)
```

### Querying data

Query methods are concentrated behind the `.query` namespace.

By default, _all object properties_ and its UUID are included in the response. To select a subset of properties, pass their names to `.returnProperties(...)` method on the tucked builder. Retrieve additional metadata (where relevant) like so:

```java
// Distance and Certainty are only relevant to semantic search
q -> q.returnMetadata(Metadata.VECTOR, Metadata.DISTANCE, Metadata.CERTAINTY)

// Score and Explain Score are only relevant to BM25 and hybrid queries
q -> q.returnMetadata(Metadata.SCORE, Metadata.EXPLAIN_SCORE)
```

#### Semantic search

```java
songs.query.nearVector(new float[]{...}, nv -> nv.distance(.3f));
songs.query.nearText("a song about weather", nt -> nt.moveAway(.6f, from -> from.concepts("summertime")));
songs.query.nearObject(yellowSubmarine.uuid(), nobj -> nobj.excludeSelf());
songs.query.nearImage("base64-encoded-image");
// Other "near-media" methods available: nearVideo, nearAudio, nearDepth, nearImu, nearThermal
```

> [!TIP]
> The first object returned in a NearObject query will _always_ be the search object itself. To filter it out, use the `.excludeSelf()` helper as in the example above.

#### Keyword and Hybrid search

```java
songs.query.bm25("rain", bm25 -> bm25.queryProeperties("lyrics"));

songs.query.hybrid(
  "rain",
  h -> h
    .queryProperties("lyrics")
    .nearVector(NearVector.of(new float[]{...}))
);
```

#### Filtering

Objects can be filtered by property or reference values. In the latter case you need to pass the "path" to the property in the referenced collection.

```java
.where(Where.property("year").gte(1969))
.where(Where.reference("hasAwards", "GrammyAwards", "category").eq("New Artist"))
```

Supported **comparison operators**:

- Equal: `.eq`
- NotEqual: `.ne`
- LessThan: `.lt`
- LessThanEqual: `.lte`
- GreaterThan: `.gt`
- GreaterThanEqual: `.gte`
- Like: `.like`
- ContainsAll: `.containsAll`
- ContainsAny: `.containsAny`
- WithinGeoRange: `.withinGeoRange`

Comparison operators can be grouped using **logical operators** with arbitrarily deep nesting.

```java
.where(
  Where.or(
    Where.and(
      Where.property("year").gt(2000),
      Where.property("year").lt(2017)
    ),
    Where.or(
      Where.property("artist").like("Boys"),
      Where.property("genres").containsAny("#rock", "#rocknroll", "#grunge")
    )
  )
)
```

Operators passed in subsequent calls to `.where` are concatenated with the `.and` operartor.
These 3 calls are equivalent:

```java
q -> q.where(Where.and(cond1, cond2))
q -> q.where(cond1, cond2)
q -> q.where(cond1).where(cond2)
```

Passing `null` and and empty `Where[]` to any of the logical operators as well as to the `.where()` method is safe -- the empty operators will simply be ignored.

#### Grouping results

Every query above has an overloaded variant that accepts a group-by clause.

```java
songs.query.nearVector(new float[]{...}, GroupBy.property("artist", 10, 100)); // Required arguments + GroupBy
songs.query.bm25("rain", bm25 -> bm25.queryProperties("lyrics"), GroupBy.property("artist", 10, 100)); // Required argument, optional parameters, GroupBy
```

The shape of the response object is different too, see [`QueryResponseGrouped`](./src/main/java/io/weaviate/client6/v1/api/collections/query/QueryResponseGrouped.java).

### Pagination

Paginating a Weaviate collection is straighforward and the API should is instantly familiar. `CursorSpliterator` powers 2 patterns for iterating over objects:
- the default Paginator object returned by `collection.paginate()` implements Iterable that can be used in a traditional for-loop
- `.stream()` presents the internal Spliterator via an idiomatic Stream API

```java
var allSongs = songs.paginate();

for (WeaviateObject song : allSongs) {
    // Traditional for-loop
}

// Stream API
var allSongUUIDs = allSongs.stream().map(WeaviateObject::uuid).toList();
```

Paginator can be configured to return a subset of properties / metadata fields, use a different page size (defaults to 100) or resume iteration from an arbitrary object.

```java
// Create a paginator
var allSongs = things.paginate(
  p -> p
    .pageSize(10)
    .resumeFrom("uuid-3")
    .returnProperties("artist", "album")
    .returnMetadata(Metadata.VECTOR));

// Process data
allSongs.stream().toList();
```

### Aggregating data

```java
songs.aggregate.overAll(
    with -> with
        .metrics(
            Aggregate.integer("year", calc -> calc.min().max().median()),
            Aggregate.text("album", calc -> calc.topOccurrences().topOccurencesCutoff(5)),
            Aggregate.bool("isSingle", calc -> calc.percentageTrue().totalFalse()),
            Aggregate.number("monthlyListeners", calc -> calc.mode().count())
        )
        .includeTotalCount(true)
)
```

#### Filtered aggregations

To perform aggregations over query results, use one of the other method under the `aggregate` namespace: it has the same set of methods as the `query` namespace, with the exception of `.bm25(...)`, which cannot be used as an aggregation filter.

```java
songs.aggregate.hybrid(
    "summer of love",
    hybrid -> hybrid
        .queryProperties("title", "lyrics")
        .nearVector(NearVector.of(
            new float[]{...},
            nv -> nv.certainty(.7f)
        )
        .alpha(.3f),
    aggregate -> aggregate
        .metrics(
            Aggregate.text("artist", calc -> calc.topOccurrences())
        )
);
```

Similartly, an overload with another parameter for a group-by clause is available:

```java
songs.aggregate.nearObject(
    yellowSubmarine.uuid(),
    nearObject -> nearObject.excludeSelf(),
    aggregate -> aggregate.metrics(
        Aggregate.text("album", calc -> calc.topOccurrences())
    ),
    GroupBy.property("year")
);
```

#### Counting collection objects

To query the total object count in a collection use `songs.size()` shorthand.


### Error handling

The client throws exceptions extending `WeaviateException`, which can be used as a catch-all case for any package-related exceptions. Other exception types, such as `IOException` which may be thrown by the underlying HTTP / gRPC libraries are allowed to propagate, as they usually signal different kinds of errors: malformed URL, network problems, etc.

`WeaviateException` is an **unchecked exception**.

```java
try (final var client = WeaviateClient.connectToLocal()) {
    // Make some requests
} catch (WeaviateException | IOException e) {
    e.printStackTrace();
}
```

Concrete exception types:

- `WeaviateApiException` - Bad request.
- `PaginationException` - Wrapper exception with pagination details (page size, last cursor UUID)
- `WeaviateConnectException` - Weaviate instance not available, failed to connect.
- `WeaviateOAuthException` - Error during OAuth credentials exchange.
- `WeaviateTransportException` - Internal transport layer exception.

### ORM

Weaviate client comes with a minimal ORM, which lets you serialize and deserialize object properties into Java **records**. Moreover, the client can create a collection based on the record's declared fields.
The "Songs" collection that we've been working with so far may look somethins like this:


```java
import io.weaviate.client6.v1.api.collections.annotations.Collection;
import io.weaviate.client6.v1.api.collections.annotations.Property;

@Collection("Songs", description = "Global media library")
record Song(
  String title,
  String lyrics,
  @Property("artist") String singer,
  int year,
  String[] genres
) {}
```

By default, the class and field names map to the collection and property names respectively. The `@Collection` and `@Property` annotations can be used to override those defaults.
To create the collection, pass the class definition to `.create`.

```java
client.collections.create(
  Song.class,
  collection -> collection
    .references(...)
    .vectorConfig(...);
```

Ingestion and search work the same way, but will accept / return `Song.class` instances instead of `Map<String, Object>`.

```java
Song trust = new Song("Bad", "...", "Michael Jackson", 1987, ...);
Song badGuy = new Song("Bad Guy", "...", "Billie Eilish", 2019, ...);
Song crown = new Song("You Should See Me in a Crown", "...", "Billie Eilish", 2019, ...);

songs.data.insert(trust);
songs.data.insertMany(badGuy, crown);

var result = songs.query.bm25("bad", opt -> opt.queryProperties("lyrics").returnProperties("artist"));

for (var song : result.objects()) {
  System.out.println(song.properties().artist());
}
```

We want to stress that this ORM's focus is on improving type-safety around object properties and simplifying de-/serialization. The ORM is intentionally kept minimal and as such has the following limitations:

- **Does not support BLOB properties.** On the wire, blob properties are represented as base64-encoded strings, and both logically map to the Java's `String`. Presently there isn't a good way for the client to deduce which property type should be created, so it always maps `Sting -> TEXT`.
- **Limited configuration options.** Vector indices, replication, multi-tenancy, and such need to be configured via a tucked builder in `.create(..., here -> here)`.
- **Does not support cross-references.** Properties and Cross-References are conceptually and "physically" separated in Weaviate' client libraries, so doing something like in the snippet below is not supported.

```java
record Artist(String firstName, String lastName, int age) {};

record Song(String title, Artist artist) {};

var song1 = songs.query.byId(uuid1, song -> song.returnReferences(QueryReference.single("artist")));
System.out.println("Artist's last name is: " + song1.properties().artist().lastName());
```

Instead you'd work with cross-references same way as without the ORM:

```java
System.out.println("Artist's last name is: " + song1.references().get("artist").properties().get("lastName"));
```

Some of these features may be added in future releases.

### Collection alias

```java
client.collections.alias("RapSongs", "Songs_Alias");
client.collections.list(only -> only.collection("RapSongs"));
client.collections.get("Songs_Alias");
client.collections.update("Songs_Alias", "PopSongs");
client.collections.delete("Songs_Alias");
```

## Useful resources

- [Documentation](https://weaviate.io/developers/weaviate/current/client-libraries/java.html).
- [StackOverflow for questions about Weaviate](https://stackoverflow.com/questions/tagged/weaviate).
- [Github for issues in client6](https://github.com/weaviate/java-client/issues).

## Contributing

- [How to Contribute](https://github.com/weaviate/java-client/blob/main/CONTRIBUTE.md).

