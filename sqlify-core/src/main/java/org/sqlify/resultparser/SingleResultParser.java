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

  /**
   * Define your own RowParser. Full flexibility.
   * 
   * @param <E> The type the RowParser will return
   * @param rowParser The RowParser to use
   * @return The ListResultParser that will use the defined RowParser to parse
   *         rows.
   */
  public static <E> SingleResultParser<E> of(RowParser<E> rowParser) {
    return new SingleResultParser(rowParser);
  }
  
  public static <E> SingleResultParser<E> of(Class<E> clazz) {
    RowParser<E> rowParser = RowParsers.getDefaultParserFor(clazz);
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
