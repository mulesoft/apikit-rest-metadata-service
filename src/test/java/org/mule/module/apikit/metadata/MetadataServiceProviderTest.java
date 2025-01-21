/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata;

import org.junit.jupiter.api.Test;
import org.mule.module.apikit.metadata.internal.MetadataServiceImpl;
import org.mule.module.apikit.metadata.internal.MetadataServiceProvider;
import org.mule.runtime.api.service.ServiceDefinition;

import static org.junit.jupiter.api.Assertions.*;

public class MetadataServiceProviderTest {

  @Test
  public void testGetServiceDefinition() {
    MetadataServiceProvider provider = new MetadataServiceProvider();
    ServiceDefinition serviceDefinition = provider.getServiceDefinition();

    assertNotNull("Service definition should not be null", serviceDefinition.toString());
    assertInstanceOf(MetadataServiceImpl.class, serviceDefinition.getService(), "Service instance should be of type MetadataServiceImpl");
  }

  @Test
  public void testServiceDefinitionConsistency() {
    MetadataServiceProvider provider = new MetadataServiceProvider();
    ServiceDefinition definition1 = provider.getServiceDefinition();
    ServiceDefinition definition2 = provider.getServiceDefinition();

    assertNotNull("First service definition should not be null", definition1.toString());
    assertNotNull("Second service definition should not be null", definition2.toString());
    assertEquals(definition1.getService().getClass(),
            definition2.getService().getClass(), "Service instances should be of the same type across calls");
  }

  @Test
  public void testServiceInstanceUniqueness() {
    MetadataServiceProvider provider = new MetadataServiceProvider();
    ServiceDefinition definition1 = provider.getServiceDefinition();
    ServiceDefinition definition2 = provider.getServiceDefinition();

    assertNotSame(definition1.getService(),
            definition2.getService(), "Service instances should be unique for each call");
  }
}
