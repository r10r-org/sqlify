package org.sqlify;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;

public class Database {
  
  private DataSource dataSource;

  public static interface Executable<T> {
    T execute(Connection connection);
  }

  private Database(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  public <T> T withConnection(Executable<T> block) {
    return withConnection(true, block);
  }

  public <T> T withConnection(boolean autocommit, Executable<T> block) {
    Connection connection = null;
    try {
      connection = this.dataSource.getConnection();
      connection.setAutoCommit(autocommit);
      return block.execute(connection);
    } catch (Exception e) {
      throw new SqlifyException(e);
    } finally {
      try {
        if (connection != null) {
          connection.close();
        }
      } catch (SQLException ex) {
       throw new SqlifyException(ex);
      }
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
  public static Database use(DataSource dataSource) {
    Database database = new Database(dataSource);
    return database;
  }
  
}
