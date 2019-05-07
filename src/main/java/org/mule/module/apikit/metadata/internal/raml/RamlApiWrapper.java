/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata.internal.raml;

import java.util.Optional;

import org.mule.apikit.model.parameter.Parameter;
import org.mule.module.apikit.metadata.internal.model.ApiCoordinate;
import org.mule.module.apikit.metadata.internal.model.MetadataResolver;
import org.mule.apikit.model.ApiSpecification;
import org.mule.apikit.model.Resource;

import java.util.HashMap;
import java.util.Map;
import org.mule.runtime.apikit.metadata.api.MetadataSource;
import org.mule.runtime.apikit.metadata.api.Notifier;

import static java.util.Optional.ofNullable;

public class RamlApiWrapper implements MetadataResolver {

  private final Map<String, Resource> ramlResources = new HashMap<>();
  private final Map<String, Parameter> baseUriParameters;
  private final Map<String, String> consolidatedSchemas;
  private final Notifier notifier;

  public RamlApiWrapper(ApiSpecification ramlApi, Notifier notifier) {
    collectResources(ramlApi.getResources(), ramlApi.getVersion());
    consolidatedSchemas = ramlApi.getConsolidatedSchemas();
    this.baseUriParameters = ramlApi.getBaseUriParameters();
    this.notifier = notifier;
  }

  private void collectResources(Map<String, Resource> resources, String version) {
    resources.values().forEach(resource -> {
      ramlResources.put(resource.getResolvedUri(version), resource);
      collectResources(resource.getResources(), version);
    });
  }

  public Optional<MetadataSource> getMetadataSource(ApiCoordinate coordinate, String httpStatusVar,
                                                    String outboundHeadersVar) {
    return ofNullable(ramlResources.get(coordinate.getResource()))
        .map(resource -> resource.getAction(coordinate.getMethod()))
        .map(action -> new FlowMetadata(this, action, coordinate, baseUriParameters, httpStatusVar, outboundHeadersVar,
                                        notifier));
  }

  Map<String, String> getConsolidatedSchemas() {
    return consolidatedSchemas;
  }
}


