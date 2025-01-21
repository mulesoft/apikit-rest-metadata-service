/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mule.module.apikit.metadata.internal.MetadataBuilderImpl;
import org.mule.module.apikit.metadata.internal.MetadataServiceImpl;
import org.mule.runtime.apikit.metadata.api.MetadataBuilder;
import org.mule.runtime.apikit.metadata.api.MetadataService;

import static org.junit.jupiter.api.Assertions.*;

public class MetadataServiceImplTest {

  private MetadataService metadataService;

  @BeforeEach
  public void setUp() {
    metadataService = new MetadataServiceImpl();
  }

  @Test
  public void testGetApikitMetadataBuilder() {
    MetadataBuilder builder = metadataService.getApikitMetadataBuilder();
    assertNotNull("MetadataBuilder should not be null", builder.toString());
    assertInstanceOf(MetadataBuilderImpl.class, builder, "MetadataBuilder should be an instance of MetadataBuilderImpl");
  }

  @Test
  public void testGetName() {
    String name = metadataService.getName();
    assertNotNull("Name should not be null", name);
    assertEquals("Apikit metadata service", name, "Name should match expected value");
  }

  @Test
  public void testMultipleCallsToGetApikitMetadataBuilder() {
    MetadataBuilder builder1 = metadataService.getApikitMetadataBuilder();
    MetadataBuilder builder2 = metadataService.getApikitMetadataBuilder();
    assertNotNull("First MetadataBuilder should not be null", builder1.toString());
    assertNotNull("Second MetadataBuilder should not be null", builder2.toString());
    assertNotSame(builder1, builder2, "Multiple calls should return different instances");
  }

  @Test
  public void testMetadataServiceImplImplementsMetadataService() {
    assertInstanceOf(MetadataService.class, metadataService, "MetadataServiceImpl should implement MetadataService");
  }
}
