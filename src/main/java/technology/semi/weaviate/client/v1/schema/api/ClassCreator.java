package technology.semi.weaviate.client.v1.schema.api;

import technology.semi.weaviate.client.Config;
import technology.semi.weaviate.client.base.BaseClient;
import technology.semi.weaviate.client.base.ClientResult;
import technology.semi.weaviate.client.base.Response;
import technology.semi.weaviate.client.base.Result;
import technology.semi.weaviate.client.v1.misc.model.BM25Config;
import technology.semi.weaviate.client.v1.misc.model.StopwordConfig;
import technology.semi.weaviate.client.v1.schema.model.WeaviateClass;

public class ClassCreator extends BaseClient<WeaviateClass> implements ClientResult<Boolean> {

  private WeaviateClass clazz;

  public ClassCreator(Config config) {
    super(config);
  }

  public ClassCreator withClass(WeaviateClass clazz) {
    this.clazz = clazz;
    return this;
  }

  public ClassCreator withBM25Config(BM25Config config) throws Exception {
    if (this.clazz.getInvertedIndexConfig() == null) {
      throw new Exception("cannot add bm25 to null invertedIndexConfig");
    }

    this.clazz.getInvertedIndexConfig().setBm25Config(config);
    return this;
  }

  public ClassCreator withStopwordConfig(StopwordConfig config) throws Exception {
    if (this.clazz.getInvertedIndexConfig() == null) {
      throw new Exception("cannot add stopwords to null invertedIndexConfig");
    }

    this.clazz.getInvertedIndexConfig().setStopwordConfig(config);
    return this;
  }

  @Override
  public Result<Boolean> run() {
    Response<WeaviateClass> resp = sendPostRequest("/schema", clazz, WeaviateClass.class);
    return new Result<>(resp.getStatusCode(), resp.getStatusCode() == 200, resp.getErrors());
  }
}
