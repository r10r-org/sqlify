package org.r10r.core;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import javax.sql.DataSource;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.Before;
import org.junit.Test;
import org.r10r.slqify.testutils.ARunningPostgresServer;
import org.r10r.sqlify.Database;
import org.r10r.sqlify.Sqlify;
import org.r10r.sqlify.resultparser.SingleResultParser;
import static org.assertj.core.api.Assertions.*;

public class ParserTest {
  
  Database database;
  
  @Before
  public void init() {
    DataSource dataSource = ARunningPostgresServer.getDatasource();
    this.database = Database.use(dataSource);
    
  }
  
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class TestTable {
    public OffsetDateTime time_created;
  }
  
  @Test
  public void testIt() {
    
      database.withConnection(connection -> {
        
        // given
        Sqlify.sql("CREATE TABLE test_table (time_created TIMESTAMPTZ NOT NULL)").executeUpdate(connection);
        
        // when
        OffsetDateTime offsetDateTime = OffsetDateTime.now(ZoneId.of("UTC"));
        Sqlify
            .sql("INSERT INTO test_table (time_created) VALUES ({timeCreated})")
            .withParameter("timeCreated", offsetDateTime)
            .executeUpdate(connection);
        
        // then
        TestTable testTable = Sqlify
            .sql("SELECT time_created from test_table")
            .parseResultWith(SingleResultParser.of(TestTable.class))
            .executeSelect(connection);
        
        assertThat(testTable.time_created).isEqualTo(offsetDateTime);
        
        
        Sqlify.sql("DROP TABLE test_table").executeUpdate(connection);
        return null;
      });
        

      
      
      
    
    
//    CREATE TABLE dashboards (
//  dashboard_id BIGSERIAL PRIMARY KEY,
//  version BIGINT NOT NULL,
//  account_id BIGINT NOT NULL,
//  user_id TEXT NOT NULL,
//  is_deleted BOOLEAN NOT NULL,
//  name TEXT NOT NULL,
//  description TEXT NOT NULL,
//  time_created TIMESTAMPTZ NOT NULL
//);
    
    
  
  
  
  }
  
  
}
