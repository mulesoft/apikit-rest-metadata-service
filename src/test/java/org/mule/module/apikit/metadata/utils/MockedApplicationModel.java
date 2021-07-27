/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata.utils;

import com.google.common.base.Preconditions;
import org.mule.extension.http.internal.temporary.HttpConnector;
import org.mule.extension.socket.api.SocketsExtension;
import org.mule.module.apikit.ApikitExtensionLoadingDelegate;
import org.mule.runtime.api.dsl.DslResolvingContext;
import org.mule.runtime.api.meta.model.ExtensionModel;
import org.mule.runtime.api.meta.model.declaration.fluent.ExtensionDeclarer;
import org.mule.runtime.ast.api.ArtifactAst;
import org.mule.runtime.ast.api.ComponentAst;
import org.mule.runtime.ast.api.xml.AstXmlParser;
import org.mule.runtime.config.api.dsl.processor.ArtifactConfig;
import org.mule.runtime.core.api.extension.MuleExtensionModelProvider;
import org.mule.runtime.core.api.extension.RuntimeExtensionModelProvider;
import org.mule.runtime.core.api.registry.SpiServiceRegistry;
import org.mule.runtime.extension.api.loader.ExtensionLoadingContext;
import org.mule.runtime.extension.internal.loader.DefaultExtensionLoadingContext;
import org.mule.runtime.extension.internal.loader.ExtensionModelFactory;
import org.mule.runtime.module.extension.api.loader.java.DefaultJavaExtensionModelLoader;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static org.mule.runtime.api.dsl.DslResolvingContext.getDefault;
import static org.mule.runtime.core.api.config.MuleManifest.getProductVersion;
import static org.mule.runtime.module.extension.api.loader.AbstractJavaExtensionModelLoader.TYPE_PROPERTY_NAME;
import static org.mule.runtime.module.extension.api.loader.AbstractJavaExtensionModelLoader.VERSION;

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

    private final ArtifactConfig.Builder artifactConfigBuilder;
    private final List<String> typesDataList;
    private URI configURI;

    public Builder() {
      artifactConfigBuilder = new ArtifactConfig.Builder();
      typesDataList = new ArrayList<>();
    }

    public Builder addConfig(File configData) {
      Preconditions.checkNotNull(configData);
      this.configURI = configData.toURI();
      return this;
    }

    public MockedApplicationModel build() {

      List<ExtensionModel> extensionModels =
          Arrays.asList(loadApikitExtensionModel(), loadExtensionModel(HttpConnector.class),
                        loadExtensionModel(SocketsExtension.class));
      ArtifactAst applicationModel = AstXmlParser.builder()
          .withExtensionModels(extensionModels)
          .withExtensionModels(loadRuntimeExtensionModels())
          .withSchemaValidationsDisabled()
          .build()
          .parse(configURI);

      return new MockedApplicationModel(applicationModel, typesDataList);
    }


    private static ExtensionModel loadApikitExtensionModel() {
      ApikitExtensionLoadingDelegate apikitExtensionLoadingDelegate = new ApikitExtensionLoadingDelegate();
      ExtensionDeclarer extensionDeclarer = new ExtensionDeclarer();
      apikitExtensionLoadingDelegate.accept(extensionDeclarer, null);
      ExtensionLoadingContext ctx =
          new DefaultExtensionLoadingContext(extensionDeclarer, Thread.currentThread().getContextClassLoader(),
                                             DslResolvingContext.getDefault(emptySet()));
      return new ExtensionModelFactory().create(ctx);
    }

    private static List<ExtensionModel> loadRuntimeExtensionModels() {
      Collection<RuntimeExtensionModelProvider> runtimeExtensionModelProviders = new SpiServiceRegistry().lookupProviders(
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

    private static ExtensionModel loadExtensionModel(Class extensionClass) {
      DefaultJavaExtensionModelLoader extensionModelLoader = new DefaultJavaExtensionModelLoader();

      DslResolvingContext dslResolvingContext = getDefault(singleton(MuleExtensionModelProvider.getExtensionModel()));
      Map<String, Object> params = new HashMap<>();
      params.put(TYPE_PROPERTY_NAME, extensionClass.getName());
      params.put(VERSION, getProductVersion());

      return extensionModelLoader.loadExtensionModel(extensionClass.getClassLoader(), dslResolvingContext, params);
    }

  }
}
