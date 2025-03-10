
package io.weaviate.client6.v1.data;

import java.util.function.Consumer;

import io.weaviate.client6.v1.Vectors;

public class Metadata {
  public final String id;
  public final Vectors vectors;

  Metadata(String id, Vectors vectors) {
    this(m -> m.id(id).vectors(vectors));
  }

  Metadata(Consumer<Options> options) {
    var opt = new Options(options);

    this.id = opt.id;
    this.vectors = opt.vectors;
  }

  public static class Options {
    public String id;
    public Vectors vectors;

    public Options id(String id) {
      this.id = id;
      return this;
    }

    public Options vectors(Vectors vectors) {
      this.vectors = vectors;
      return this;
    }

    public Options vectors(Vectors.Builder vectors) {
      return vectors(vectors.build());
    }

    Options(Consumer<Options> options) {
      options.accept(this);
    }
  }
}
