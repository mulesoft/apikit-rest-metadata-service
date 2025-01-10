/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata.internal.amf;

import amf.apicontract.client.platform.model.domain.EndPoint;
import amf.apicontract.client.platform.model.domain.Operation;
import amf.apicontract.client.platform.model.domain.api.WebApi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mule.module.apikit.metadata.internal.model.ApiCoordinate;
import org.mule.module.apikit.metadata.utils.TestNotifier;
import org.mule.runtime.apikit.metadata.api.MetadataSource;
import org.mule.runtime.apikit.metadata.api.Notifier;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AmfWrapperTest {


  private Notifier testNotifier;

  @BeforeEach
  void setUp() {
    testNotifier = new TestNotifier();
  }

  @Test
  void getMetadataSource() {
    // ApiCoordinate coordinate = new ApiCoordinate("/test", "GET", "/test", null, null);
    //
    // Operation operation = new Operation();
    // operation.withMethod("GET");
    //
    // EndPoint endPoint = new EndPoint();
    // endPoint.withPath("/test");
    // endPoint.withOperations(Collections.singletonList(operation));
    //
    //
    // WebApi webApi = mock(WebApi.class);
    // when(webApi.endPoints()).thenReturn(Collections.singletonList(endPoint));
    // when(webApi.version()).thenReturn(Collections.singletonList(endPoint));
    //
    //
    //
    // AmfWrapper wrapper = new AmfWrapper(webApi, testNotifier);
    //
    // ApiCoordinate coordinate = new ApiCoordinate("/test", "GET", null, null, null);
    // Optional<MetadataSource> result = wrapper.getMetadataSource(coordinate, "200", "outboundHeaders");
    //
    // assertTrue(result.isPresent());
  }

  @Test
  void resolveVersion() {}
}
