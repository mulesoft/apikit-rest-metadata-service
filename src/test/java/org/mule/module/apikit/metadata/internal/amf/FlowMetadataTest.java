/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.apikit.metadata.internal.amf;

import amf.apicontract.client.platform.model.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mule.metadata.api.model.FunctionType;
import org.mule.module.apikit.metadata.internal.model.ApiCoordinate;
import org.mule.runtime.apikit.metadata.api.Notifier;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FlowMetadataTest {

  @Mock
  private EndPoint endPoint;

  @Mock
  private Operation operation;

  @Mock
  private ApiCoordinate coordinate;

  @Mock
  private Notifier notifier;

  @Mock
  private Response response;
  private FlowMetadata flowMetadata;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.initMocks(this);
    Map<String, Parameter> baseUriParameters = new HashMap<>();
    flowMetadata = new FlowMetadata(endPoint, operation, coordinate, baseUriParameters, notifier);
  }

  @Test
  void testGetMetadata() {

    Response response = new Response();
    response.withStatusCode("200");
    when(operation.responses()).thenReturn(Collections.singletonList(response));
    when(operation.request()).thenReturn(mock(Request.class));
    Optional<FunctionType> result = flowMetadata.getMetadata();

    assertTrue(result.isPresent());
    FunctionType functionType = result.get();
    assertEquals(1, functionType.getParameters().size());
    assertNotNull(functionType.getReturnType());
  }

  @Test
  void testGetMetadataWithNoRequestAndResponse() {
    when(operation.request()).thenReturn(null);
    when(operation.responses()).thenReturn(Collections.emptyList());

    Optional<FunctionType> result = flowMetadata.getMetadata();

    assertTrue(result.isPresent());
    FunctionType functionType = result.get();
    assertEquals(1, functionType.getParameters().size());
    assertNotNull(functionType.getReturnType());
  }

}
