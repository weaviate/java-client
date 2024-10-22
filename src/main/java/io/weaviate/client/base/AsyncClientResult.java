package io.weaviate.client.base;

import java.util.concurrent.Future;
import org.apache.hc.core5.concurrent.FutureCallback;

public interface AsyncClientResult<T> {
  Future<Result<T>> run();
  Future<Result<T>> run(FutureCallback<Result<T>> callback);
}
