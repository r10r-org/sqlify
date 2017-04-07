package org.sqlify.resultparser;

import org.sqlify.rowparser.RowParsers;
import java.sql.ResultSet;
import org.sqlify.SqlifyException;
import org.sqlify.rowparser.RowParser;

public class SingleResultParser<T> implements ResultParser<T> {

  private final RowParser<T> rowParser;

  private SingleResultParser(RowParser<T> rowParser) {
    this.rowParser = rowParser;
  }

  public static <E> SingleResultParser<E> of(RowParser<E> rowParser) {
    return new SingleResultParser(rowParser);
  }
  
  public static <E> SingleResultParser<E> of(Class<E> clazz) {
    RowParser<E> rowParser = RowParsers.getRowParserFor(clazz);
    return new SingleResultParser(rowParser);
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
