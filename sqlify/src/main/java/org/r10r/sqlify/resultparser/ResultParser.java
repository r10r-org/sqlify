package org.r10r.sqlify.resultparser;

import java.sql.ResultSet;


public interface ResultParser<T> {
  <T> T parseResultSet(ResultSet resultSet) throws Exception;
}
