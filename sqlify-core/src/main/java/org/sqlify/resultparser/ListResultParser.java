package org.sqlify.resultparser;

import org.sqlify.rowparser.RowParsers;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import org.sqlify.rowparser.RowParser;

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

  /**
   * Define your own RowParser. Full flexibility.
   * 
   * @param <E> The type the RowParser will return
   * @param clazz Class that determines the RowParser. See RowParsers how
   *              the class is resolved.
   * @return The ListResultParser that will use the defined RowParser to parse
   *         rows.
   */
  public static <E> ListResultParser<E> of(Class<E> clazz) {
    RowParser<E> rowParser = RowParsers.getRowParserFor(clazz);
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
