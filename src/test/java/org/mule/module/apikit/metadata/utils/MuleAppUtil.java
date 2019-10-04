/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata.utils;

import org.mule.runtime.api.meta.model.ExtensionModel;
import org.mule.runtime.ast.api.ArtifactAst;
import org.mule.runtime.config.api.dsl.model.ComponentBuildingDefinitionRegistry;
import org.mule.runtime.config.api.dsl.model.ResourceProvider;
import org.mule.runtime.config.api.dsl.processor.ArtifactConfig;
import org.mule.runtime.dsl.api.xml.parser.ConfigLine;

import java.io.InputStream;
import java.util.Optional;
import java.util.Set;

// THIS CLASS WAS COPIED FROM git@github.com:mulesoft/mule-datasense-api.git
class MuleAppUtil {

  public static Optional<ConfigLine> loadConfigLines(InputStream inputStream) {
    return MuleAppHelper.loadConfigLines(inputStream);
  }

  public static ArtifactAst loadApplicationModel(ArtifactConfig artifactConfig,
                                                 Set<ExtensionModel> extensionModels,
                                                 Optional<ComponentBuildingDefinitionRegistry> componentBuildingDefinitionRegistry,
                                                 ResourceProvider externalResourceProvider)
      throws Exception {
    return MuleAppHelper.loadApplicationModel(artifactConfig, extensionModels, componentBuildingDefinitionRegistry,
                                              externalResourceProvider);
  }

  public static Optional<ArtifactAst> createInternalApplicationModel(String name,
                                                                     InputStream inputStream) {
    return MuleAppHelper.createInternalApplicationModel(name, inputStream);
  }
}
