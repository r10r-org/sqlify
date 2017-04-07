package org.sqlify.rowparser;

import java.sql.ResultSet;


public class OneLongRowParser implements RowParser<Long> {
    
  @Override
  public Long parse(ResultSet resultSet) throws Exception {
    return resultSet.getLong(1);
  }
    
}
