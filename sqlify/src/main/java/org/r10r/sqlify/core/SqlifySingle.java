package org.r10r.sqlify.core;

import org.r10r.sqlify.resultparser.ResultParser;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import org.r10r.sqlify.SqlifyException;

public final class SqlifySingle {

  private final String sqlForJdbc;
  private final ResultParser<?> resultParser;
  private final Map<String, Object> parameterMap;
  private final List<String> parametersInSqlSorted;

  public SqlifySingle(
      String userProvidedSqlWithPlaceholder, 
      ResultParser<?> resultParser, 
      Map<String, Object> parameterMap) {
    this.resultParser = resultParser;
    this.parameterMap = parameterMap;
    this.parametersInSqlSorted = SqlifyCore.extractParameterNames(userProvidedSqlWithPlaceholder);
    this.sqlForJdbc = SqlifyCore.convertNamedParametersIntoJdbcCompliantPreparedStatement(userProvidedSqlWithPlaceholder);
 
     SqlifyCore.verifyThatAllNeededParametersAreProvidedByUser(parameterMap, parametersInSqlSorted);
  }

  public <T> T executeSelect(Connection connection) {
    try (PreparedStatement preparedStatement = connection.prepareStatement(sqlForJdbc)) {
      SqlifyCore.applyParameterMapToPreparedStatement(preparedStatement, parameterMap, parametersInSqlSorted);
      
      ResultSet resultSet = preparedStatement.executeQuery();

      T t = resultParser.parseResultSet(resultSet);
      return t;
    } catch (Exception exception) {
      throw new SqlifyException("Ops. Something strange happened: " + exception.getMessage(), exception);
    }
  }

  public int executeUpdate(Connection connection) {
    try (PreparedStatement preparedStatement = connection.prepareStatement(sqlForJdbc)) {
      
      SqlifyCore.applyParameterMapToPreparedStatement(preparedStatement, parameterMap, parametersInSqlSorted);
        
      int numberOfChangedLines = preparedStatement.executeUpdate();
      return numberOfChangedLines;
    } catch (SQLException sqlException) {
      throw new SqlifyException("Ops. Something strange happened:  " + sqlException.getMessage(), sqlException);
    }
  }
  
  public <T> T executeUpdateAndReturnGeneratedKey(Connection connection) {
    
    if (resultParser == null) {
      throw new SqlifyException("Arg. I don't know how to parse the generated key. Please specify result parser. Example: '.parseResultWith(SingleResultParser.of(Long.class))'");
    }
    
    try (PreparedStatement preparedStatement = connection.prepareStatement(
          sqlForJdbc, 
          Statement.RETURN_GENERATED_KEYS)) {
      
      SqlifyCore.applyParameterMapToPreparedStatement(preparedStatement, parameterMap, parametersInSqlSorted);
      
      preparedStatement.executeUpdate();
      ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
      return resultParser.parseResultSet(generatedKeys);
    } catch (Exception exception) {
      throw new SqlifyException("Ops. Something strange happened: " + exception.getMessage(), exception);
    }
    
  }
  
}
