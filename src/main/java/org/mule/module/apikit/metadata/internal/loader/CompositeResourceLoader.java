/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata.internal.loader;

import java.net.URI;
import org.mule.runtime.apikit.metadata.api.ResourceLoader;

public class CompositeResourceLoader implements ResourceLoader {

  private final ResourceLoader[] resourceLoaders;

  public CompositeResourceLoader(ResourceLoader... resourceLoaders) {
    this.resourceLoaders = resourceLoaders;
  }

  @Override
  public URI getResource(String s) {
    for (ResourceLoader loader : resourceLoaders) {
      URI resourceURI = loader.getResource(s);
      if (resourceURI != null) {
        return resourceURI;
      }
    }
    return null;
  }
}
