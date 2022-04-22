package technology.semi.weaviate.client.v1.graphql;

import technology.semi.weaviate.client.Config;
import technology.semi.weaviate.client.v1.graphql.query.Aggregate;
import technology.semi.weaviate.client.v1.graphql.query.Explore;
import technology.semi.weaviate.client.v1.graphql.query.Get;
import technology.semi.weaviate.client.v1.graphql.query.argument.AskArgument;
import technology.semi.weaviate.client.v1.graphql.query.argument.GroupArgument;
import technology.semi.weaviate.client.v1.graphql.query.argument.NearImageArgument;
import technology.semi.weaviate.client.v1.graphql.query.argument.NearObjectArgument;
import technology.semi.weaviate.client.v1.graphql.query.argument.NearTextArgument;
import technology.semi.weaviate.client.v1.graphql.query.argument.NearTextMoveParameters;
import technology.semi.weaviate.client.v1.graphql.query.argument.SortArgument;
import technology.semi.weaviate.client.v1.graphql.query.argument.SortArguments;

public class GraphQL {
  private Config config;

  public class Arguments {
    public NearTextArgument.NearTextArgumentBuilder nearTextArgBuilder() {
      return NearTextArgument.builder();
    }
    public NearTextMoveParameters.NearTextMoveParametersBuilder nearTextMoveParameterBuilder() {
      return NearTextMoveParameters.builder();
    }
    public NearObjectArgument.NearObjectArgumentBuilder nearObjectArgBuilder() {
      return NearObjectArgument.builder();
    }
    public AskArgument.AskArgumentBuilder askArgBuilder() {
      return AskArgument.builder();
    }
    public NearImageArgument.NearImageArgumentBuilder nearImageArgBuilder() {
      return NearImageArgument.builder();
    }
    public GroupArgument.GroupArgumentBuilder groupArgBuilder() {
      return GroupArgument.builder();
    }
    public SortArgument.SortArgumentBuilder sortArgBuilder() {
      return SortArgument.builder();
    }
  }

  public GraphQL(Config config) {
    this.config = config;
  }

  public Get get() {
    return new Get(config);
  }

  public Explore explore() {
    return new Explore(config);
  }

  public Aggregate aggregate() {
    return new Aggregate(config);
  }

  public GraphQL.Arguments arguments() {
    return new GraphQL.Arguments();
  }
}
