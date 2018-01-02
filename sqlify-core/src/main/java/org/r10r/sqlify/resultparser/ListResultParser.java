package org.r10r.sqlify.resultparser;

import org.r10r.sqlify.rowparser.RowParsers;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import org.r10r.sqlify.rowparser.RowParser;

public class ListResultParser<T> implements ResultParser<List<T>> {

  private final RowParser<T> rowParser;
  
  private ListResultParser(RowParser<T> rowParser) {
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
  public static <E> ListResultParser<E> of(RowParser<E> rowParser) {
    return new ListResultParser(rowParser);
  }

  public static <E> ListResultParser<E> of(Class<E> clazz) {
    RowParser<E> rowParser = RowParsers.getDefaultParserFor(clazz);
    return new ListResultParser(rowParser);
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
