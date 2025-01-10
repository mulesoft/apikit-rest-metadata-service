/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata.internal.raml;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mule.apikit.model.ApiSpecification;
import org.mule.apikit.model.Resource;
import org.mule.apikit.model.Action;
import org.mule.module.apikit.metadata.internal.model.ApiCoordinate;
import org.mule.runtime.apikit.metadata.api.MetadataSource;
import org.mule.runtime.apikit.metadata.api.Notifier;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class RamlApiWrapperTest {

  @Mock
  private ApiSpecification mockApiSpecification;

  @Mock
  private Notifier mockNotifier;

  @Mock
  private Resource mockResource;

  @Mock
  private Action mockAction;

  private RamlApiWrapper ramlApiWrapper;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);

    Map<String, Resource> resources = new HashMap<>();
    resources.put("/test", mockResource);

    when(mockApiSpecification.getResources()).thenReturn(resources);
    when(mockApiSpecification.getVersion()).thenReturn("1.0");
    when(mockApiSpecification.getConsolidatedSchemas()).thenReturn(new HashMap<>());
    when(mockApiSpecification.getBaseUriParameters()).thenReturn(new HashMap<>());

    when(mockResource.getResolvedUri("1.0")).thenReturn("/test");
    when(mockResource.getResources()).thenReturn(new HashMap<>());

    ramlApiWrapper = new RamlApiWrapper(mockApiSpecification, mockNotifier);
  }

  @Test
  public void testCollectResources() {
    verify(mockResource).getResolvedUri("1.0");
    verify(mockResource).getResources();
  }

  @Test
  public void testGetMetadataSourceWithExistingResource() {
    when(mockResource.getAction("GET")).thenReturn(mockAction);

    ApiCoordinate coordinate = new ApiCoordinate("/test", "GET", "/test", null, null);
    Optional<MetadataSource> result = ramlApiWrapper.getMetadataSource(coordinate, "httpStatus", "outboundHeaders");

    assertTrue(result.isPresent());
    assertTrue(result.get() instanceof FlowMetadata);
  }

  @Test
  public void testGetMetadataSourceWithNonExistingResource() {
    ApiCoordinate coordinate = new ApiCoordinate("/nonexistent", "GET", null, null, null);
    Optional<MetadataSource> result = ramlApiWrapper.getMetadataSource(coordinate, "httpStatus", "outboundHeaders");

    assertFalse(result.isPresent());
  }

  @Test
  public void testGetMetadataSourceWithNonExistingMethod() {
    when(mockResource.getAction("POST")).thenReturn(null);

    ApiCoordinate coordinate = new ApiCoordinate("/test", "POST", null, null, null);
    Optional<MetadataSource> result = ramlApiWrapper.getMetadataSource(coordinate, "httpStatus", "outboundHeaders");

    assertFalse(result.isPresent());
  }

  @Test
  public void testGetConsolidatedSchemas() {
    Map<String, String> expectedSchemas = new HashMap<>();
    when(mockApiSpecification.getConsolidatedSchemas()).thenReturn(expectedSchemas);

    ramlApiWrapper = new RamlApiWrapper(mockApiSpecification, mockNotifier);
    Map<String, String> result = ramlApiWrapper.getConsolidatedSchemas();

    assertEquals(expectedSchemas, result);
  }
}
