/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata.utils;

import java.io.InputStream;
import java.util.Optional;
import java.util.Set;
import org.mule.runtime.api.meta.model.ExtensionModel;
import org.mule.runtime.config.api.dsl.model.ComponentBuildingDefinitionRegistry;
import org.mule.runtime.config.api.dsl.model.ResourceProvider;
import org.mule.runtime.config.api.dsl.processor.ArtifactConfig;
import org.mule.runtime.dsl.api.xml.parser.ConfigLine;

// THIS CLASS WAS COPIED FROM git@github.com:mulesoft/mule-datasense-api.git
class MuleAppUtil {

  public static Optional<ConfigLine> loadConfigLines(InputStream inputStream) {
    return MuleAppHelper.loadConfigLines(inputStream);
  }

  public static org.mule.runtime.config.internal.model.ApplicationModel loadApplicationModel(
                                                                                             ArtifactConfig artifactConfig,
                                                                                             String fileName,
                                                                                             Set<ExtensionModel> extensionModels,
                                                                                             Optional<ComponentBuildingDefinitionRegistry> componentBuildingDefinitionRegistry,
                                                                                             boolean runtimeMode,
                                                                                             ResourceProvider externalResourceProvider)
      throws Exception {
    return MuleAppHelper.loadApplicationModel(artifactConfig, fileName, extensionModels, componentBuildingDefinitionRegistry,
                                              runtimeMode, externalResourceProvider);
  }

  public static Optional<org.mule.runtime.config.internal.model.ApplicationModel> createInternalApplicationModel(String name,
                                                                                                                 InputStream inputStream) {
    return MuleAppHelper.createInternalApplicationModel(name, inputStream);
  }
}
