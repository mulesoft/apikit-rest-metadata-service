/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata.internal.raml;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mule.apikit.model.Action;
import org.mule.apikit.model.MimeType;
import org.mule.apikit.model.Resource;
import org.mule.apikit.model.Response;
import org.mule.apikit.model.parameter.Parameter;
import org.mule.metadata.api.model.FunctionType;
import org.mule.metadata.message.api.MuleEventMetadataType;
import org.mule.module.apikit.metadata.internal.model.ApiCoordinate;
import org.mule.runtime.apikit.metadata.api.Notifier;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class FlowMetadataTest {

  @Mock
  private RamlApiWrapper api;
  @Mock
  private Action action;
  @Mock
  private ApiCoordinate coordinate;
  @Mock
  private Notifier notifier;
  @Mock
  private Resource resource;
  @Mock
  private Response response;
  @Mock
  private MimeType mimeType;

  private FlowMetadata flowMetadata;
  private Map<String, Parameter> baseUriParameters;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    baseUriParameters = new HashMap<>();
    flowMetadata = new FlowMetadata(api, action, coordinate, baseUriParameters, "httpStatus", "outboundHeaders", notifier);

    when(action.getResource()).thenReturn(resource);
    when(action.hasBody()).thenReturn(true);
    when(resource.getResolvedUriParameters()).thenReturn(Collections.emptyMap());
  }

  @Test
  public void testGetMetadata() {
    when(action.getQueryParameters()).thenReturn(new HashMap<>());
    when(action.getHeaders()).thenReturn(new HashMap<>());
    when(action.getResponses()).thenReturn(Collections.singletonMap("200", response));
    when(response.getHeaders()).thenReturn(new HashMap<>());
    when(response.hasBody()).thenReturn(true);
    when(response.getBody()).thenReturn(Collections.singletonMap("application/json", mimeType));

    Optional<FunctionType> result = flowMetadata.getMetadata();

    assertTrue(result.isPresent());
    FunctionType functionType = result.get();
    assertEquals(1, functionType.getParameters().size());
    assertTrue(functionType.getReturnType().get() instanceof MuleEventMetadataType);
  }

  @Test
  public void testMetadataWithNoResponses() {
    when(action.getResponses()).thenReturn(Collections.emptyMap());
    when(action.getQueryParameters()).thenReturn(Collections.emptyMap());
    when(action.getHeaders()).thenReturn(Collections.emptyMap());
    when(coordinate.getMediaType()).thenReturn("application/json");

    Optional<FunctionType> result = flowMetadata.getMetadata();

    assertTrue(result.isPresent());
    FunctionType functionType = result.get();
    MuleEventMetadataType outputEvent = (MuleEventMetadataType) functionType.getReturnType().get();

    assertNotNull(outputEvent.getMessageType().getFields());
  }

  @Test
  public void testMetadataWithOneResponse() {
    when(action.getResponses()).thenReturn(Collections.emptyMap());
    when(action.getQueryParameters()).thenReturn(Collections.emptyMap());
    when(action.getHeaders()).thenReturn(Collections.emptyMap());
    when(coordinate.getMediaType()).thenReturn("application/json");
    when(action.getBody()).thenReturn(Collections.singletonMap("application/json", mimeType));

    Optional<FunctionType> result = flowMetadata.getMetadata();

    assertTrue(result.isPresent());
    FunctionType functionType = result.get();
    MuleEventMetadataType outputEvent = (MuleEventMetadataType) functionType.getReturnType().get();

    assertNotNull(outputEvent.getMessageType().getFields());
  }
}
