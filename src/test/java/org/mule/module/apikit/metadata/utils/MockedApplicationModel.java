/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata.utils;

import org.mule.runtime.api.meta.model.ExtensionModel;
import org.mule.runtime.ast.api.ArtifactAst;
import org.mule.runtime.ast.api.ComponentAst;
import org.mule.runtime.ast.api.xml.AstXmlParser;
import org.mule.runtime.core.api.extension.ExtensionManager;
import org.mule.runtime.core.api.extension.RuntimeExtensionModelProvider;
import org.mule.runtime.core.api.registry.SpiServiceRegistry;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class MockedApplicationModel implements ApplicationModel {

  private final ArtifactAst applicationModel;
  private final List<String> typesDataList;

  private MockedApplicationModel(ArtifactAst applicationModel,
                                 List<String> typesDataList) {
    this.applicationModel = applicationModel;
    this.typesDataList = new ArrayList<>();
    this.typesDataList.addAll(typesDataList);
  }

  @Override
  public Optional<URI> findResource(String resource) {
    return Optional.empty();
  }

  @Override
  public ArtifactAst getMuleApplicationModel() {
    return applicationModel;
  }

  @Override
  public Optional<ComponentAst> findNamedComponent(String name) {
    return getMuleApplicationModel().topLevelComponentsStream()
        .filter(comp -> name.equals(comp.getComponentId().orElse(null)))
        .findFirst();
  }

  @Override
  public List<String> findTypesDataList() {
    return typesDataList;
  }


  public static class Builder {

    private URI configURI;
    private List<String> typesDataList;
    private ExtensionManager extensionManager;

    public Builder() {
      typesDataList = new ArrayList<>();
    }

    public Builder extensionManager(ExtensionManager extensionManager) {
      this.extensionManager = extensionManager;
      return this;
    }


    public Builder addConfig(File configData) {
      this.configURI = configData.toURI();
      return this;
    }

    public MockedApplicationModel build() {

      Set<ExtensionModel> extensionModels = extensionManager.getExtensions();
      ArtifactAst applicationModel = AstXmlParser.builder()
          .withExtensionModels(extensionModels)
          .withExtensionModels(loadRuntimeExtensionModels())
          .withSchemaValidationsDisabled()
          .build()
          .parse(configURI);

      return new MockedApplicationModel(applicationModel, typesDataList);
    }

    private static List<ExtensionModel> loadRuntimeExtensionModels() {
      Collection<RuntimeExtensionModelProvider> runtimeExtensionModelProviders = new SpiServiceRegistry()
          .lookupProviders(
                           RuntimeExtensionModelProvider.class,
                           Thread
                               .currentThread()
                               .getContextClassLoader());

      List<ExtensionModel> runtimeExtensionModels = new ArrayList<>();
      for (RuntimeExtensionModelProvider runtimeExtensionModelProvider : runtimeExtensionModelProviders) {
        runtimeExtensionModels.add(runtimeExtensionModelProvider.createExtensionModel());
      }
      return runtimeExtensionModels;
    }
  }
}
