/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata.internal.amf;

import amf.apicontract.client.platform.model.domain.api.WebApi;
import java.io.InputStream;
import java.net.URI;
import java.util.Optional;
import org.mule.amf.impl.AMFParser;
import org.mule.apikit.validation.ApiValidationReport;
import org.mule.module.apikit.metadata.internal.model.MetadataResolver;
import org.mule.module.apikit.metadata.internal.model.MetadataResolverFactory;
import org.mule.apikit.model.api.ApiReference;
import org.mule.runtime.apikit.metadata.api.Notifier;
import org.mule.runtime.apikit.metadata.api.ResourceLoader;
import org.mule.runtime.core.api.util.StringUtils;

import static java.lang.String.format;
import static java.util.Optional.empty;
import static java.util.Optional.of;

public class AmfHandler implements MetadataResolverFactory {

  private final ResourceLoader resourceLoader;
  private final Notifier notifier;

  public AmfHandler(ResourceLoader resourceLoader, Notifier notifier) {
    this.resourceLoader = resourceLoader;
    this.notifier = notifier;
  }

  @Override
  public Optional<MetadataResolver> getMetadataResolver(final String apiDefinition) {
    return getApi(apiDefinition).map(webApi -> new AmfWrapper(webApi, notifier));
  }

  Optional<WebApi> getApi(final String apiDefinition) {

    if (StringUtils.isEmpty(apiDefinition)) {
      notifier.error("API definition is undefined using AMF parser.");
      return empty();
    }

    try {
      ApiReference apiRef = ApiReference.create(apiDefinition, adaptResourceLoader(resourceLoader));
      AMFParser parserWrapper = new AMFParser(apiRef);
      ApiValidationReport report = parserWrapper.validate();
      if (!report.conforms()) {
        report.getResults().stream().forEach(result -> notifier
            .info(format("Error reading API definition using AMF parser. Detail: %s", result.getMessage())));
        return empty();
      }
      notifier.info(format("Metadata for API definition '%s' was generated using AMF parser.", apiDefinition));
      return of(parserWrapper.getWebApi());
    } catch (Exception e) {
      notifier.error(format("Error reading API definition '%s' using AMF parser. Detail: %s", apiDefinition, e.getMessage()));
      return empty();
    }

  }

  private static org.mule.apikit.loader.ResourceLoader adaptResourceLoader(ResourceLoader resourceLoader) {
    return new org.mule.apikit.loader.ResourceLoader() {

      @Override
      public URI getResource(final String path) {
        return resourceLoader.getResource(path);
      }

      public InputStream getResourceAsStream(String relativePath) {
        return resourceLoader.getResourceAsStream(relativePath);
      }
    };
  }

}
