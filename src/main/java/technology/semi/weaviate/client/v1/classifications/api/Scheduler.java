package technology.semi.weaviate.client.v1.classifications.api;

import technology.semi.weaviate.client.Config;
import technology.semi.weaviate.client.base.BaseClient;
import technology.semi.weaviate.client.base.Client;
import technology.semi.weaviate.client.base.Response;
import technology.semi.weaviate.client.v1.classifications.model.Classification;
import technology.semi.weaviate.client.v1.classifications.model.ClassificationFilters;
import technology.semi.weaviate.client.v1.classifications.model.WhereFilter;

public class Scheduler extends BaseClient<Classification> implements Client<Classification> {

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
      Classification runningClassification = getter.withID(id).run();
      if (runningClassification == null) {
        return null;
      }
      if (runningClassification.getStatus() == "running") {
        Thread.sleep(2000);
      } else {
        return runningClassification;
      }
    }
  }

  @Override
  public Classification run() {
    Classification config = Classification.builder()
            .basedOnProperties(basedOnProperties)
            .className(className)
            .classifyProperties(classifyProperties)
            .filters(ClassificationFilters.builder()
                    .sourceWhere(sourceWhereFilter)
                    .targetWhere(targetWhereFilter)
                    .trainingSetWhere(trainingSetWhereFilter)
                    .build())
            .type(classificationType)
            .settings(settings)
            .build();
    Response<Classification> resp = sendPostRequest("/classifications", config, Classification.class);
    if (resp.getStatusCode() == 201) {
      if (waitForCompletion) {
        try {
          return waitForCompletion(resp.getBody().getId());
        } catch (InterruptedException e) {
          return null;
        }
      }
      return resp.getBody();
    }
    return null;
  }
}
