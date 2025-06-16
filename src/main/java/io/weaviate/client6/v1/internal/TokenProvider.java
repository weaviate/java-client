package io.weaviate.client6.v1.internal;

@FunctionalInterface
public interface TokenProvider {
  Token getToken();

  public record Token(String accessToken) {
  }

  public static TokenProvider staticToken(Token token) {
    return () -> token;
  }
}
