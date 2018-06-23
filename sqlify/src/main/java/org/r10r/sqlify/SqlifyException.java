package org.r10r.sqlify;

public class SqlifyException extends RuntimeException {

  public SqlifyException(String message, Throwable throwable) {
    super(message, throwable);
  }

  public SqlifyException(String message) {
    super(message);
  }

  public SqlifyException(Throwable throwable) {
    super(throwable);
  }

}
