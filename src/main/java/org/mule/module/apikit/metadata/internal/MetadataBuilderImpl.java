/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata.internal;

import org.mule.module.apikit.metadata.internal.model.MetadataModel;
import org.mule.runtime.apikit.metadata.api.Metadata;
import org.mule.runtime.apikit.metadata.api.MetadataBuilder;
import org.mule.runtime.apikit.metadata.api.Notifier;
import org.mule.runtime.apikit.metadata.api.ResourceLoader;
import org.mule.runtime.config.internal.model.ApplicationModel;

import static org.mule.apikit.common.ApiSyncUtils.isExchangeModules;
import static org.mule.apikit.common.ApiSyncUtils.toApiSyncResource;

/**
 * Builder for Metadata module
 */
public class MetadataBuilderImpl implements MetadataBuilder {

  private ResourceLoader resourceLoader;
  private ApplicationModel applicationModel;
  private Notifier notifier;

  public static final String MULE_APIKIT_PARSER = "mule.apikit.parser";

  public MetadataBuilderImpl() {

  }

  public MetadataBuilderImpl withResourceLoader(final ResourceLoader resourceLoader) {
    this.resourceLoader = resourceLoader;
    return this;
  }

  public MetadataBuilderImpl withApplicationModel(final ApplicationModel applicationModel) {
    this.applicationModel = applicationModel;
    return this;
  }

  public MetadataBuilderImpl withNotifier(final Notifier notifier) {
    this.notifier = notifier;
    return this;
  }

  public Metadata build() {
    return new MetadataModel(applicationModel, doMagic(resourceLoader), notifier);
  }

  private static ResourceLoader doMagic(final ResourceLoader resourceLoader) {
    return s -> {
      if (isExchangeModules(s)) {
        String apiSyncResource = toApiSyncResource(s);
        if (apiSyncResource != null)
          return resourceLoader.getResource(apiSyncResource);
      }
      return resourceLoader.getResource(s);
    };
  }
}
