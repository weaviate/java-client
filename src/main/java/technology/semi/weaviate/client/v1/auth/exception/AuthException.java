package technology.semi.weaviate.client.v1.auth.exception;

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
