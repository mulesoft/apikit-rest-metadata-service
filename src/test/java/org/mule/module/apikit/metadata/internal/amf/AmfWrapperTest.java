/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata.internal.amf;

import amf.apicontract.client.platform.model.domain.EndPoint;
import amf.apicontract.client.platform.model.domain.Operation;
import amf.apicontract.client.platform.model.domain.Parameter;
import amf.apicontract.client.platform.model.domain.Server;
import amf.apicontract.client.platform.model.domain.api.WebApi;
import amf.core.client.platform.model.StrField;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mule.module.apikit.metadata.internal.model.ApiCoordinate;
import org.mule.runtime.apikit.metadata.api.MetadataSource;
import org.mule.runtime.apikit.metadata.api.Notifier;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class AmfWrapperTest {

  @Mock
  private WebApi webApi;

  @Mock
  private Notifier notifier;

  @Mock
  private EndPoint endPoint;

  @Mock
  private Operation operation;

  @Mock
  private Server server;

  @Mock
  private Parameter parameter;

  @Mock
  private StrField endpointPath;

  @Mock
  private StrField webApiVersion;

  @Mock
  private StrField parameterName;

  @Mock
  private StrField operationMethod;

  private AmfWrapper amfWrapper;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
    when(webApi.endPoints()).thenReturn(Collections.singletonList(endPoint));
    when(endPoint.path()).thenReturn(endpointPath);
    when(endpointPath.value()).thenReturn("/api/{version}/resource");
    when(webApi.version()).thenReturn(webApiVersion);
    when(webApiVersion.value()).thenReturn("v1");
    when(webApi.servers()).thenReturn(Collections.singletonList(server));
    when(server.variables()).thenReturn(Collections.singletonList(parameter));
    when(parameter.name()).thenReturn(parameterName);
    when(parameterName.value()).thenReturn("baseUri");
    amfWrapper = new AmfWrapper(webApi, notifier);
  }

  @Test
  public void testResolveVersion() {
    String result = amfWrapper.resolveVersion("/api/{version}/resource", "v1");
    assertEquals("/api/v1/resource", result);
  }

  @Test
  public void testResolveVersionWithoutVersionPlaceholder() {
    String result = amfWrapper.resolveVersion("/api/resource", "v1");
    assertEquals("/api/resource", result);
  }

  @Test
  public void testResolveVersionWithEmptyVersion() {
    String result = amfWrapper.resolveVersion("/api/{version}/resource", "");
    assertEquals("/api/{version}/resource", result);
  }

  @Test
  public void testGetMetadataSourceNotFound() {
    ApiCoordinate coordinate =
        new ApiCoordinate("config1", "/non-existent", "POST", "application/json", "config1:POST:/non-existent");
    Optional<MetadataSource> result = amfWrapper.getMetadataSource(coordinate, "httpStatus", "outboundHeaders");

    assertFalse(result.isPresent());
  }

  @Test
  public void testGetMetadataSourceOperationNotFound() {
    when(endPoint.operations()).thenReturn(Collections.singletonList(operation));
    when(operation.method()).thenReturn(operationMethod);
    when(operationMethod.value()).thenReturn("POST");

    ApiCoordinate coordinate =
        new ApiCoordinate("config1", "/api/v1/resource", "POST", "application/json", "config1:POST:/api/v1/resource");
    Optional<MetadataSource> result = amfWrapper.getMetadataSource(coordinate, "httpStatus", "outboundHeaders");

    assertFalse(result.isPresent());
  }

  @Test
  public void testBaseUriParametersWithEmptyServers() {
    when(webApi.servers()).thenReturn(Collections.emptyList());
    AmfWrapper emptyServerAmfWrapper = new AmfWrapper(webApi, notifier);

    ApiCoordinate coordinate =
        new ApiCoordinate("config1", "/api/v1/resource", "GET", "application/json", "config1:GET:/api/v1/resource");
    Optional<MetadataSource> result = emptyServerAmfWrapper.getMetadataSource(coordinate, "httpStatus", "outboundHeaders");

    assertFalse(result.isPresent());
  }
}
