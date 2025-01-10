/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata.internal.model;

import org.junit.Test;
import static org.junit.Assert.*;

public class FlowMappingTest {

  @Test
  public void testFlowMappingConstructorAndGetters() {
    String configName = "testConfig";
    String resource = "/api/users";
    String action = "GET";
    String contentType = "application/json";
    String flowRef = "getUsers";

    FlowMapping flowMapping = new FlowMapping(configName, resource, action, contentType, flowRef);

    assertEquals(configName, flowMapping.getConfigName());
    assertEquals(resource, flowMapping.getResource());
    assertEquals(action, flowMapping.getAction());
    assertEquals(contentType, flowMapping.getContentType());
    assertEquals(flowRef, flowMapping.getFlowRef());
  }

  @Test
  public void testFlowMappingWithNullValues() {
    FlowMapping flowMapping = new FlowMapping(null, null, null, null, null);

    assertNull(flowMapping.getConfigName());
    assertNull(flowMapping.getResource());
    assertNull(flowMapping.getAction());
    assertNull(flowMapping.getContentType());
    assertNull(flowMapping.getFlowRef());
  }

  @Test
  public void testFlowMappingWithEmptyStrings() {
    FlowMapping flowMapping = new FlowMapping("", "", "", "", "");

    assertEquals("", flowMapping.getConfigName());
    assertEquals("", flowMapping.getResource());
    assertEquals("", flowMapping.getAction());
    assertEquals("", flowMapping.getContentType());
    assertEquals("", flowMapping.getFlowRef());
  }

  @Test
  public void testFlowMappingWithSpecialCharacters() {
    String configName = "config#1";
    String resource = "/api/users/{id}";
    String action = "POST";
    String contentType = "application/xml; charset=UTF-8";
    String flowRef = "create_user_flow";

    FlowMapping flowMapping = new FlowMapping(configName, resource, action, contentType, flowRef);

    assertEquals(configName, flowMapping.getConfigName());
    assertEquals(resource, flowMapping.getResource());
    assertEquals(action, flowMapping.getAction());
    assertEquals(contentType, flowMapping.getContentType());
    assertEquals(flowRef, flowMapping.getFlowRef());
  }
}
