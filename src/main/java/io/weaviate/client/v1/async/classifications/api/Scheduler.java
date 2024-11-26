package io.weaviate.client.v1.async.classifications.api;

import io.weaviate.client.Config;
import io.weaviate.client.base.AsyncBaseClient;
import io.weaviate.client.base.AsyncClientResult;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.util.Futures;
import io.weaviate.client.v1.classifications.model.Classification;
import io.weaviate.client.v1.classifications.model.ClassificationFilters;
import io.weaviate.client.v1.filters.WhereFilter;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.core5.concurrent.FutureCallback;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;

public class Scheduler extends AsyncBaseClient<Classification>
  implements AsyncClientResult<Classification> {

  private static final long WAIT_INTERVAL = 2000;

  private String classificationType;
  private String className;
  private String[] classifyProperties;
  private String[] basedOnProperties;
  private WhereFilter sourceWhereFilter;
  private WhereFilter trainingSetWhereFilter;
  private WhereFilter targetWhereFilter;
  private boolean waitForCompletion;
  private Object settings;

  private final Getter getter;
  private final Executor executor;


  public Scheduler(CloseableHttpAsyncClient client, Config config, Getter getter, Executor executor) {
    super(client, config);
    this.getter = getter;
    this.executor = executor;
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


  @Override
  public Future<Result<Classification>> run(FutureCallback<Result<Classification>> callback) {
    if (waitForCompletion) {
      return scheduleAndWaitForCompletion(callback);
    }
    return schedule(callback);
  }

  private Future<Result<Classification>> schedule(FutureCallback<Result<Classification>> callback) {
    Classification config = Classification.builder()
      .basedOnProperties(basedOnProperties)
      .className(className)
      .classifyProperties(classifyProperties)
      .type(classificationType)
      .settings(settings)
      .filters(getClassificationFilters(sourceWhereFilter, targetWhereFilter, trainingSetWhereFilter))
      .build();
    return sendPostRequest("/classifications", config, Classification.class, callback);
  }

  private Future<Result<Classification>> scheduleAndWaitForCompletion(FutureCallback<Result<Classification>> callback) {
    CompletableFuture<Result<Classification>> future = new CompletableFuture<>();
    FutureCallback<Result<Classification>> internalCallback = new FutureCallback<Result<Classification>>() {
      @Override
      public void completed(Result<Classification> classificationResult) {
        future.complete(classificationResult);
      }

      @Override
      public void failed(Exception e) {
        future.completeExceptionally(e);
      }

      @Override
      public void cancelled() {
        future.cancel(true);
        if (callback != null) {
          callback.cancelled(); // TODO:AL propagate cancel() call from future to completable future
        }
      }
    };

    schedule(internalCallback);

    return future.thenCompose(classificationResult -> {
        if (classificationResult.hasErrors()) {
          return CompletableFuture.completedFuture(classificationResult);
        }
        return getByIdRecursively(classificationResult.getResult().getId());
      })
      .whenComplete((classificationResult, throwable) -> {
        if (callback != null) {
          if (throwable != null) {
            callback.failed((Exception) throwable);
          } else {
            callback.completed(classificationResult);
          }
        }
      });
  }

  private CompletableFuture<Result<Classification>> getById(String id) {
    CompletableFuture<Result<Classification>> future = new CompletableFuture<>();
    getter.withID(id).run(new FutureCallback<Result<Classification>>() {
      @Override
      public void completed(Result<Classification> classificationResult) {
        future.complete(classificationResult);
      }

      @Override
      public void failed(Exception e) {
        future.completeExceptionally(e);
      }

      @Override
      public void cancelled() {
      }
    });
    return future;
  }

  private CompletableFuture<Result<Classification>> getByIdRecursively(String id) {
    return Futures.thenComposeAsync(getById(id), classificationResult -> {
      boolean isRunning = Optional.ofNullable(classificationResult)
        .map(Result::getResult)
        .map(Classification::getStatus)
        .filter(status -> status.equals("running"))
        .isPresent();

      if (isRunning) {
        try {
          return Futures.supplyDelayed(() -> getByIdRecursively(id), WAIT_INTERVAL, executor);
        } catch (InterruptedException e) {
          throw new CompletionException(e);
        }
      }
      return CompletableFuture.completedFuture(classificationResult);
    }, executor);
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
