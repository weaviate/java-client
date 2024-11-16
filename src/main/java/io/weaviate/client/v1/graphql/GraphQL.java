package io.weaviate.client.v1.graphql;

import io.weaviate.client.v1.graphql.query.Aggregate;
import io.weaviate.client.v1.graphql.query.Explore;
import io.weaviate.client.v1.graphql.query.Get;
import io.weaviate.client.v1.graphql.query.Raw;
import io.weaviate.client.v1.graphql.query.argument.AskArgument;
import io.weaviate.client.v1.graphql.query.argument.Bm25Argument;
import io.weaviate.client.v1.graphql.query.argument.GroupArgument;
import io.weaviate.client.v1.graphql.query.argument.GroupByArgument;
import io.weaviate.client.v1.graphql.query.argument.HybridArgument;
import io.weaviate.client.v1.graphql.query.argument.NearAudioArgument;
import io.weaviate.client.v1.graphql.query.argument.NearDepthArgument;
import io.weaviate.client.v1.graphql.query.argument.NearImageArgument;
import io.weaviate.client.v1.graphql.query.argument.NearImuArgument;
import io.weaviate.client.v1.graphql.query.argument.NearObjectArgument;
import io.weaviate.client.v1.graphql.query.argument.NearTextArgument;
import io.weaviate.client.v1.graphql.query.argument.NearTextMoveParameters;
import io.weaviate.client.v1.graphql.query.argument.NearThermalArgument;
import io.weaviate.client.v1.graphql.query.argument.NearVectorArgument;
import io.weaviate.client.v1.graphql.query.argument.NearVideoArgument;
import io.weaviate.client.v1.graphql.query.argument.SortArgument;
import io.weaviate.client.Config;
import io.weaviate.client.base.http.HttpClient;

public class GraphQL {
  private Config config;
  private HttpClient httpClient;

  public static class Arguments {
    public NearTextArgument.NearTextArgumentBuilder nearTextArgBuilder() {
      return NearTextArgument.builder();
    }

    public Bm25Argument.Bm25ArgumentBuilder bm25ArgBuilder() {
      return Bm25Argument.builder();
    }

    public HybridArgument.HybridArgumentBuilder hybridArgBuilder() {
      return HybridArgument.builder();
    }

    public AskArgument.AskArgumentBuilder askArgBuilder() {
      return AskArgument.builder();
    }

    public NearTextMoveParameters.NearTextMoveParametersBuilder nearTextMoveParameterBuilder() {
      return NearTextMoveParameters.builder();
    }

    public NearObjectArgument.NearObjectArgumentBuilder nearObjectArgBuilder() {
      return NearObjectArgument.builder();
    }

    public NearVectorArgument.NearVectorArgumentBuilder nearVectorArgBuilder() {
      return NearVectorArgument.builder();
    }

    public NearImageArgument.NearImageArgumentBuilder nearImageArgBuilder() {
      return NearImageArgument.builder();
    }

    public NearAudioArgument.NearAudioArgumentBuilder nearAudioArgBuilder() {
      return NearAudioArgument.builder();
    }

    public NearVideoArgument.NearVideoArgumentBuilder nearVideoArgBuilder() {
      return NearVideoArgument.builder();
    }

    public NearDepthArgument.NearDepthArgumentBuilder nearDepthArgBuilder() {
      return NearDepthArgument.builder();
    }

    public NearThermalArgument.NearThermalArgumentBuilder nearThermalArgBuilder() {
      return NearThermalArgument.builder();
    }

    public NearImuArgument.NearImuArgumentBuilder nearImuArgBuilder() {
      return NearImuArgument.builder();
    }

    public GroupArgument.GroupArgumentBuilder groupArgBuilder() {
      return GroupArgument.builder();
    }

    public SortArgument.SortArgumentBuilder sortArgBuilder() {
      return SortArgument.builder();
    }

    public GroupByArgument.GroupByArgumentBuilder groupByArgBuilder() {
      return GroupByArgument.builder();
    }
  }

  public GraphQL(HttpClient httpClient, Config config) {
    this.config = config;
    this.httpClient = httpClient;
  }

  public Get get() {
    return new Get(httpClient, config);
  }

  public Raw raw() {
    return new Raw(httpClient, config);
  }

  public Explore explore() {
    return new Explore(httpClient, config);
  }

  public Aggregate aggregate() {
    return new Aggregate(httpClient, config);
  }

  public GraphQL.Arguments arguments() {
    return new GraphQL.Arguments();
  }
}
