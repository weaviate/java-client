package io.weaviate.integration;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.BeforeClass;
import org.junit.Test;

import io.weaviate.ConcurrentTest;
import io.weaviate.client6.v1.api.WeaviateClient;
import io.weaviate.client6.v1.api.export.Export;
import io.weaviate.client6.v1.api.export.ExportStatus;
import io.weaviate.client6.v1.api.export.FileFormat;
import io.weaviate.client6.v1.api.export.ShardExportProgress;
import io.weaviate.containers.Weaviate;

public class ExportITest extends ConcurrentTest {
  private static final WeaviateClient client = Weaviate.custom()
      .withExportPath("/tmp/export").build()
      .getClient();

  @BeforeClass
  public static void __() {
    Weaviate.Version.V137.orSkip();
  }

  @Test
  public void test_lifecycle() throws IOException, TimeoutException {
    // Arrange
    String nsA = ns("A"), nsB = ns("B"), nsC = ns("C");
    String exportId = ns("export_1").toLowerCase();
    String backend = "filesystem";

    var collectionA = client.collections.create(nsA);
    var collectionB = client.collections.create(nsB);
    var collectionC = client.collections.create(nsC);

    // Insert some data
    for (var c : List.of(collectionA, collectionB, collectionC)) {
      var resp = c.data.insertMany(Map.of(), Map.of(), Map.of());
      Assertions.assertThat(resp.errors()).isEmpty();
    }

    // Act: start export
    var started = client.export.create(exportId, backend, FileFormat.PARQUET,
        export -> export
            .includeCollections(nsA, nsB));

    // Assert
    Assertions.assertThat(started)
        .as("created export operation")
        .returns(exportId, Export::id)
        .returns(backend, Export::backend)
        .returns(ExportStatus.STARTED, Export::status)
        .returns(null, Export::error)
        .extracting(Export::includesCollections, InstanceOfAssertFactories.list(String.class))
        .containsOnly(nsA, nsB);

    // Act: await export competion
    var completed = started.waitForCompletion(client);

    // Assert
    Assertions.assertThat(completed)
        .as("await export completion")
        .returns(exportId, Export::id)
        .returns(backend, Export::backend)
        .returns(ExportStatus.SUCCESS, Export::status)
        .returns(null, Export::error)
        .extracting(Export::includesCollections, InstanceOfAssertFactories.list(String.class))
        .containsOnly(nsA, nsB);

    Assertions.assertThat(completed)
        .extracting(Export::shardStatus, InstanceOfAssertFactories.map(String.class,
            Object.class))
        .allSatisfy((__, shards) -> {
          Assertions.assertThat(shards)
              .asInstanceOf(InstanceOfAssertFactories.map(String.class, ShardExportProgress.class))
              .isNotEmpty();
        });
  }
}
