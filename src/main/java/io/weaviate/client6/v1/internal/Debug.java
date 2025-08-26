package io.weaviate.client6.v1.internal;

import java.util.function.Consumer;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageOrBuilder;
import com.google.protobuf.util.JsonFormat;

/** Debug utilities. */
public final class Debug {
  public static final void printProto(Object proto) {
    System.out.println(proto2json((MessageOrBuilder) proto));
  }

  public static final void printProto(Object proto, String message, Object... args) {
    System.out.println(message.formatted(args) + ": " + proto2json((MessageOrBuilder) proto));
  }

  private static final String proto2json(MessageOrBuilder proto) {
    String out;
    try {
      out = JsonFormat.printer().print(proto);
    } catch (InvalidProtocolBufferException e) {
      out = e.getMessage();
    }

    return out;
  }

  /**
   * Write build info to an output. See {@link #printBuildInfo}.
   *
   * <p>
   * Usage:
   *
   * <pre>{@code
   * // Log to stdout
   * Debug.writeBuildInfo(System.out::println);
   *
   * // Write to custom logger
   * Debug.writeBuildInfo(mylog::info);
   * }</pre>
   *
   * @param writer Output writer.
   */
  public static final void writeBuildInfo(Consumer<String> writer) {
    writer.accept("[io.weaviate.client6.v1.internal.BuildInfo] branch=%s commit_id=%s"
        .formatted(BuildInfo.BRANCH, BuildInfo.COMMIT_ID_ABBREV));
  }

  /** Print build info to stdout. */
  public static final void printBuildInfo() {
    writeBuildInfo(System.out::println);
  }
}
