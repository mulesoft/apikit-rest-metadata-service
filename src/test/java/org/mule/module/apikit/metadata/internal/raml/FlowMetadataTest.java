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
import org.mule.apikit.model.Action;
import org.mule.apikit.model.Resource;
import org.mule.apikit.model.parameter.Parameter;
import org.mule.metadata.api.model.FunctionType;
import org.mule.metadata.message.api.MuleEventMetadataType;
import org.mule.module.apikit.metadata.internal.model.ApiCoordinate;
import org.mule.runtime.apikit.metadata.api.Notifier;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.*;
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

    private FlowMetadata flowMetadata;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        Map<String, Parameter> baseUriParameters = new HashMap<>();
        flowMetadata = new FlowMetadata(api, action, coordinate, baseUriParameters, "httpStatus", "outboundHeaders",
                notifier);
    }

    @Test
    public void testGetMetadata() {
        when(action.getQueryParameters()).thenReturn(new HashMap<>());
        when(action.getHeaders()).thenReturn(new HashMap<>());
        when(action.getResource()).thenReturn(mock(Resource.class));
        when(action.getResponses()).thenReturn(new HashMap<>());

        Optional<FunctionType> result = flowMetadata.getMetadata();

        assertTrue(result.isPresent());
        FunctionType functionType = result.get();
        assertEquals(1, functionType.getParameters().size());
        assertTrue(functionType.getReturnType().get() instanceof MuleEventMetadataType);
    }

}
