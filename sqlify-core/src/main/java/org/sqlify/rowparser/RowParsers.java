package org.sqlify.rowparser;

public class RowParsers<T> {

  /**
   * Determines the default RowParser for a class.
   * 
   * Use your own RowParser implementation if you need something more flexible.
   * 
   */  
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
