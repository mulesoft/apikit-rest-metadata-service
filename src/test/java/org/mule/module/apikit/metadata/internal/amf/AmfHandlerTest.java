/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata.internal.amf;

import amf.apicontract.client.platform.model.domain.api.WebApi;
import org.junit.jupiter.api.Test;
import org.mule.module.apikit.metadata.internal.model.MetadataResolver;
import org.mule.runtime.apikit.metadata.api.Notifier;
import org.mule.runtime.apikit.metadata.api.ResourceLoader;

import java.util.Optional;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

class AmfHandlerTest {

  private ResourceLoader resourceLoader = mock(ResourceLoader.class);
  private Notifier notifier = mock(Notifier.class);
  private AmfHandler amfHandler = new AmfHandler(resourceLoader, notifier);

  @Test
  void testGetMetadataResolverWithEmptyApiDefinition() {
    Optional<MetadataResolver> metadataResolver = amfHandler.getMetadataResolver("");
    assertTrue(!metadataResolver.isPresent());
  }

  @Test
  void testGetApiWithEmptyApiDefinition() {
    Optional<WebApi> webApi = amfHandler.getApi("");
    assertTrue(!webApi.isPresent());
  }
}
