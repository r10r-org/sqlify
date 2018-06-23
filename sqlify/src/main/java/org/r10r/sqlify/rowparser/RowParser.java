package org.r10r.sqlify.rowparser;

import java.sql.ResultSet;

public interface RowParser<T> {

  T parse(ResultSet resultSet) throws Exception;
}
