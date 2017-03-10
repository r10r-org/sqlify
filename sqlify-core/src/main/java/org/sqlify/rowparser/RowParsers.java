package org.sqlify.rowparser;

// able to parse the result of one row into something useful.
public class RowParsers<T> {

  public static <E> RowParser getRowParserFor(Class<E> e) {
    if (e == String.class) {
      return new OneStringRowParser();
    } else if (e == Long.class) {
      return new OneLongRowParser();
    } else {
      return new PojoRowParser<>(e);
    }
  }

}
