package org.sqlify.rowparser;

import java.sql.ResultSet;

public class OneStringRowParser implements RowParser<String> {

  @Override
  public String parse(ResultSet resultSet) throws Exception {
    return resultSet.getString(1);
  }

}
