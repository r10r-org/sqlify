package org.sqlify.resultparser;

import org.sqlify.rowparser.RowParsers;
import java.sql.ResultSet;
import java.util.Optional;
import org.sqlify.rowparser.RowParser;

public class SingleOptionalResultParser<T> implements ResultParser<Optional<T>> {

  private final RowParser<T> rowParser;

  private SingleOptionalResultParser(Class<T> clazz) {
    this.rowParser = RowParsers.<T>getRowParserFor(clazz);
  }

  public static <E> SingleOptionalResultParser<E> of(Class<E> clazz) {
    return new SingleOptionalResultParser<>(clazz);
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
