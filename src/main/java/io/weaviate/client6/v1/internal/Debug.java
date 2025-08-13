package io.weaviate.client6.v1.internal;

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
}
