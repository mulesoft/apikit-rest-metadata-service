/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata.internal.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mule.module.apikit.metadata.internal.amf.AmfHandler;
import org.mule.module.apikit.metadata.internal.amf.AutoHandler;
import org.mule.module.apikit.metadata.internal.raml.RamlHandler;
import org.mule.parser.service.ParserMode;
import org.mule.runtime.apikit.metadata.api.Notifier;
import org.mule.runtime.apikit.metadata.api.ResourceLoader;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ApikitConfigTest {

  @Mock
  private ResourceLoader resourceLoader;

  @Mock
  private Notifier notifier;

  @Mock
  private MetadataResolver metadataResolver;

  private ApikitConfig apikitConfig;

  @BeforeEach
  void setUp() {
    List<FlowMapping> flowMappings =
        Arrays.asList(new FlowMapping("testConfig", "/api/users", "GET", "application/json", "getUsers"));
    apikitConfig = new ApikitConfig("testConfig", "api.raml", flowMappings,
                                    "httpStatus", "outHeaders", "AUTO", resourceLoader, notifier);
  }

  @Test
  void testGetName() {
    assertEquals("testConfig", apikitConfig.getName());
  }

  @Test
  void testGetHttpStatusVarName() {
    assertEquals("httpStatus", apikitConfig.getHttpStatusVarName());
  }

  @Test
  void testGetOutputHeadersVarName() {
    assertEquals("outHeaders", apikitConfig.getOutputHeadersVarName());
  }

  @ParameterizedTest
  @EnumSource(ParserMode.class)
  void testGetMetadataResolverFactory(ParserMode parserMode) {
    ApikitConfig config = new ApikitConfig("testConfig", "api.raml", null,
                                           null, null, parserMode.name(), resourceLoader, notifier);

    MetadataResolverFactory factory = getMetadataResolverFactoryUsingReflection(config);

    assertNotNull(factory);
    Class<?> expectedClass = getExpectedFactoryClass(parserMode);
    assertEquals(expectedClass, factory.getClass());
  }

  private MetadataResolverFactory getMetadataResolverFactoryUsingReflection(ApikitConfig config) {
    try {
      java.lang.reflect.Method method = ApikitConfig.class.getDeclaredMethod("getMetadataResolverFactory");
      method.setAccessible(true);
      return (MetadataResolverFactory) method.invoke(config);
    } catch (Exception e) {
      throw new RuntimeException("Failed to invoke getMetadataResolverFactory using reflection", e);
    }
  }

  private Class<?> getExpectedFactoryClass(ParserMode parserMode) {
    switch (parserMode) {
      case RAML:
        return RamlHandler.class;
      case AMF:
        return AmfHandler.class;
      default:
        return AutoHandler.class;
    }
  }
}
