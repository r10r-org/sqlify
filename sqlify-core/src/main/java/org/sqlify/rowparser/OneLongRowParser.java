/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sqlify.rowparser;

import java.sql.ResultSet;

/**
 *
 * @author ra
 */
public class OneLongRowParser implements RowParser<Long> {
    
  @Override
  public Long parse(ResultSet resultSet) throws Exception {
    return resultSet.getLong(1);
  }
    
}
