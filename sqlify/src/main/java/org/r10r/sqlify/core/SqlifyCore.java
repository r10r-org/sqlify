package org.r10r.sqlify.core;

import java.math.BigDecimal;
import java.net.URL;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.r10r.sqlify.SqlifyException;

public final class SqlifyCore {

  protected final static Pattern USER_PARAMETER_EXTRACTION_PATTERN = Pattern.compile("\\{(.*?)\\}");

  protected final static String convertNamedParametersIntoJdbcCompliantPreparedStatement(String sqlWithNamedPlaceholers) {
    return sqlWithNamedPlaceholers.replaceAll("\\{.*?\\}", "?");
  }

  protected final static List<String> extractParameterNames(String userSpecifiedSql) {
    List<String> allParametersSorted = new ArrayList<>();

    Matcher matcher = USER_PARAMETER_EXTRACTION_PATTERN.matcher(userSpecifiedSql);
    while (matcher.find()) {
      String name = matcher.group(1);
      allParametersSorted.add(name);
    }

    return allParametersSorted;
  }

  protected final static PreparedStatement applyParameterMapToPreparedStatement(
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
    } catch (SQLException sqlException) {
      throw new SqlifyException("Ops. An error occurred. " + sqlException.getMessage(), sqlException);
    }
    return preparedStatement;
  }
  
  
  public static void verifyThatAllNeededParametersAreProvidedByUser(
      Map<String, Object> parameterMap, 
      List<String> parametersInSqlSorted) {
    
    List<String> missingParametersToExecuteSqlStatement = parametersInSqlSorted
        .stream()
        .filter(parameterInSql -> !parameterMap.containsKey(parameterInSql))
        .collect(Collectors.toList());
    
    if (!missingParametersToExecuteSqlStatement.isEmpty()) {
      String missingParametersToExecuteSqlStatementAsString = missingParametersToExecuteSqlStatement
          .stream()
          .collect( Collectors.joining( ", " ) );
      throw new SqlifyException("Missing parameters to execute sql query. Please provide the following paramters via withParameters(key, value): " + missingParametersToExecuteSqlStatementAsString);
    }
  
  }

}
