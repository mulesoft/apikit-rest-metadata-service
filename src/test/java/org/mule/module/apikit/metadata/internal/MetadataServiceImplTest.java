package org.mule.module.apikit.metadata.internal;

import org.junit.Before;
import org.junit.Test;
import org.mule.runtime.apikit.metadata.api.MetadataBuilder;
import org.mule.runtime.apikit.metadata.api.MetadataService;

import static org.junit.Assert.*;

public class MetadataServiceImplTest {

  private MetadataService metadataService;

  @Before
  public void setUp() {
    metadataService = new MetadataServiceImpl();
  }

  @Test
  public void testGetApikitMetadataBuilder() {
    MetadataBuilder builder = metadataService.getApikitMetadataBuilder();
    assertNotNull("MetadataBuilder should not be null", builder);
    assertTrue("MetadataBuilder should be an instance of MetadataBuilderImpl", builder instanceof MetadataBuilderImpl);
  }

  @Test
  public void testGetName() {
    String name = metadataService.getName();
    assertNotNull("Name should not be null", name);
    assertEquals("Name should match expected value", "Apikit metadata service", name);
  }

  @Test
  public void testMultipleCallsToGetApikitMetadataBuilder() {
    MetadataBuilder builder1 = metadataService.getApikitMetadataBuilder();
    MetadataBuilder builder2 = metadataService.getApikitMetadataBuilder();
    assertNotNull("First MetadataBuilder should not be null", builder1);
    assertNotNull("Second MetadataBuilder should not be null", builder2);
    assertNotSame("Multiple calls should return different instances", builder1, builder2);
  }

  @Test
  public void testMetadataServiceImplImplementsMetadataService() {
    assertTrue("MetadataServiceImpl should implement MetadataService", metadataService instanceof MetadataService);
  }
}
