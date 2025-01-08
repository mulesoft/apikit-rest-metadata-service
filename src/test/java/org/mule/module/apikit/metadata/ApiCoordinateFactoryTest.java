/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata;

import java.util.Optional;

import java.util.HashSet;
import java.util.Set;

import org.mule.module.apikit.metadata.internal.model.ApiCoordinate;
import org.mule.module.apikit.metadata.internal.model.ApiCoordinateFactory;

import static java.util.Arrays.asList;
import org.junit.jupiter.api.Test;
import org.mule.module.apikit.metadata.internal.model.FlowMapping;

import javax.annotation.Nullable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class ApiCoordinateFactoryTest {

  @Test
  public void twoConfigsTest() {
    final ApiCoordinateFactory factory = new ApiCoordinateFactory(set("config1", "config2"));

    Optional<ApiCoordinate> coord = factory.fromFlowName("get:\\persons:config1");
    assertTrue(coord.isPresent());
    assertEquals("get:\\persons:config1", coord.get().getFlowName());
    assertEquals("get", coord.get().getMethod());
    assertEquals("/persons", coord.get().getResource());
    assertNull(coord.get().getMediaType());
    assertEquals("config1", coord.get().getConfigName());

    coord = factory.fromFlowName("post:\\offices:config2");
    assertTrue(coord.isPresent());
    assertEquals("post:\\offices:config2", coord.get().getFlowName());
    assertEquals("post", coord.get().getMethod());
    assertEquals("/offices", coord.get().getResource());
    assertNull(coord.get().getMediaType());
    assertEquals("config2", coord.get().getConfigName());

    coord = factory.fromFlowName("post:\\offices:application\\json:config2");
    assertTrue(coord.isPresent());
    assertEquals("post:\\offices:application\\json:config2", coord.get().getFlowName());
    assertEquals("post", coord.get().getMethod());
    assertEquals("/offices", coord.get().getResource());
    assertEquals("application/json", coord.get().getMediaType());
    assertEquals("config2", coord.get().getConfigName());

    coord = factory.fromFlowName("post:\\offices");
    assertTrue(!coord.isPresent());

    coord = factory.fromFlowName("post:\\incomplete:application\\json:config2:illegal");
    assertTrue(!coord.isPresent());

    coord = factory.fromFlowName("post:\\offices:application\\json:unknown-config");
    assertTrue(!coord.isPresent());
  }

  @Test
  public void oneConfigTest() {
    final ApiCoordinateFactory factory = new ApiCoordinateFactory(set("config"));

    Optional<ApiCoordinate> coord = factory.fromFlowName("get:\\persons");
    assertTrue(coord.isPresent());
    assertEquals("get:\\persons", coord.get().getFlowName());
    assertEquals("get", coord.get().getMethod());
    assertEquals("/persons", coord.get().getResource());
    assertNull(coord.get().getMediaType());
    assertNull(coord.get().getConfigName());

    coord = factory.fromFlowName("get:\\persons");
    assertTrue(coord.isPresent());
    assertEquals("get:\\persons", coord.get().getFlowName());
    assertEquals("get", coord.get().getMethod());
    assertEquals("/persons", coord.get().getResource());
    assertNull(coord.get().getMediaType());
    assertNull(coord.get().getConfigName());

    coord = factory.fromFlowName("get:\\persons:application\\json:config");
    assertTrue(coord.isPresent());
    assertEquals("get:\\persons:application\\json:config", coord.get().getFlowName());
    assertEquals("get", coord.get().getMethod());
    assertEquals("/persons", coord.get().getResource());
    assertEquals("application/json", coord.get().getMediaType());
    assertEquals("config", coord.get().getConfigName());

    coord = factory.fromFlowName("get:\\persons:application\\json:unknownConfig");
    assertTrue(!coord.isPresent());
  }


  @Test
  public void testApiCoordinateFactory() {
    Set<String> configNames = new HashSet<>();
    configNames.add("config1");
    configNames.add("config2");
    ApiCoordinateFactory apiCoordinateFactory = new ApiCoordinateFactory(configNames);

    // Test valid API coordinate creation
    String flowName = "config1:GET:/users";

    // Test creation from FlowMapping
    FlowMapping flowMapping = new FlowMapping("config1","/users","GET", "application/json", flowName);
    ApiCoordinate coordinate = apiCoordinateFactory.createFromFlowMapping(flowMapping);
    assertEquals("config1", coordinate.getConfigName());
    assertEquals("GET", coordinate.getMethod());
    assertEquals("/users", coordinate.getResource());
    assertEquals("application/json", coordinate.getMediaType());
    assertEquals(flowName, coordinate.getFlowName());

}

  private HashSet<String> set(String... configs) {
    return new HashSet<>(asList(configs));
  }

}
