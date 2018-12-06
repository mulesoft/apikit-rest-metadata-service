/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata.internal.raml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.mule.module.apikit.metadata.internal.model.MetadataResolver;
import org.mule.module.apikit.metadata.internal.model.MetadataResolverFactory;
import org.mule.raml.interfaces.model.IRaml;
import org.mule.runtime.apikit.metadata.api.Notifier;
import org.mule.runtime.apikit.metadata.api.ResourceLoader;
import org.mule.runtime.core.api.util.StringUtils;

import static java.lang.Boolean.getBoolean;
import static java.lang.String.format;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.mule.raml.implv2.ParserV2Utils.PARSER_V2_PROPERTY;

public class RamlHandler implements MetadataResolverFactory {

  private final ResourceLoader resourceLoader;
  private final Notifier notifier;

  public RamlHandler(ResourceLoader resourceLoader, Notifier notifier) {
    this.resourceLoader = resourceLoader;
    this.notifier = notifier;
  }

  @Override
  public Optional<MetadataResolver> getMetadataResolver(String apiDefinition) {
    return getApi(apiDefinition).map(raml -> new RamlApiWrapper(raml, notifier));
  }

  public Optional<IRaml> getApi(String uri) {
    try {

      if (StringUtils.isEmpty(uri)) {
        notifier.error("RAML document is undefined.");
        return empty();
      }

      final URI resource = resourceLoader.getResource(uri);
      final File file = resource != null ? new File(resource) : null;

      if (file == null) {
        notifier.error(format("RAML document '%s' not found.", uri));
        return empty();
      }

      final String content = getRamlContent(file);
      final Parseable parser = getParser(content);

      return of(parser.build(file, content));
    } catch (Exception e) {
      notifier.error(format("Error reading RAML document '%s'. Detail: %s", uri, e.getMessage()));
    }

    return empty();
  }

  private Parseable getParser(String ramlContent) {
    return useParserV2(ramlContent) ? new RamlV2Parser() : new RamlV1Parser();
  }

  private String getRamlContent(File uri) throws IOException {
    try (final InputStream is = new FileInputStream(uri)) {
      return IOUtils.toString(is);
    }
  }

  private static boolean useParserV2(String content) {
    return getBoolean(PARSER_V2_PROPERTY) || content.startsWith("#%RAML 1.0");
  }
}
