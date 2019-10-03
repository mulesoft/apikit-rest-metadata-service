/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata.utils;

import static java.lang.String.format;
import static java.util.Collections.emptySet;
import static org.mule.module.apikit.metadata.utils.MetadataProviderUtil.createComponentBuildingDefinitionRegistry;

import org.mule.runtime.api.meta.model.ExtensionModel;
import org.mule.runtime.ast.api.ArtifactAst;
import org.mule.runtime.ast.api.ComponentAst;
import org.mule.runtime.config.api.dsl.model.ComponentBuildingDefinitionRegistry;
import org.mule.runtime.config.api.dsl.model.ResourceProvider;
import org.mule.runtime.config.api.dsl.processor.ArtifactConfig;
import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.core.api.extension.RuntimeExtensionModelProvider;
import org.mule.runtime.core.api.registry.SpiServiceRegistry;
import org.mule.runtime.dsl.api.component.ComponentBuildingDefinitionProvider;
import org.mule.runtime.dsl.api.xml.parser.ConfigFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

// THIS CLASS WAS COPIED FROM git@github.com:mulesoft/mule-datasense-api.git
public class MockedApplicationModel implements ApplicationModel {

  private static final transient Logger logger = LoggerFactory.getLogger(MockedApplicationModel.class);

  private final String name;
  private final URI baseURI;
  private final ArtifactAst applicationModel;
  private final List<String> typesDataList;

  private MockedApplicationModel(String name,
                                 ArtifactAst applicationModel,
                                 List<String> typesDataList, URI baseURI) {
    this.name = name;
    this.applicationModel = applicationModel;
    this.typesDataList = new ArrayList<>();
    this.typesDataList.addAll(typesDataList);
    this.baseURI = baseURI;
  }

  public String getName() {
    return name;
  }

  @Override
  public Optional<URI> findResource(String resource) {
    if (baseURI == null) {
      return Optional.empty();
    }
    return Optional.of(baseURI.resolve(resource));
  }

  @Override
  public ArtifactAst getMuleApplicationModel() {
    return applicationModel;
  }

  public static ApplicationModel load(String name, String content) throws Exception {
    return load(name, content, null);
  }

  public static ApplicationModel load(String name, String content, String typesData) throws Exception {
    Builder builder = new Builder();
    builder.addConfig(name, IOUtils.toInputStream(content));
    if (typesData != null) {
      builder.typesData(typesData);
    }
    return builder.build();

  }

  public static ApplicationModel load(String name, File appDir) throws Exception {
    return load(name, appDir, null, null);
  }

  public static ApplicationModel load(String name, File appDir, MuleContext muleContext) throws Exception {
    return load(name, appDir, null, muleContext);
  }

  public static ApplicationModel load(String name, File appDir, File typesDataFile) throws Exception {
    return load(name, appDir, typesDataFile, null);
  }

  public static ApplicationModel load(String name, File appDir, File typesDataFile, MuleContext muleContext) throws Exception {
    Builder builder = new Builder();
    builder.addConfig(name, new File(appDir, name));
    if (typesDataFile != null) {
      builder.typesData(typesDataFile);
    }
    if (muleContext != null) {
      builder.muleContext(muleContext);
    }
    return builder.build();
  }

  @Override
  public Optional<ComponentAst> findNamedComponent(String name) {
    return getMuleApplicationModel().topLevelComponentsStream()
        .filter(comp -> name.equals(comp.getName().orElse(null)))
        .findFirst();
  }

  @Override
  public List<String> findTypesDataList() {
    return typesDataList;
  }


  public static class Builder {

    private final ArtifactConfig.Builder artifactConfigBuilder;
    private ResourceProvider resourceProvider;
    private MuleContext muleContext;
    private final List<String> typesDataList;
    private URI baseURI;

    public Builder() {
      artifactConfigBuilder = new ArtifactConfig.Builder();
      typesDataList = new ArrayList<>();
    }

    public Builder muleContext(MuleContext muleContext) {
      Preconditions.checkNotNull(muleContext);
      this.muleContext = muleContext;
      return this;
    }

