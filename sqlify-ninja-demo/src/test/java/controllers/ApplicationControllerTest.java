package controllers;

import com.google.common.collect.Maps;
import java.util.Map;
import ninja.NinjaTest;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;

public class ApplicationControllerTest extends NinjaTest {

  @Test
  public void testThatItWorks() {

    // test that post is not there
    String result = ninjaTestBrowser.makeRequest(getServerAddress() + "/");
    Assert.assertThat(result, CoreMatchers.not(CoreMatchers.containsString("a funky content")));
    Assert.assertThat(result, CoreMatchers.not(CoreMatchers.containsString("a@funkyemail.com")));
    
    // post
    Map<String, String> map = Maps.newHashMap();
    map.put("content", "a funky content");
    map.put("email", "a@funkyemail.com");
    Map<String, String> headers = Maps.newHashMap();
    ninjaTestBrowser.makePostRequestWithFormParameters(getServerAddress() + "/post", headers, map);
    
    // test result of post
    result = ninjaTestBrowser.makeRequest(getServerAddress() + "/");
    Assert.assertThat(result, CoreMatchers.containsString("a funky content"));
    Assert.assertThat(result, CoreMatchers.containsString("a@funkyemail.com"));
  }
  
  @Test
  public void testThatSelectWorks() {
    String result = ninjaTestBrowser.makeRequest(getServerAddress() + "/1");
    // If select with id is broken then the following'd not show up:
    Assert.assertThat(result, CoreMatchers.containsString("Hi. This is a simple guestbook."));
  }

}
