/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata.internal.raml;

import java.net.URI;
import org.mule.apikit.implv1.ParserV1Utils;
import org.mule.apikit.implv1.loader.ApiSyncResourceLoader;
import org.mule.apikit.model.ApiSpecification;

import javax.annotation.Nullable;
import java.io.InputStream;
import org.mule.runtime.apikit.metadata.api.ResourceLoader;

class ParserWrapperV1 implements ParserWrapper {

  private final String ramlPath;
  private String content;
  private final ResourceLoader resourceLoader;

  ParserWrapperV1(final String ramlPath, final String content, final ResourceLoader resourceLoader) {
    this.ramlPath = ramlPath;
    this.content = content;
    this.resourceLoader = resourceLoader;
  }

  @Override
  public ApiSpecification build() {
    return ParserV1Utils.build(content, new ApiSyncResourceLoader(ramlPath, adaptResourceLoader(resourceLoader)), ramlPath);
  }

  private org.raml.parser.loader.ResourceLoader adaptResourceLoader(ResourceLoader resourceLoader) {
    return new org.raml.parser.loader.ResourceLoader() {

      @Nullable
      @Override
      public InputStream fetchResource(String s) {
        final URI uri = resourceLoader.getResource(s);
        return ParserWrapper.toInputStream(uri);
      }
    };
  }
}

