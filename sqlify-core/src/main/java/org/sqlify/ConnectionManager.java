package org.sqlify;

import java.sql.Connection;
import javax.sql.DataSource;

public class ConnectionManager<T> {

  private final Connection connection;

  public static interface Executable<T> {
    T execute(Connection connection);
  }

  public static <E> E withConnection(DataSource dataSource, Executable<E> executable) {
    return new ConnectionManager<E>(dataSource).withConnection(executable);
  }

  public static <E> E withTransaction(DataSource dataSource, Executable<E> executable) {
    return new ConnectionManager<E>(dataSource).withTransaction(executable);
  }

  public static <E> E withTransaction(Connection connection, Executable<E> executable) {
    return new ConnectionManager<E>(connection).withTransaction(executable);
  }

  private ConnectionManager(DataSource dataSource) {
    try {
      this.connection = dataSource.getConnection();
    } catch (Exception e) {
      throw new SqlifyException(e);
    }
  }

  private ConnectionManager(Connection connection) {
    this.connection = connection;
  }

  private T withConnection(Executable<T> executable) {
    return withConnection(true, executable);
  }

  private T withConnection(boolean autocommit, Executable<T> executable) {
    try {
      connection.setAutoCommit(autocommit);
      return executable.execute(connection);
    } catch (Exception e) {
      throw new SqlifyException(e);
    }
  }

  private T withTransaction(Executable<T> executeable) {
    return withConnection(false, connection -> {
      try {
        T t = executeable.execute(connection);
        connection.commit();
        return t;

      } catch (Exception e1) {
        try {
          connection.rollback();
        } catch (Exception e2) {
          throw new SqlifyException(e2);
        }

        throw new SqlifyException(e1);
      }

    });

  }
}
