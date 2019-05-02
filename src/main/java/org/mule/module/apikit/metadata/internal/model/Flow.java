/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata.internal.model;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

public class Flow {

  public static final String URL_RESOURCE_SEPARATOR = "/";

  private static final ImmutableMap<String, String> specialCharacters = ImmutableMap.<String, String>builder()
      .put(URL_RESOURCE_SEPARATOR, "\\")
      .put("{", "(")
      .put("}", ")")
      .build();

  public static String decode(String value) {
    for (Map.Entry<String, String> entry : specialCharacters.entrySet()) {
      value = value.replace(entry.getValue(), entry.getKey());
    }

    return value;
  }

  private String name;

  public Flow(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  @Override
  public String toString() {
    return getName();
  }
}
