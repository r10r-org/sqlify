package org.sqlify.rowparser;

import java.lang.reflect.Field;
import java.sql.ResultSet;

public class PojoRowParser<E> implements RowParser<E> {

  Class<E> clazz;

  PojoRowParser(Class<E> clazz) {
    this.clazz = clazz;
  }

  @Override
  public E parse(ResultSet resultSet) throws Exception {
    E e = clazz.newInstance();
    for (Field field : e.getClass().getDeclaredFields()) {
      String name = field.getName();
      Class<?> type = field.getType();
      boolean isFieldAccessible = field.isAccessible();
      field.setAccessible(true);
      if (type == String.class) {
        String value = resultSet.getString(name);
        field.set(e, value);
      } else if (type == Integer.class) {
        Integer value = resultSet.getInt(name);
        field.set(e, value);
      } else {
        throw new RuntimeException("Ops. not supported... " + field.getName() + " -- " + field.getType());
      }
      field.setAccessible(isFieldAccessible);
    }
    return e;
  }

}
