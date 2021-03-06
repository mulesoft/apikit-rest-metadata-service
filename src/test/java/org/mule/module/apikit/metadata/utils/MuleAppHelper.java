/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata.utils;

import static org.mule.runtime.dsl.api.xml.parser.XmlConfigurationDocumentLoader.noValidationDocumentLoader;

import org.mule.runtime.api.exception.MuleRuntimeException;
import org.mule.runtime.api.meta.model.ExtensionModel;
import org.mule.runtime.ast.api.ArtifactAst;
import org.mule.runtime.config.api.dsl.model.ComponentBuildingDefinitionRegistry;
import org.mule.runtime.config.api.dsl.model.ResourceProvider;
import org.mule.runtime.config.api.dsl.processor.ArtifactConfig;
import org.mule.runtime.config.internal.model.ApplicationModel;
import org.mule.runtime.core.api.registry.ServiceRegistry;
import org.mule.runtime.core.api.registry.SpiServiceRegistry;
import org.mule.runtime.dsl.api.xml.XmlNamespaceInfoProvider;
import org.mule.runtime.dsl.api.xml.parser.ConfigFile;
import org.mule.runtime.dsl.api.xml.parser.ConfigLine;
import org.mule.runtime.dsl.internal.xml.parser.XmlApplicationParser;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.xml.parsers.SAXParserFactory;

import org.w3c.dom.Document;
import org.xml.sax.helpers.DefaultHandler;

import com.google.common.collect.ImmutableList;

// THIS CLASS WAS COPIED FROM git@github.com:mulesoft/mule-datasense-api.git
class MuleAppHelper {

  public static List<XmlNamespaceInfoProvider> discoverNamespaceInfoProviders(ServiceRegistry serviceRegistry,
                                                                              List<ClassLoader> pluginsClassLoaders) {
    ImmutableList.Builder<XmlNamespaceInfoProvider> namespaceInfoProvidersBuilder = ImmutableList.builder();
    namespaceInfoProvidersBuilder
        .addAll(serviceRegistry.lookupProviders(XmlNamespaceInfoProvider.class, Thread.currentThread().getContextClassLoader()));
    pluginsClassLoaders
        .forEach(pluginClassLoader -> namespaceInfoProvidersBuilder
            .addAll(serviceRegistry.lookupProviders(XmlNamespaceInfoProvider.class, pluginClassLoader)));
    return namespaceInfoProvidersBuilder.build();
  }

  public static Optional<ConfigLine> loadConfigLines(InputStream inputStream) {
    ServiceRegistry serviceRegistry = new SpiServiceRegistry();
    Document document =
        noValidationDocumentLoader().loadDocument(SAXParserFactory::newInstance, "config", inputStream, new DefaultHandler());
    XmlApplicationParser xmlApplicationParser =
        new XmlApplicationParser(discoverNamespaceInfoProviders(serviceRegistry, Collections.emptyList()));
    return xmlApplicationParser.parse(document.getDocumentElement());
  }

  public static ArtifactAst loadApplicationModel(ConfigLine configLine, String fileName) throws Exception {
    ConfigFile configFile = new ConfigFile(fileName, Collections.singletonList(configLine));
    ArtifactConfig artifactConfig = new ArtifactConfig.Builder().addConfigFile(configFile).build();

    return loadApplicationModel(artifactConfig,
                                Collections.emptySet(),
                                Optional.of(new ComponentBuildingDefinitionRegistry()),
                                s -> {
                                  throw new UnsupportedOperationException();
                                });
  }

  public static ArtifactAst loadApplicationModel(ArtifactConfig artifactConfig,
                                                 Set<ExtensionModel> extensionModels,
                                                 Optional<ComponentBuildingDefinitionRegistry> componentBuildingDefinitionRegistry,
                                                 ResourceProvider externalResourceProvider)
      throws Exception {
    return new ApplicationModel(artifactConfig, null, extensionModels, Collections.emptyMap(), Optional.empty(),
                                componentBuildingDefinitionRegistry, externalResourceProvider);
  }

  static Optional<ArtifactAst> createInternalApplicationModel(String name, InputStream inputStream) {
    return loadConfigLines(inputStream).map(configLine -> {
      try {
        return loadApplicationModel(configLine, name);
      } catch (Exception e) {
        throw new MuleRuntimeException(e);
      }
    });
  }
}
