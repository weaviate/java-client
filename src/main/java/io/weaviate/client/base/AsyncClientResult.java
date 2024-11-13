package io.weaviate.client.base;

import org.apache.hc.core5.concurrent.FutureCallback;

import java.util.concurrent.Future;

public interface AsyncClientResult<T> {
  default Future<Result<T>> run() {
    return run(null);
  }

  Future<Result<T>> run(FutureCallback<Result<T>> callback);
}
