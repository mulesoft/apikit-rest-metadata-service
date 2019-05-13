/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata.internal.raml;

import java.io.InputStream;
import java.net.URI;
import javax.annotation.Nullable;
import org.mule.apikit.implv2.ParserV2Utils;
import org.mule.apikit.implv2.loader.ApiSyncResourceLoader;
import org.mule.apikit.model.ApiSpecification;
import org.mule.runtime.apikit.metadata.api.ResourceLoader;

class ParserWrapperV2 implements ParserWrapper {

  private final String ramlPath;
  private String content;
  private final ResourceLoader resourceLoader;

  ParserWrapperV2(String ramlPath, final String content, ResourceLoader resourceLoader) {
    this.ramlPath = ramlPath;
    this.content = content;
    this.resourceLoader = resourceLoader;
  }

  @Override
  public ApiSpecification build() {
    return ParserV2Utils.build(new ApiSyncResourceLoader(ramlPath, adaptResourceLoader(resourceLoader)), ramlPath, content);
  }

  private org.raml.v2.api.loader.ResourceLoader adaptResourceLoader(final ResourceLoader resourceLoader) {
    return new org.raml.v2.api.loader.ResourceLoader() {

      @Nullable
      @Override
      public InputStream fetchResource(String s) {
        final URI uri = resourceLoader.getResource(s);
        return ParserWrapper.toInputStream(uri);
      }
    };
  }
}
