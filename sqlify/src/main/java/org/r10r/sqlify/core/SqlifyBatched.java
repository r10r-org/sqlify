package org.r10r.sqlify.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public final class SqlifyBatched {

  private final String sqlForJdbc;
  private final List<Batch> parameterMapBatches;
  private final List<String> parametersInSqlSorted;

  public SqlifyBatched(
      String userProvidedSqlWithPlaceholder, 
      List<Batch> parameterMapBatches) {
    
    if (parameterMapBatches.isEmpty()) {
      throw new IllegalArgumentException("You have to provide at least one batch when using batched sql mode...");
    }

    this.parameterMapBatches = parameterMapBatches;
    this.parametersInSqlSorted = SqlifyCore.extractParameterNames(userProvidedSqlWithPlaceholder);
    this.sqlForJdbc = SqlifyCore.convertNamedParametersIntoJdbcCompliantPreparedStatement(userProvidedSqlWithPlaceholder);
  }
  
  public int [] executeUpdateBatch(Connection connection) {
    try (PreparedStatement preparedStatement = connection.prepareStatement(sqlForJdbc)) {
      
        for (Batch batch: parameterMapBatches) {
          SqlifyCore.applyParameterMapToPreparedStatement(preparedStatement, batch.getParameterMap(), parametersInSqlSorted);
          preparedStatement.addBatch();
        }
    
      int [] numberOfChangedLines = preparedStatement.executeBatch();
      return numberOfChangedLines;
    } catch (SQLException sqlException) {
      throw new SqlifyException("Ops. Something strange happened " + sqlException, sqlException);
    }
  }

}
