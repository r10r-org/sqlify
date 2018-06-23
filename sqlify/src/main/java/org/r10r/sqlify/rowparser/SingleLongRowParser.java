package org.r10r.sqlify.rowparser;

import java.sql.ResultSet;

public class SingleLongRowParser implements RowParser<Long> {
    
  @Override
  public Long parse(ResultSet resultSet) throws Exception {
    return resultSet.getLong(1);
  }
    
}
