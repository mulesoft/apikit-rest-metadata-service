/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata.internal.raml;

import java.net.URI;
import java.util.Optional;
import org.mule.apikit.model.api.ApiReference;
import org.mule.module.apikit.metadata.internal.model.MetadataResolver;
import org.mule.module.apikit.metadata.internal.model.MetadataResolverFactory;
import org.mule.apikit.model.ApiSpecification;
import org.mule.parser.service.ParserMode;
import org.mule.parser.service.result.ParseResult;
import org.mule.runtime.apikit.metadata.api.Notifier;
import org.mule.runtime.apikit.metadata.api.ResourceLoader;
import org.mule.runtime.core.api.util.StringUtils;

import static java.lang.String.format;
import static java.util.Optional.empty;
import static java.util.Optional.of;

public class RamlHandler implements MetadataResolverFactory {

  private final ResourceLoader resourceLoader;
  private final Notifier notifier;
  private final org.mule.parser.service.ParserService SERVICE = new org.mule.parser.service.ParserService();

  public RamlHandler(ResourceLoader resourceLoader, Notifier notifier) {
    this.resourceLoader = resourceLoader;
    this.notifier = notifier;
  }

  @Override
  public Optional<MetadataResolver> getMetadataResolver(String apiDefinition) {
    return getApi(apiDefinition).map(raml -> new RamlApiWrapper(raml, notifier));
  }

  public Optional<ApiSpecification> getApi(String uri) {
    try {

      if (StringUtils.isEmpty(uri)) {
        notifier.error("RAML document is undefined.");
        return empty();
      }

      ParseResult result =
          SERVICE.parse(ApiReference.create(uri, new ResourceLoaderAdapter(resourceLoader)), ParserMode.RAML);
      if (!result.success()) {
        result.getErrors().forEach(error -> notifier.error(error.cause()));
        return empty();
      }
      return of(result.get());

    } catch (Exception e) {
      notifier.error(format("Error reading RAML document '%s'. Detail: %s", uri, e.getMessage()));
    }

    return empty();
  }

  private class ResourceLoaderAdapter implements org.mule.apikit.loader.ResourceLoader {

    private final ResourceLoader resourceLoader;

    ResourceLoaderAdapter(ResourceLoader resourceLoader) {
      this.resourceLoader = resourceLoader;
    }

    @Override
    public URI getResource(String res) {
      return resourceLoader.getResource(res);
    }

  }
}
