package org.sqlify;

import org.sqlify.resultparser.ResultParser;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Sqlify<T> {

  private final String sql;
  private final ResultParser<T> resultParser;
  private final LinkedHashMap<String, Object> parameterMap;
  private final Map<String, Integer> positionToNameMap;

  private Sqlify(String sql, ResultParser<T> resultParser, LinkedHashMap<String, Object> parameterMap) {
    this.sql = sql;
    this.resultParser = resultParser;
    this.parameterMap = parameterMap;
    this.positionToNameMap = extractNameAndPosition(sql, parameterMap);
  }

  private T executeSelect(Connection connection) {
    try {
      PreparedStatement preparedStatement = connection.prepareStatement(sql);
      ResultSet resultSet = preparedStatement.executeQuery();
      T t = resultParser.parseResultSet(resultSet);
      return t;
    } catch (Exception e) {
      throw new RuntimeException("Ops. Something strange happened", e);
    }
  }

  private int executeUpdate(Connection connection) {
    try {
      String convertedPreparedStatement = convertIntoPreparedStatement(sql);
      PreparedStatement preparedStatement = connection.prepareStatement(convertedPreparedStatement);
      applyParameterMapToPreparedStatement(preparedStatement, parameterMap, positionToNameMap);
      int numberOfChangedLines = preparedStatement.executeUpdate();
      return numberOfChangedLines;
    } catch (SQLException ex) {
      throw new RuntimeException("Ops. Something strange happened", ex);
    }
  }
  
  private T executeUpdateAndReturnGeneratedKey(Connection connection) {
    try {
      String convertedPreparedStatement = convertIntoPreparedStatement(sql);
      PreparedStatement preparedStatement = connection.prepareStatement(
          convertedPreparedStatement, 
          Statement.RETURN_GENERATED_KEYS);
      applyParameterMapToPreparedStatement(preparedStatement, parameterMap, positionToNameMap);
      preparedStatement.executeUpdate();
      ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
      return resultParser.parseResultSet(generatedKeys);
    } catch (Exception ex) {
      throw new RuntimeException("Ops. Something strange happened", ex);
    }
  }

  // insert into table(name) values ({name}) => insert into table(name) values (?)
  private String convertIntoPreparedStatement(String sql) {
    return sql.replaceAll("\\{.*?\\}", "?");
  }

  private String withBraces(String rawKey) {
    return "{" + rawKey + "}";
  }

  // insert into table(name) values ({name}) => insert into table(name) values (?)
  private Map<String, Integer> extractNameAndPosition(String userSpecifiedSql, Map<String, Object> parameterMap) {
    // some ugly code... that should work better...
    Map<Integer, String> indexToKey = new HashMap<>();
    for (String key : parameterMap.keySet()) {
      String keyWithBraces = withBraces(key);
      int index = userSpecifiedSql.indexOf(keyWithBraces);
      indexToKey.put(index, key);
    }
    List<String> parametersSortedByAppearenceInSql = indexToKey.entrySet().stream().sorted((e1, e2) -> {
      return e1.getKey() - e2.getKey();
    }).map((e) -> e.getValue()).collect(Collectors.toList());
    Map<String, Integer> nameAndPosition = new HashMap<>();
    for (int i = 0; i < parametersSortedByAppearenceInSql.size(); i++) {
      int indexPositionForJdbc = i + 1;
      nameAndPosition.put(parametersSortedByAppearenceInSql.get(i), indexPositionForJdbc);
    }
    return nameAndPosition;
  }

  private PreparedStatement applyParameterMapToPreparedStatement(
      PreparedStatement preparedStatement, 
      Map<String, Object> parameterMap, 
      Map<String, Integer> positionToNameMap) {
    try {
      for (Map.Entry<String, Object> entrySet : parameterMap.entrySet()) {
        int i = positionToNameMap.get(entrySet.getKey()); // FIXME => get exception...
        if (entrySet.getValue() instanceof String) {
          preparedStatement.setString(i, (String) entrySet.getValue());
        } else if (entrySet.getValue() instanceof Long) {
          preparedStatement.setLong(i, (Long) entrySet.getValue());
        } else if (entrySet.getValue() instanceof Integer) {
          preparedStatement.setInt(i, (Integer) entrySet.getValue());
        } else if (entrySet.getValue() instanceof Timestamp) {
          preparedStatement.setTimestamp(i, (Timestamp) entrySet.getValue());
        } else if (entrySet.getValue() instanceof Time) {
          preparedStatement.setTime(i, (Time) entrySet.getValue());
        } else {
          //TODO add more conversions
          throw new RuntimeException("ops... type not supported...");
        }
      }
    } catch (SQLException ex) {
      throw new RuntimeException("ops... sql error ocurred...", ex);
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

    private String sql;
    private LinkedHashMap<String, Object> parameterMap;
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

    public E executeSelect(Connection connection) {
      Sqlify<E> sql = new Sqlify<>(this.sql, this.resultParser, this.parameterMap);
      return sql.<E>executeSelect(connection);
    }

    public int executeUpdate(Connection connection) {
      Sqlify<E> sql = new Sqlify<>(this.sql, this.resultParser, this.parameterMap);
      return sql.<E>executeUpdate(connection);
    }
    
    public E executeUpdateAndReturnGeneratedKey(Connection connection) {
      Sqlify<E> sql = new Sqlify<>(this.sql, this.resultParser, this.parameterMap);
      return sql.<E>executeUpdateAndReturnGeneratedKey(connection);
    }

  }

}
