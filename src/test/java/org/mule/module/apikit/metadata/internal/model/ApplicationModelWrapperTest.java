/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata.internal.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mule.runtime.apikit.metadata.api.Notifier;
import org.mule.runtime.apikit.metadata.api.ResourceLoader;
import org.mule.runtime.ast.api.ArtifactAst;
import org.mule.runtime.ast.api.ComponentAst;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mule.runtime.api.component.ComponentIdentifier.buildFromStringRepresentation;

public class ApplicationModelWrapperTest {

  @Mock
  private ArtifactAst mockArtifactAst;
  @Mock
  private ResourceLoader mockResourceLoader;
  @Mock
  private Notifier mockNotifier;
  private ApplicationModelWrapper applicationModelWrapper;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    applicationModelWrapper = new ApplicationModelWrapper(mockArtifactAst, mockResourceLoader, mockNotifier);
  }

  @Test
  public void testGetApiCoordinateWithNonExistingFlow() {
    Optional<ApiCoordinate> result = applicationModelWrapper.getApiCoordinate("nonExistingFlow");
    assertFalse(result.isPresent());
  }

  @Test
  public void testFindFlows() {
    ComponentAst flow1 = mock(ComponentAst.class);
    ComponentAst flow2 = mock(ComponentAst.class);
    when(mockArtifactAst.topLevelComponentsStream()).thenReturn(Arrays.asList(flow1, flow2).stream());
    when(flow1.getIdentifier()).thenReturn(buildFromStringRepresentation("flow"));
    when(flow2.getIdentifier()).thenReturn(buildFromStringRepresentation("flow"));
    when(flow1.getComponentId()).thenReturn(Optional.of("flow1"));
    when(flow2.getComponentId()).thenReturn(Optional.of("flow2"));

    List<Flow> flows = applicationModelWrapper.findFlows();

    assertEquals(2, flows.size());
    assertEquals("flow1", flows.get(0).getName());
    assertEquals("flow2", flows.get(1).getName());
  }

}
