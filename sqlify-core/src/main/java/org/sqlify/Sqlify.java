package org.sqlify;

import org.sqlify.resultparser.ResultParser;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Sqlify<T> {

  private final String sql;
  private final ResultParser<T> resultParser;
  private final LinkedHashMap<String, Object> parameterMap;
  private final List<String> parametersInSqlSorted;

  private Sqlify(String sql, ResultParser<T> resultParser, LinkedHashMap<String, Object> parameterMap) {
    this.sql = sql;
    this.resultParser = resultParser;
    this.parameterMap = parameterMap;
    this.parametersInSqlSorted = extractNameAndPosition(sql);
  }

  private T executeSelect(Connection connection) {
    try {
      PreparedStatement preparedStatement = connection.prepareStatement(sql);
      ResultSet resultSet = preparedStatement.executeQuery();
      T t = resultParser.parseResultSet(resultSet);
      return t;
    } catch (Exception e) {
      throw new SqlifyException("Ops. Something strange happened", e);
    }
  }

  private int executeUpdate(Connection connection) {
    try {
      String convertedPreparedStatement = convertIntoPreparedStatement(sql);
      PreparedStatement preparedStatement = connection.prepareStatement(convertedPreparedStatement);
      applyParameterMapToPreparedStatement(preparedStatement, parameterMap, parametersInSqlSorted);
      int numberOfChangedLines = preparedStatement.executeUpdate();
      return numberOfChangedLines;
    } catch (SQLException ex) {
      throw new SqlifyException("Ops. Something strange happened", ex);
    }
  }
  
  private T executeUpdateAndReturnGeneratedKey(Connection connection) {
    try {
      String convertedPreparedStatement = convertIntoPreparedStatement(sql);
      PreparedStatement preparedStatement = connection.prepareStatement(
          convertedPreparedStatement, 
          Statement.RETURN_GENERATED_KEYS);
      applyParameterMapToPreparedStatement(preparedStatement, parameterMap, parametersInSqlSorted);
      preparedStatement.executeUpdate();
      ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
      return resultParser.parseResultSet(generatedKeys);
    } catch (Exception ex) {
      throw new SqlifyException("Ops. Something strange happened", ex);
    }
  }

  // insert into table(name) values ({name}) => insert into table(name) values (?)
  private String convertIntoPreparedStatement(String sqlWithNamedPlaceholers) {
    return sqlWithNamedPlaceholers.replaceAll("\\{.*?\\}", "?");
  }

  private List<String> extractNameAndPosition(String userSpecifiedSql) {
    List<String> allParametersSorted = new ArrayList<>();

    Pattern p = Pattern.compile("\\{(.*?)\\}");
    Matcher m = p.matcher(userSpecifiedSql);
    while (m.find()) {
      String name = m.group(1);
      allParametersSorted.add(name);
    }

    return allParametersSorted;
  }

  private PreparedStatement applyParameterMapToPreparedStatement(
      PreparedStatement preparedStatement, 
      Map<String, Object> parameterMap, 
      List<String> parametersInSqlSorted) {
    try {
      for (int i = 0; i < parametersInSqlSorted.size(); i++) {
        Object value = parameterMap.get(parametersInSqlSorted.get(i));
        int positionInPreparedStatement = i + 1; // jdbc parameters start with 1...
        
        if (value instanceof String) {
          preparedStatement.setString(positionInPreparedStatement, (String) value);
        } else if (value instanceof Long) {
          preparedStatement.setLong(positionInPreparedStatement, (Long) value);
        } else if (value instanceof Integer) {
          preparedStatement.setInt(positionInPreparedStatement, (Integer) value);
        } else if (value instanceof Timestamp) {
          preparedStatement.setTimestamp(positionInPreparedStatement, (Timestamp) value);
        } else if (value instanceof Time) {
          preparedStatement.setTime(positionInPreparedStatement, (Time) value);
        } else {
          // Add more type conversions here.
          throw new SqlifyException(
              "Ops - Mapping from type to jdbc not (yet) supported: " 
                  + value.getClass().getName()
                  + ". You can help by adding the mapping to the source code.");
        }
      }
    } catch (SQLException ex) {
      throw new SqlifyException("Ops. An error occurred.", ex);
    }
    return preparedStatement;
  }

  ////////////////////////////////////////////////////////////////////////////
  // Builder pattern
  ////////////////////////////////////////////////////////////////////////////
  public static <E> Builder1<E> sql(String sql) {
    return new Builder1<>(sql);
  }

  public static class Builder1<E> {

    private final String sql;
    private final LinkedHashMap<String, Object> parameterMap;
    private ResultParser<E> resultParser;

    public Builder1(String sql) {
      this.sql = sql;
      this.parameterMap = new LinkedHashMap<>();
    }

    public Builder1<E> withParameter(String key, Object value) {
      parameterMap.put(key, value);
      return this;
    }

    public Builder1<E> parseResultWith(ResultParser<E> resultParser) {
      this.resultParser = resultParser;
      return this;
    }

    /**
     * Executes a select. Use 'parseResultWith' to specify a parser that will
     * map the result to nice Java objects.
     * 
     * @param connection The connection to use for this query.
     * @return The result as specified via 'parseResultWith'
     */
    public E executeSelect(Connection connection) {
      Sqlify<E> sqlify = new Sqlify<>(this.sql, this.resultParser, this.parameterMap);
      return sqlify.<E>executeSelect(connection);
    }

    /**
     * Executes an update (insert, delete statement)
     * 
     * @param connection The connection to use for this query.
     * @return The number of lines affected by this query.
     */
    public int executeUpdate(Connection connection) {
      Sqlify<E> sqlify = new Sqlify<>(this.sql, this.resultParser, this.parameterMap);
      return sqlify.<E>executeUpdate(connection);
    }
    
    /**
     * Executes an update (insert, delete statement) and returns the generated
     * key. Define the mapping via a ResultParser. For instance if you expect
     * one Long as result you can use 
     * parseResultWith(SingleResultParser.of(Long.class))
     * 
     * @param connection The connection to use for this query.
     * @return The generated key.
     */
    public E executeUpdateAndReturnGeneratedKey(Connection connection) {
      Sqlify<E> sqlify = new Sqlify<>(this.sql, this.resultParser, this.parameterMap);
      return sqlify.<E>executeUpdateAndReturnGeneratedKey(connection);
    }

  }

}
