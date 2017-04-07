package org.sqlify.resultparser;

import java.sql.ResultSet;


public interface ResultParser<T> {
  <T> T parseResultSet(ResultSet resultSet) throws Exception;
}
