/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata.utils;

import com.google.common.collect.ImmutableList;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.xml.parsers.SAXParserFactory;
import org.mule.runtime.api.exception.MuleRuntimeException;
import org.mule.runtime.api.meta.model.ExtensionModel;
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
import org.w3c.dom.Document;
import org.xml.sax.helpers.DefaultHandler;

import static org.mule.runtime.dsl.api.xml.parser.XmlConfigurationDocumentLoader.noValidationDocumentLoader;

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

  public static ApplicationModel loadApplicationModel(ConfigLine configLine,
                                                      String fileName)
      throws Exception {
    ArtifactConfig artifactConfig = new ArtifactConfig.Builder()
        .addConfigFile(new ConfigFile(fileName, Collections.singletonList(configLine))).build();
    return loadApplicationModel(artifactConfig, fileName, Collections.emptySet(),
                                Optional.of(new ComponentBuildingDefinitionRegistry()),
                                false, s -> {
                                  throw new UnsupportedOperationException();
                                });
  }

  public static ApplicationModel loadApplicationModel(
                                                      ArtifactConfig artifactConfig,
                                                      String fileName,
                                                      Set<ExtensionModel> extensionModels,
                                                      Optional<ComponentBuildingDefinitionRegistry> componentBuildingDefinitionRegistry,
                                                      boolean runtimeMode,
                                                      ResourceProvider externalResourceProvider)
      throws Exception {
    return new ApplicationModel(artifactConfig, null,
                                extensionModels, Collections.emptyMap(),
                                Optional.empty(), componentBuildingDefinitionRegistry, runtimeMode, externalResourceProvider);
  }

  public static Optional<ApplicationModel> createInternalApplicationModel(String name,
                                                                          InputStream inputStream) {
    return loadConfigLines(inputStream).map(configLine -> {
      try {
        return loadApplicationModel(configLine, name);
      } catch (Exception e) {
        throw new MuleRuntimeException(e);
      }
    });
  }
}
