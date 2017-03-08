/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import com.google.common.collect.Maps;
import java.util.Map;
import ninja.NinjaTest;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;

public class CtonrollerTest extends NinjaTest {

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

}
