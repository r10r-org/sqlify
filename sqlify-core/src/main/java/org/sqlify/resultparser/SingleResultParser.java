package org.sqlify.resultparser;

import org.sqlify.rowparser.RowParsers;
import java.sql.ResultSet;
import java.util.Optional;
import org.sqlify.SqlifyException;
import org.sqlify.rowparser.RowParser;

public class SingleResultParser<T> implements ResultParser<T> {

  private final RowParser<T> rowParser;

  private SingleResultParser(Class<T> clazz) {
    this.rowParser = RowParsers.<T>getRowParserFor(clazz);
  }

  public static <E> SingleResultParser<E> of(Class<E> clazz) {
    return new SingleResultParser<>(clazz);
  }

  @Override
  public T parseResultSet(ResultSet resultSet) throws Exception {
    if (resultSet.next()) {
      return rowParser.parse(resultSet);
    } else {
      throw new SqlifyException("Ops. Could not parse single result, or there was not result.");
    }
  }

}
