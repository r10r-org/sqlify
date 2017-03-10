package org.sqlify.resultparser;

import org.sqlify.rowparser.RowParsers;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import org.sqlify.rowparser.RowParser;

public class ListResultParser<T> implements ResultParser<List<T>> {

  private final RowParser<T> rowParser;

  private ListResultParser(Class<T> clazz) {
    this.rowParser = RowParsers.<T>getRowParserFor(clazz);
  }

  public static <E> ListResultParser<E> of(Class<E> clazz) {
    return new ListResultParser<>(clazz);
  }

  @Override
  public List<T> parseResultSet(ResultSet resultSet) throws Exception {
    List<T> list = new ArrayList<>();
    while (resultSet.next()) {
      T t = rowParser.parse(resultSet);
      list.add(t);
    }
    return list;
  }

}
