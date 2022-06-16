package technology.semi.weaviate.client.v1.classifications.api;

import org.apache.commons.lang3.ObjectUtils;
import technology.semi.weaviate.client.Config;
import technology.semi.weaviate.client.base.BaseClient;
import technology.semi.weaviate.client.base.ClientResult;
import technology.semi.weaviate.client.base.Response;
import technology.semi.weaviate.client.base.Result;
import technology.semi.weaviate.client.v1.classifications.model.Classification;
import technology.semi.weaviate.client.v1.classifications.model.ClassificationFilters;
import technology.semi.weaviate.client.v1.filters.WhereFilter;

public class Scheduler extends BaseClient<Classification> implements ClientResult<Classification> {

  private String classificationType;
  private String className;
  private String[] classifyProperties;
  private String[] basedOnProperties;
  private WhereFilter sourceWhereFilter;
  private WhereFilter trainingSetWhereFilter;
  private WhereFilter targetWhereFilter;
  private Boolean waitForCompletion;
  private Object settings;

  private Getter getter;

  public Scheduler(Config config) {
    super(config);
    this.getter = new Getter(config);
    this.waitForCompletion = false;
  }

  public Scheduler withType(String classificationType) {
    this.classificationType = classificationType;
    return this;
  }

  public Scheduler withClassName(String className) {
    this.className = className;
    return this;
  }

  public Scheduler withClassifyProperties(String[] classifyProperties) {
    this.classifyProperties = classifyProperties;
    return this;
  }

  public Scheduler withBasedOnProperties(String[] basedOnProperties) {
    this.basedOnProperties = basedOnProperties;
    return this;
  }

  public Scheduler withSourceWhereFilter(WhereFilter whereFilter) {
    this.sourceWhereFilter = whereFilter;
    return this;
  }

  public Scheduler withTrainingSetWhereFilter(WhereFilter whereFilter) {
    this.trainingSetWhereFilter = whereFilter;
    return this;
  }

  public Scheduler withTargetWhereFilter(WhereFilter whereFilter) {
    this.targetWhereFilter = whereFilter;
    return this;
  }

  public Scheduler withSettings(Object settings) {
    this.settings = settings;
    return this;
  }

  public Scheduler withWaitForCompletion() {
    this.waitForCompletion = true;
    return this;
  }

  private Classification waitForCompletion(String id) throws InterruptedException {
    for (; ; ) {
      Result<Classification> result = getter.withID(id).run();
      if (result == null || result.getResult() == null) {
        return null;
      }
      Classification runningClassification = result.getResult();
      if (runningClassification.getStatus() == "running") {
        Thread.sleep(2000);
      } else {
        return runningClassification;
      }
    }
  }

  @Override
  public Result<Classification> run() {
    Classification config = Classification.builder()
      .basedOnProperties(basedOnProperties)
      .className(className)
      .classifyProperties(classifyProperties)
      .type(classificationType)
      .settings(settings)
      .filters(getClassificationFilters(sourceWhereFilter, targetWhereFilter, trainingSetWhereFilter))
      .build();
    Response<Classification> resp = sendPostRequest("/classifications", config, Classification.class);
    if (resp.getStatusCode() == 201) {
      if (waitForCompletion) {
        try {
          Classification c = waitForCompletion(resp.getBody().getId());
          return new Result<>(resp.getStatusCode(), c, null);
        } catch (InterruptedException e) {
          return new Result<>(resp);
        }
      }
      return new Result<>(resp);
    }
    return new Result<>(resp);
  }

  private ClassificationFilters getClassificationFilters(WhereFilter sourceWhere, WhereFilter targetWhere, WhereFilter trainingSetWhere) {
    if (ObjectUtils.anyNotNull(sourceWhere, targetWhere, trainingSetWhere)) {
      return ClassificationFilters.builder()
        .sourceWhere(sourceWhere)
        .targetWhere(targetWhere)
        .trainingSetWhere(trainingSetWhere)
        .build();
    }
    return null;
  }
}
