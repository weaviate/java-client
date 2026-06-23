package io.weaviate.client6.v1.api.export;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import com.google.gson.annotations.SerializedName;

import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.json.JSON;
import io.weaviate.client6.v1.internal.rest.Endpoint;
import io.weaviate.client6.v1.internal.rest.SimpleEndpoint;

public record CreateExportRequest(ExportCreate body, String backend) {

  public static Endpoint<CreateExportRequest, Export> _ENDPOINT = new SimpleEndpoint<>(
      request -> "POST",
      request -> "/export/" + request.backend,
      request -> Collections.emptyMap(),
      request -> JSON.serialize(request.body),
      (statusCode, response) -> JSON.deserialize(response, Export.class));

  public static record ExportCreate(
      @SerializedName("id") String id,
      @SerializedName("file_format") FileFormat fileFormat,
      @SerializedName("include") List<String> includeCollections,
      @SerializedName("exclude") List<String> excludeCollections) {

    public static ExportCreate of(String exportId) {
      return of(exportId, ObjectBuilder.identity());
    }

    public static ExportCreate of(String exportId, Function<Builder, ObjectBuilder<ExportCreate>> fn) {
      return fn.apply(new Builder(exportId)).build();
    }

    public ExportCreate(Builder builder) {
      this(
          builder.exportId,
          builder.fileFormat,
          builder.includeCollections,
          builder.excludeCollections);
    }

    public static class Builder implements ObjectBuilder<ExportCreate> {
      private final String exportId;

      private FileFormat fileFormat = FileFormat.PARQUET;
      private final List<String> includeCollections = new ArrayList<>();
      private final List<String> excludeCollections = new ArrayList<>();

      public Builder(String exportId) {
        this.exportId = exportId;
      }

      /** Collection that should be included in the export. */
      public Builder includeCollections(String... includeCollections) {
        return includeCollections(Arrays.asList(includeCollections));
      }

      /** Collection that should be included in the export. */
      public Builder includeCollections(List<String> includeCollections) {
        this.includeCollections.addAll(includeCollections);
        return this;
      }

      /** Collection that should be excluded from the export. */
      public Builder excludeCollections(String... excludeCollections) {
        return excludeCollections(Arrays.asList(excludeCollections));
      }

      /** Collection that should be excluded from the export. */
      public Builder excludeCollections(List<String> excludeCollections) {
        this.excludeCollections.addAll(excludeCollections);
        return this;
      }

      /** Export file format. */
      public Builder fileFormat(FileFormat fileFormat) {
        this.fileFormat = fileFormat;
        return this;
      }

      @Override
      public ExportCreate build() {
        return new ExportCreate(this);
      }
    }
  }
}
