/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata.utils;

import org.mule.module.apikit.metadata.MetadataTestCase;
import org.mule.runtime.apikit.metadata.api.ResourceLoader;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class TestResourceLoader implements ResourceLoader {

  @Override
  public URI getResource(String relativePath) {
    try {
      URL resource = MetadataTestCase.class.getResource(relativePath);
      if (resource == null)
        return null;
      return resource.toURI();

    } catch (URISyntaxException e) {
      e.printStackTrace();
    }

    return null;
  }
}