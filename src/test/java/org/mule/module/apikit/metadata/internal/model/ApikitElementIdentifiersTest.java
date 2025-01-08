/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata.internal.model;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mule.runtime.api.component.ComponentIdentifier;
import org.mule.runtime.ast.api.ComponentAst;

import static org.junit.jupiter.api.Assertions.*;

class ApikitElementIdentifiersTest {

  @Test
  void testIsFlow() {
    ComponentAst component = Mockito.mock(ComponentAst.class);
    ComponentIdentifier identifier = ComponentIdentifier.buildFromStringRepresentation("flow");
    Mockito.when(component.getIdentifier()).thenReturn(identifier);

    assertTrue(ApikitElementIdentifiers.isFlow(component));
  }

  @Test
  void testIsNotFlow() {
    ComponentAst component = Mockito.mock(ComponentAst.class);
    ComponentIdentifier identifier = ComponentIdentifier.buildFromStringRepresentation("not-flow");
    Mockito.when(component.getIdentifier()).thenReturn(identifier);

    assertFalse(ApikitElementIdentifiers.isFlow(component));
  }

  @Test
  void testIsApikitConfig() {
    ComponentAst component = Mockito.mock(ComponentAst.class);
    ComponentIdentifier identifier = ComponentIdentifier.buildFromStringRepresentation("apikit:config");
    Mockito.when(component.getIdentifier()).thenReturn(identifier);

    assertTrue(ApikitElementIdentifiers.isApikitConfig(component));
  }

  @Test
  void testIsNotApikitConfig() {
    ComponentAst component = Mockito.mock(ComponentAst.class);
    ComponentIdentifier identifier = ComponentIdentifier.buildFromStringRepresentation("not-apikit:config");
    Mockito.when(component.getIdentifier()).thenReturn(identifier);

    assertFalse(ApikitElementIdentifiers.isApikitConfig(component));
  }

  @Test
  void testIsFlowMappings() {
    ComponentIdentifier identifier = ComponentIdentifier.buildFromStringRepresentation("apikit:flow-mappings");
    assertTrue(ApikitElementIdentifiers.isFlowMappings(identifier));
  }

  @Test
  void testIsNotFlowMappings() {
    ComponentIdentifier identifier = ComponentIdentifier.buildFromStringRepresentation("not-apikit:flow-mappings");
    assertFalse(ApikitElementIdentifiers.isFlowMappings(identifier));
  }

  @Test
  void testIsFlowMapping() {
    ComponentIdentifier identifier = ComponentIdentifier.buildFromStringRepresentation("apikit:flow-mapping");
    assertTrue(ApikitElementIdentifiers.isFlowMapping(identifier));
  }

  @Test
  void testIsNotFlowMapping() {
    ComponentIdentifier identifier = ComponentIdentifier.buildFromStringRepresentation("not-apikit:flow-mapping");
    assertFalse(ApikitElementIdentifiers.isFlowMapping(identifier));
  }
}
