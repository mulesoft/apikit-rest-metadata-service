/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata.internal;

import com.google.common.collect.Lists;
import org.mule.runtime.api.service.ServiceDefinition;
import org.mule.runtime.api.service.ServiceProvider;
import org.mule.runtime.apikit.metadata.api.MetadataService;

import java.util.List;

public class MetadataServiceProvider implements ServiceProvider {

  @Override
  public List<ServiceDefinition> providedServices() {
    return Lists.newArrayList(new ServiceDefinition(MetadataService.class, new MetadataServiceImpl()));
  }
}
