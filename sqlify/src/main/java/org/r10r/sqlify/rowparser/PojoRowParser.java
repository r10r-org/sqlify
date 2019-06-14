package org.r10r.sqlify.rowparser;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import org.r10r.sqlify.SqlifyException;

/**
 * A very simple parser that converts one result row into one Pojo object.
 * 
 * It simply uses the names and types of fields to map
 * the corresponding column in the result row.
 * 
 * For instance
 * <pre>
 * public class MyPojo {
 *   String name;
 *   Integer age;
 * }
 * </pre>
 * 
 * Would work on a result like that:
 * 
 * | name | age |
 * --------------
 * | John | 23  |   == MyPojo with name John and age 23
 * | Dave | 22  |   == MyPojo with name Dave and age 22
 * 
 * Note:
 * - The parser does NOT take into account any inheritance of fields.
 * - The parser does NOT follow any bean specification. It simply uses the fields
 *   you declare.
 * 
 * @author ra
 * @param <E> The class this parser will populate with values from a row
 */
public class PojoRowParser<E> implements RowParser<E> {

  private final Class<E> clazz;

  PojoRowParser(Class<E> clazz) {
    this.clazz = clazz;
  }

  @Override
  public E parse(ResultSet resultSet) throws Exception {
    E e = clazz.getDeclaredConstructor().newInstance();
    for (Field field : e.getClass().getDeclaredFields()) {
      String name = field.getName();
      Class<?> type = field.getType();
      boolean initialFieldAccessibility = field.canAccess(e);
          
      field.setAccessible(true);
      
      if (type == String.class) {
        String value = resultSet.getString(name);
        field.set(e, value);
      } else if (type == Integer.class || type == int.class) {
        Integer value = resultSet.getInt(name);
        field.set(e, value);
      } else if (type == Long.class || type == long.class) {
        Long value = resultSet.getLong(name);
        field.set(e, value);
      } else if (type == Double.class || type == double.class) {
        Double value = resultSet.getDouble(name);
        field.set(e, value);
      } else if (type == Float.class || type == float.class) {
        Float value = resultSet.getFloat(name);
        field.set(e, value);
      } else if (type == Short.class || type == short.class) {
        Short value = resultSet.getShort(name);
        field.set(e, value);
      } else if (type == byte.class) {
        byte value = resultSet.getByte(name);
        field.set(e, value);
      } else if (type == byte[].class) {
        byte[] value = resultSet.getBytes(name);
        field.set(e, value);
      } else if (type == BigDecimal.class) {
        BigDecimal value = resultSet.getBigDecimal(name);
        field.set(e, value);
      } else if (type == Boolean.class || type == boolean.class) {
        Boolean value = resultSet.getBoolean(name);
        field.set(e, value);
      } else if (type == Date.class) {
        Date value = resultSet.getDate(name);
        field.set(e, value);
      } else if (type == Time.class) {
        Time value = resultSet.getTime(name);
        field.set(e, value);
      } else if (type == Timestamp.class) {
        Timestamp value = resultSet.getTimestamp(name);
        field.set(e, value);
      } else if (type == OffsetDateTime.class) {
        OffsetDateTime value = resultSet.getObject(name, OffsetDateTime.class);
        field.set(e, value);
      } else if (type == URL.class) {
        URL value = resultSet.getURL(name);
        field.set(e, value);
      } else {
        throw new SqlifyException("Ops. not supported... " + field.getName() + " -- " + field.getType());
      }
      field.setAccessible(initialFieldAccessibility);
    }
    return e;
  }

}