    public Builder resourceProvider(ResourceProvider resourceProvider) {
      Preconditions.checkNotNull(muleContext);
      this.resourceProvider = resourceProvider;
      return this;
    }

    public Builder typesData(String typesData) {
      Preconditions.checkNotNull(typesData);
      typesDataList.add(typesData);
      return this;
    }

    public Builder baseURI(URI baseURI) {
      Preconditions.checkNotNull(baseURI);
      this.baseURI = baseURI;
      return this;
    }

    public Builder typesData(File typesDataFile) throws IOException {
      Preconditions.checkNotNull(typesDataFile);
      return typesData(IOUtils.toString(typesDataFile.toURI().toURL()));
    }

    public Builder addConfig(String configName, String configData) {
      Preconditions.checkNotNull(configName);
      Preconditions.checkNotNull(configData);
      return addConfig(configName, IOUtils.toInputStream(configData));
    }

    public Builder addConfig(String configName, File configData) throws IOException {
      Preconditions.checkNotNull(configName);
      Preconditions.checkNotNull(configData);
      try (FileInputStream fileInputStream = new FileInputStream(configData)) {
        return addConfig(configName, fileInputStream);
      }
    }

    public Builder addConfig(String configName, InputStream configData) {
      Preconditions.checkNotNull(configName);
      Preconditions.checkNotNull(configData);
      artifactConfigBuilder.addConfigFile(new ConfigFile(configName, Collections.singletonList(
                                                                                               MuleAppUtil
                                                                                                   .loadConfigLines(configData)
                                                                                                   .orElseThrow(() -> new IllegalArgumentException(format("Failed to get %s.",
                                                                                                                                                          configName))))));
      return this;
    }

    private ResourceProvider getResourceProvider() {
      return Optional.ofNullable(resourceProvider).orElse(s -> {
        throw new UnsupportedOperationException();
      });
    }

    public MockedApplicationModel build() throws Exception {
      ImmutableSet<ExtensionModel> extensions = ImmutableSet.<ExtensionModel>builder()
        .addAll(muleContext != null ? muleContext.getExtensionManager().getExtensions() : emptySet())
        .addAll(discoverRuntimeExtensionModels())
        .build();
      ClassLoader cl = ComponentBuildingDefinitionProvider.class.getClassLoader();
      ComponentBuildingDefinitionRegistry registry = createComponentBuildingDefinitionRegistry(extensions, cl);
      ArtifactAst toolingApp = MuleAppUtil.loadApplicationModel(artifactConfigBuilder.build(),
                                                                extensions,
                                                                Optional.ofNullable(registry),
                                                                getResourceProvider());

      logger.debug("Resolved locations for Tooling ApplicationModel:");
      toolingApp
          .recursiveStream().forEach(componentModel -> {
            if (componentModel.getLocation() != null) {
              logger.debug(format("Location: %s (%s)", componentModel.getLocation().getLocation(),
                                  componentModel.getIdentifier()));
            }
          });

      if (muleContext != null) {
        logger.debug("Resolved locations from deployed application:");
        muleContext.getConfigurationComponentLocator().findAllLocations().stream()
            .forEach(componentLocation -> logger.debug(
                                                       format("Location: %s (%s)", componentLocation.getLocation(),
                                                              componentLocation.getComponentIdentifier())));
      }

      return new MockedApplicationModel("", toolingApp, typesDataList, baseURI);
    }

    public Set<ExtensionModel> discoverRuntimeExtensionModels() {
      final Set<ExtensionModel> extensionModels = new HashSet<>();

      Collection<RuntimeExtensionModelProvider> runtimeExtensionModelProviders = new SpiServiceRegistry()
          .lookupProviders(RuntimeExtensionModelProvider.class, Thread.currentThread().getContextClassLoader());
      for (RuntimeExtensionModelProvider runtimeExtensionModelProvider : runtimeExtensionModelProviders) {
        extensionModels.add(runtimeExtensionModelProvider.createExtensionModel());
      }
      return extensionModels;
    }

  }
}
