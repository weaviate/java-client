package technology.semi.weaviate.client.v1.auth.provider;

public class AuthException extends Exception {
  public AuthException(String message) {
    super(message);
  }

  public AuthException(Throwable cause) {
    super(cause);
  }

  public AuthException(String message, Throwable cause) {
    super(message, cause);
  }
}
