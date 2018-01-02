package org.r10r.sqlify;

import java.math.BigDecimal;
import java.net.URL;
import org.r10r.sqlify.resultparser.ResultParser;
import java.sql.Connection;
import java.sql.Date;
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

public final class Sqlify {

  private final String sql;
  private final ResultParser<?> resultParser;
  private final LinkedHashMap<String, Object> parameterMap;
  private final List<String> parametersInSqlSorted;

  private Sqlify(String sql, ResultParser<?> resultParser, LinkedHashMap<String, Object> parameterMap) {
    this.sql = sql;
    this.resultParser = resultParser;
    this.parameterMap = parameterMap;
    this.parametersInSqlSorted = extractNameAndPosition(sql);
  }

  private <T> T executeSelect(Connection connection) {
    try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
      ResultSet resultSet = preparedStatement.executeQuery();
      T t = resultParser.parseResultSet(resultSet);
      return t;
    } catch (Exception e) {
      throw new SqlifyException("Ops. Something strange happened", e);
    }
  }

  private int executeUpdate(Connection connection) {
    String convertedPreparedStatement = convertIntoPreparedStatement(sql);
    try (PreparedStatement preparedStatement = connection.prepareStatement(convertedPreparedStatement)) {
      applyParameterMapToPreparedStatement(preparedStatement, parameterMap, parametersInSqlSorted);
      int numberOfChangedLines = preparedStatement.executeUpdate();
      return numberOfChangedLines;
    } catch (SQLException ex) {
      throw new SqlifyException("Ops. Something strange happened", ex);
    }
  }
  
  private <T> T executeUpdateAndReturnGeneratedKey(Connection connection) {
    String convertedPreparedStatement = convertIntoPreparedStatement(sql);
    try (PreparedStatement preparedStatement = connection.prepareStatement(
          convertedPreparedStatement, 
          Statement.RETURN_GENERATED_KEYS)) {
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
        
        if (value instanceof BigDecimal) {
          preparedStatement.setBigDecimal(positionInPreparedStatement, (BigDecimal) value);
        } else if (value instanceof Boolean) {
          preparedStatement.setBoolean(positionInPreparedStatement, (Boolean) value);
        } else if (value instanceof Date) {
          preparedStatement.setDate(positionInPreparedStatement, (Date) value);
        } else if (value instanceof Double) {
          preparedStatement.setDouble(positionInPreparedStatement, (Double) value);
        } else if (value instanceof Float) {
          preparedStatement.setFloat(positionInPreparedStatement, (Float) value);
        } else if (value instanceof Integer) {
          preparedStatement.setInt(positionInPreparedStatement, (Integer) value);
        } else if (value instanceof Long) {
          preparedStatement.setLong(positionInPreparedStatement, (Long) value);
        } else if (value instanceof Short) {
          preparedStatement.setShort(positionInPreparedStatement, (Short) value);
        } else if (value instanceof String) {
          preparedStatement.setString(positionInPreparedStatement, (String) value);
        } else if (value instanceof Time) {
          preparedStatement.setTime(positionInPreparedStatement, (Time) value);
        } else if (value instanceof Timestamp) {
          preparedStatement.setTimestamp(positionInPreparedStatement, (Timestamp) value);
        } else if (value instanceof URL) {
          preparedStatement.setURL(positionInPreparedStatement, (URL) value);
        } else {
          // Kind of a fallback. If you expect some other behavior feel
          // free to implement it.
          preparedStatement.setObject(positionInPreparedStatement, value);
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
  public static Builder1 sql(String sql) {
    return new Builder1(sql);
  }

  public static class Builder1 {

    private final String sql;
    private final LinkedHashMap<String, Object> parameterMap;
    private ResultParser<?> resultParser;

    public Builder1(String sql) {
      this.sql = sql;
      this.parameterMap = new LinkedHashMap<>();
    }

    public Builder1 withParameter(String key, Object value) {
      parameterMap.put(key, value);
      return this;
    }

    public Builder1 parseResultWith(ResultParser<?> resultParser) {
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
    public <E> E executeSelect(Connection connection) {
      Sqlify sqlify = new Sqlify(this.sql, this.resultParser, this.parameterMap);
      return sqlify.<E>executeSelect(connection);
    }

    /**
     * Executes an update (insert, delete statement)
     * 
     * @param connection The connection to use for this query.
     * @return The number of lines affected by this query.
     */
    public int executeUpdate(Connection connection) {
      Sqlify sqlify = new Sqlify(this.sql, this.resultParser, this.parameterMap);
      return sqlify.executeUpdate(connection);
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
    public <E> E executeUpdateAndReturnGeneratedKey(Connection connection) {
      Sqlify sqlify = new Sqlify(this.sql, this.resultParser, this.parameterMap);
      return sqlify.<E>executeUpdateAndReturnGeneratedKey(connection);
    }

  }

}
