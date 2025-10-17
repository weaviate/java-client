package io.weaviate.client6.v1.api.collections.generate;

import java.util.List;
import java.util.function.Function;

import io.weaviate.client6.v1.api.collections.query.GenerativeSearch;
import io.weaviate.client6.v1.internal.ObjectBuilder;

public final class Generate {

  public static io.weaviate.client6.v1.api.collections.query.FetchObjects fetchObjects(
      Function<Generate.FetchObjects, ObjectBuilder<io.weaviate.client6.v1.api.collections.query.FetchObjects>> fn) {
    return fn.apply(new Generate.FetchObjects()).build();
  }

  public static io.weaviate.client6.v1.api.collections.query.Bm25 bm25(String query) {
    return bm25(query, ObjectBuilder.identity());
  }

  public static io.weaviate.client6.v1.api.collections.query.Bm25 bm25(String query,
      Function<Bm25, ObjectBuilder<io.weaviate.client6.v1.api.collections.query.Bm25>> fn) {
    return fn.apply(new Generate.Bm25(query)).build();
  }

  public static class Bm25 extends io.weaviate.client6.v1.api.collections.query.Bm25.Builder {

    public Bm25(String query) {
      super(query);
    }

    /**
     * Add generative query prompts.
     * Builders which support this parameter should make the method public.
     *
     * @param fn Lambda expression for optional parameters.
     */
    public final Bm25 generate(Function<GenerativeSearch.Builder, ObjectBuilder<GenerativeSearch>> fn) {
      super.generate(fn);
      return this;
    }
  }

  public static class FetchObjects extends io.weaviate.client6.v1.api.collections.query.FetchObjects.Builder {

    /**
     * Add generative query prompts.
     * Builders which support this parameter should make the method public.
     *
     * @param fn Lambda expression for optional parameters.
     */
    public final FetchObjects generate(Function<GenerativeSearch.Builder, ObjectBuilder<GenerativeSearch>> fn) {
      super.generate(fn);
      return this;
    }
  }

  public static class Hybrid extends io.weaviate.client6.v1.api.collections.query.Hybrid.Builder {

    public Hybrid(String query) {
      super(query);
    }

    /**
     * Add generative query prompts.
     * Builders which support this parameter should make the method public.
     *
     * @param fn Lambda expression for optional parameters.
     */
    public final Hybrid generate(Function<GenerativeSearch.Builder, ObjectBuilder<GenerativeSearch>> fn) {
      super.generate(fn);
      return this;
    }
  }

  public static class NearVector extends io.weaviate.client6.v1.api.collections.query.NearVector.Builder {

    public NearVector(float[] vector) {
      super(vector);
    }

    /**
     * Add generative query prompts.
     * Builders which support this parameter should make the method public.
     *
     * @param fn Lambda expression for optional parameters.
     */
    public final NearVector generate(Function<GenerativeSearch.Builder, ObjectBuilder<GenerativeSearch>> fn) {
      super.generate(fn);
      return this;
    }
  }

  public static class NearText extends io.weaviate.client6.v1.api.collections.query.NearText.Builder {

    public NearText(List<String> concepts) {
      super(concepts);
    }

    /**
     * Add generative query prompts.
     * Builders which support this parameter should make the method public.
     *
     * @param fn Lambda expression for optional parameters.
     */
    public final NearText generate(Function<GenerativeSearch.Builder, ObjectBuilder<GenerativeSearch>> fn) {
      super.generate(fn);
      return this;
    }
  }

  public static class NearObject extends io.weaviate.client6.v1.api.collections.query.NearObject.Builder {

    public NearObject(String uuid) {
      super(uuid);
    }

    /**
     * Add generative query prompts.
     * Builders which support this parameter should make the method public.
     *
     * @param fn Lambda expression for optional parameters.
     */
    public final NearObject generate(Function<GenerativeSearch.Builder, ObjectBuilder<GenerativeSearch>> fn) {
      super.generate(fn);
      return this;
    }
  }
}
