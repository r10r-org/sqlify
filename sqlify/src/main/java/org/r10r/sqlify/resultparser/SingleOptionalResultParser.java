package org.r10r.sqlify.resultparser;

import org.r10r.sqlify.rowparser.RowParsers;
import java.sql.ResultSet;
import java.util.Optional;
import org.r10r.sqlify.rowparser.RowParser;

public class SingleOptionalResultParser<T> implements ResultParser<Optional<T>> {

  private final RowParser<T> rowParser;

  private SingleOptionalResultParser(RowParser<T> rowParser) {
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
  public static <E> SingleOptionalResultParser<E> of(RowParser<E> rowParser) {
    return new SingleOptionalResultParser(rowParser);
  }

  public static <E> SingleOptionalResultParser<E> of(Class<E> clazz) {
    RowParser<E> rowParser = RowParsers.getDefaultParserFor(clazz);
    return new SingleOptionalResultParser(rowParser);
  }

  @Override
  public Optional<T> parseResultSet(ResultSet resultSet) throws Exception {
    Optional<T> optional;
    if (resultSet.next()) {
      T t = rowParser.parse(resultSet);
      optional = Optional.of(t);
    } else {
      optional = Optional.empty();
    }
    return optional;
  }

}
