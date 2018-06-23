package org.r10r.sqlify.rowparser;

public class RowParsers<T> {

  /**
   * Determines the default RowParser for a class.
   * 
   * Use your own RowParser implementation if you need something more flexible.
   * 
   */  
  public static <E> RowParser getDefaultParserFor(Class<E> e) {
    if (e == String.class) {
      return new SingleStringRowParser();
    } else if (e == Long.class) {
      return new SingleLongRowParser();
    } else {
      return new PojoRowParser<>(e);
    }
  }

}
