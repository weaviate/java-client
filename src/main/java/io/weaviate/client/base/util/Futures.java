package io.weaviate.client.base.util;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class Futures {

  private Futures() {
  }

  public static <T> CompletableFuture<T> supplyDelayed(Supplier<CompletableFuture<T>> supplier, long millis,
                                                       Executor executor) throws InterruptedException {
    if (executor instanceof ScheduledExecutorService) {
      return CompletableFuture.supplyAsync(
        supplier,
        command -> ((ScheduledExecutorService) executor).schedule(command, millis, TimeUnit.MILLISECONDS)
      ).thenCompose(f -> f);
    }
    Thread.sleep(millis);
    return supplier.get();
  }

  public static <T, U> CompletableFuture<U> thenComposeAsync(CompletableFuture<T> future, Function<T, CompletableFuture<U>> callback,
                                                             Executor executor) {
    if (executor != null) {
      return future.thenComposeAsync(callback, executor);
    }
    return future.thenComposeAsync(callback);
  }

  public static <T> CompletableFuture<T> handleAsync(CompletableFuture<T> future, BiFunction<T, Throwable, CompletableFuture<T>> callback,
                                                     Executor executor) {
    if (executor != null) {
      return future.handleAsync(callback, executor).thenCompose(f -> f);
    }
    return future.handleAsync(callback).thenCompose(f -> f);
  }

  public static <T> CompletableFuture<T> supplyAsync(Supplier<T> supplier, Executor executor) {
    if (executor != null) {
      return CompletableFuture.supplyAsync(supplier, executor);
    }
    return CompletableFuture.supplyAsync(supplier);
  }
}
