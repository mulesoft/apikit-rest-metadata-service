/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata.internal;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mule.runtime.apikit.metadata.api.Metadata;
import org.mule.runtime.apikit.metadata.api.Notifier;
import org.mule.runtime.apikit.metadata.api.ResourceLoader;
import org.mule.runtime.ast.api.ArtifactAst;
import org.mule.module.apikit.metadata.internal.model.MetadataModel;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class MetadataBuilderImplTest {

  private MetadataBuilderImpl metadataBuilder;

  @Mock
  private ResourceLoader mockResourceLoader;

  @Mock
  private ArtifactAst mockApplicationModel;

  @Mock
  private Notifier mockNotifier;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    metadataBuilder = new MetadataBuilderImpl();
  }

  @Test
  public void testWithResourceLoader() {
    MetadataBuilderImpl result = metadataBuilder.withResourceLoader(mockResourceLoader);
    assertSame(metadataBuilder, result);
  }

  @Test
  public void testWithApplicationModel() {
    MetadataBuilderImpl result = metadataBuilder.withApplicationModel(mockApplicationModel);
    assertSame(metadataBuilder, result);
  }

  @Test
  public void testWithNotifier() {
    MetadataBuilderImpl result = metadataBuilder.withNotifier(mockNotifier);
    assertSame(metadataBuilder, result);
  }

  @Test
  public void testBuild() {
    metadataBuilder.withResourceLoader(mockResourceLoader)
        .withApplicationModel(mockApplicationModel)
        .withNotifier(mockNotifier);

    Metadata result = metadataBuilder.build();

    assertNotNull(result);
    assertTrue(result instanceof MetadataModel);
  }

  @Test
  public void testBuildWithoutSettings() {
    assertThrows(NullPointerException.class, () -> metadataBuilder.build());
  }

  @Test
  public void testMuleApikitParserConstant() {
    assertEquals("mule.apikit.parser", MetadataBuilderImpl.MULE_APIKIT_PARSER);
  }
}
