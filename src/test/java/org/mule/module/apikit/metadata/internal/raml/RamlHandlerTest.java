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
import org.mule.apikit.model.ApiSpecification;
import org.mule.module.apikit.metadata.internal.model.MetadataResolver;
import org.mule.parser.service.ParserService;
import org.mule.parser.service.result.ParseResult;
import org.mule.runtime.apikit.metadata.api.Notifier;
import org.mule.runtime.apikit.metadata.api.ResourceLoader;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class RamlHandlerTest {

  @Mock
  private ResourceLoader resourceLoader;

  @Mock
  private Notifier notifier;

  @Mock
  private ParserService parserService;

  @Mock
  private ParseResult parseResult;

  @Mock
  private ApiSpecification apiSpecification;

  private RamlHandler ramlHandler;

  @BeforeEach
  void setup() {
    MockitoAnnotations.initMocks(this);
    ramlHandler = new RamlHandler(resourceLoader, notifier);
    // Use reflection to set the mocked ParserService
    try {
      java.lang.reflect.Field field = RamlHandler.class.getDeclaredField("SERVICE");
      field.setAccessible(true);
      field.set(ramlHandler, parserService);
    } catch (Exception e) {
      fail("Failed to set mocked ParserService");
    }
  }

  @Test
  void testGetMetadataResolverWithValidApiDefinition() {
    String validApiDefinition = "valid-api.raml";
    when(parserService.parse(any())).thenReturn(parseResult);
    when(parseResult.success()).thenReturn(true);
    when(parseResult.get()).thenReturn(apiSpecification);

    Optional<MetadataResolver> result = ramlHandler.getMetadataResolver(validApiDefinition);

    assertTrue(result.isPresent());
    assertTrue(result.get() instanceof RamlApiWrapper);
  }

  @Test
  void testGetMetadataResolverWithEmptyApiDefinition() {
    String emptyApiDefinition = "";

    Optional<MetadataResolver> result = ramlHandler.getMetadataResolver(emptyApiDefinition);

    assertFalse(result.isPresent());
    verify(notifier).error("RAML document is undefined.");
  }

  @Test
  void testGetApiWithValidUri() {
    String validUri = "valid-api.raml";
    when(parserService.parse(any())).thenReturn(parseResult);
    when(parseResult.success()).thenReturn(true);
    when(parseResult.get()).thenReturn(apiSpecification);

    Optional<ApiSpecification> result = ramlHandler.getApi(validUri);

    assertTrue(result.isPresent());
    assertEquals(apiSpecification, result.get());
  }

  @Test
  void testGetApiWithEmptyUri() {
    String emptyUri = "";

    Optional<ApiSpecification> result = ramlHandler.getApi(emptyUri);

    assertFalse(result.isPresent());
    verify(notifier).error("RAML document is undefined.");
  }

  @Test
  void testGetApiWithExceptionThrown() {
    String exceptionUri = "exception-api.raml";
    when(parserService.parse(any())).thenThrow(new RuntimeException("Test exception"));

    Optional<ApiSpecification> result = ramlHandler.getApi(exceptionUri);

    assertFalse(result.isPresent());
    verify(notifier).error(contains("Error reading RAML document"));
  }
}
