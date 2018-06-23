/*
 * Copyright 2018 ra.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.r10r.sqlify.core;

import java.util.HashMap;
import java.util.Map;

public class Batch {

  private final Map<String, Object> parameterMap;

  private Batch() {
    parameterMap = new HashMap<>();
  }

  public static Batch create() {
    return new Batch();
  }

  public Batch withParameter(String key, Object value) {
    parameterMap.put(key, value);
    return this;
  }

  protected Map<String, Object> getParameterMap() {
    return parameterMap;
  }

}
