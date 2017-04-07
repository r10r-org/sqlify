package org.sqlify;

import java.sql.Connection;
import javax.sql.DataSource;

public class Database {

  private final Connection connection;

  public static interface Executable<T> {
    T execute(Connection connection);
  }

  private Database(DataSource dataSource) {
    try {
      this.connection = dataSource.getConnection();
    } catch (Exception e) {
      throw new SqlifyException(e);
    }
  }

  private Database(Connection connection) {
    this.connection = connection;
  }

  public <T> T withConnection(Executable<T> block) {
    return withConnection(true, block);
  }

  public <T> T withConnection(boolean autocommit, Executable<T> block) {
    try {
      connection.setAutoCommit(autocommit);
      return block.execute(connection);
    } catch (Exception e) {
      throw new SqlifyException(e);
    }
  }

  public <T> T withTransaction(Executable<T> block) {
    return withConnection(false, connection -> {
      try {
        T t = block.execute(connection);
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
  
  //////////////////////////////////////////////////////////////////////////////
  // Simplified constructor
  //////////////////////////////////////////////////////////////////////////////
  public static Database from(DataSource dataSource) {
    Database connectionManager = new Database(dataSource);
    return connectionManager;
  }
  
}
