package org.r10r.sqlify.rowparser;

import org.r10r.sqlify.rowparser.PojoRowParser;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.ResultSet;
import org.hamcrest.CoreMatchers;
import org.junit.Test;
import static org.junit.Assert.*;
import org.mockito.Mockito;


public class PojoRowParserTest {

  static class MyPojo {
    BigDecimal BigDecimal;
    Boolean Boolean;
    boolean boolean_;
    java.sql.Date java_sql_Date;
    Double Double;
    double double_;
    Float Float;
    float float_;
    Integer Integer;
    int int_;
    Long Long;
    long long_;
    Short Short;
    short short_;
    String String;
    java.sql.Time java_sql_Time;
    java.sql.Timestamp java_sql_Timestamp;
    URL URL;
  }

  @Test
  public void testMapping() throws Exception {
    // given
    ResultSet resultSet = Mockito.mock(ResultSet.class);
    Mockito.when(resultSet.getBigDecimal("BigDecimal")).thenReturn(new BigDecimal(1234.12));
    Mockito.when(resultSet.getBoolean("Boolean")).thenReturn(true);
    Mockito.when(resultSet.getBoolean("boolean_")).thenReturn(true);
    Mockito.when(resultSet.getDate("java_sql_Date")).thenReturn(new java.sql.Date(1234));
    Mockito.when(resultSet.getDouble("Double")).thenReturn(1234.12d);
    Mockito.when(resultSet.getDouble("double_")).thenReturn(12345.12d);
    Mockito.when(resultSet.getFloat("Float")).thenReturn(1234.12f);
    Mockito.when(resultSet.getFloat("float_")).thenReturn(12345.12f);
    Mockito.when(resultSet.getInt("Integer")).thenReturn(12345);
    Mockito.when(resultSet.getInt("int_")).thenReturn(123456);
    Mockito.when(resultSet.getLong("Long")).thenReturn(12345l);
    Mockito.when(resultSet.getLong("long_")).thenReturn(123456l);
    Mockito.when(resultSet.getShort("Short")).thenReturn((short) 1);
    Mockito.when(resultSet.getShort("short_")).thenReturn((short) 2); 
    Mockito.when(resultSet.getString("String")).thenReturn("a_String");
    Mockito.when(resultSet.getTime("java_sql_Time")).thenReturn(new java.sql.Time(12345));
    Mockito.when(resultSet.getTimestamp("java_sql_Timestamp")).thenReturn(new java.sql.Timestamp(12345));
    Mockito.when(resultSet.getURL("URL")).thenReturn(new URL("http://example.com"));

    PojoRowParser<MyPojo> pojoRowParser = new PojoRowParser(MyPojo.class);
    
    // when
    MyPojo myPojo = pojoRowParser.parse(resultSet);
    
    // then
    assertThat(myPojo.BigDecimal, CoreMatchers.equalTo(new BigDecimal(1234.12)));
    assertThat(myPojo.Boolean, CoreMatchers.equalTo(true));
    assertThat(myPojo.boolean_, CoreMatchers.equalTo(true));
    assertThat(myPojo.java_sql_Date, CoreMatchers.equalTo(new java.sql.Date(1234)));
    assertThat(myPojo.Double, CoreMatchers.equalTo(1234.12d));
    assertThat(myPojo.double_, CoreMatchers.equalTo(12345.12d));
    assertThat(myPojo.Float, CoreMatchers.equalTo(1234.12f));
    assertThat(myPojo.float_, CoreMatchers.equalTo(12345.12f));
    assertThat(myPojo.Integer, CoreMatchers.equalTo(12345));
    assertThat(myPojo.int_, CoreMatchers.equalTo(123456));
    assertThat(myPojo.Long, CoreMatchers.equalTo(12345l));
    assertThat(myPojo.long_, CoreMatchers.equalTo(123456l));
    assertThat(myPojo.Short, CoreMatchers.equalTo((short) 1));
    assertThat(myPojo.short_, CoreMatchers.equalTo((short) 2));
    assertThat(myPojo.String, CoreMatchers.equalTo("a_String"));
    assertThat(myPojo.java_sql_Time, CoreMatchers.equalTo(new java.sql.Time(12345)));
    assertThat(myPojo.java_sql_Timestamp, CoreMatchers.equalTo(new java.sql.Timestamp(12345)));
    assertThat(myPojo.URL, CoreMatchers.equalTo(new URL("http://example.com")));
  }
  
}
