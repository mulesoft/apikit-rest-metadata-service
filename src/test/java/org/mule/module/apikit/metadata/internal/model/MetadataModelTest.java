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
import org.mule.metadata.api.model.FunctionType;
import org.mule.module.apikit.metadata.internal.raml.RamlApiWrapper;
import org.mule.runtime.apikit.metadata.api.MetadataSource;
import org.mule.runtime.apikit.metadata.api.Notifier;
import org.mule.runtime.apikit.metadata.api.ResourceLoader;
import org.mule.runtime.ast.api.ArtifactAst;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.when;

class MetadataModelTest {

  @Mock
  private ArtifactAst artifactAst;
  @Mock
  private ResourceLoader resourceLoader;
  @Mock
  private Notifier notifier;
  @Mock
  private ApplicationModelWrapper applicationModelWrapper;
  @Mock
  private ApikitConfig apikitConfig;
  @Mock
  private MetadataSource metadataSource;
  @Mock
  private FunctionType functionType;
  @Mock
  private RamlApiWrapper ramlApiWrapper;
  private MetadataModel metadataModel;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    when(applicationModelWrapper.getApiCoordinate(anyString()))
        .thenReturn(Optional.of(new ApiCoordinate("configName", "flowName", "", "", "")));
    when(applicationModelWrapper.getConfig(anyString())).thenReturn(Optional.of(apikitConfig));
    when(apikitConfig.getMetadataResolver()).thenReturn(Optional.of(ramlApiWrapper));
    when(metadataSource.getMetadata()).thenReturn(Optional.of(functionType));
    metadataModel = new MetadataModel(artifactAst, resourceLoader, notifier);

  }

  @Test
  void testGetMetadataForFlow_NoCoordinate() {
    when(applicationModelWrapper.getApiCoordinate(anyString())).thenReturn(Optional.empty());
    Optional<FunctionType> result = metadataModel.getMetadataForFlow("flowName");
    assertFalse(result.isPresent());
  }

  @Test
  void testGetMetadataForFlow_NoConfig() {
    when(applicationModelWrapper.getConfig(anyString())).thenReturn(Optional.empty());
    Optional<FunctionType> result = metadataModel.getMetadataForFlow("flowName");
    assertFalse(result.isPresent());
  }

  @Test
  void testGetMetadataForFlow_NoMetadataResolver() {
    when(apikitConfig.getMetadataResolver()).thenReturn(Optional.empty());
    Optional<FunctionType> result = metadataModel.getMetadataForFlow("flowName");
    assertFalse(result.isPresent());
  }

  @Test
  void testGetMetadataForFlow_NoMetadataSource() {
    when(metadataSource.getMetadata()).thenReturn(Optional.empty());
    Optional<FunctionType> result = metadataModel.getMetadataForFlow("flowName");
    assertFalse(result.isPresent());
  }
}
