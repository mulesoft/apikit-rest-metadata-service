/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata.utils;

import java.util.Set;
import java.util.function.Predicate;
import org.mule.runtime.api.meta.model.ExtensionModel;
import org.mule.runtime.config.api.dsl.model.ComponentBuildingDefinitionRegistry;
import org.mule.runtime.config.internal.dsl.model.ConfigurationDependencyResolver;
import org.mule.runtime.config.internal.dsl.model.MinimalApplicationModelGenerator;
import org.mule.runtime.config.internal.model.ApplicationModel;
import org.mule.runtime.config.internal.model.ComponentModel;
import org.mule.runtime.core.api.registry.ServiceRegistry;
import org.mule.runtime.core.api.registry.SpiServiceRegistry;
import org.mule.runtime.dsl.api.component.ComponentBuildingDefinitionProvider;
import org.mule.runtime.module.extension.internal.config.ExtensionBuildingDefinitionProvider;

// THIS CLASS WAS COPIED FROM git@github.com:mulesoft/mule-datasense-api.git
class MetadataProviderUtil {

  public static ComponentBuildingDefinitionRegistry createComponentBuildingDefinitionRegistry(
                                                                                              Set<ExtensionModel> extensionModels,
                                                                                              ClassLoader classLoader) {
    ServiceRegistry serviceRegistry = new SpiServiceRegistry();
    final ComponentBuildingDefinitionRegistry componentBuildingDefinitionRegistry =
        new ComponentBuildingDefinitionRegistry();
    serviceRegistry.lookupProviders(ComponentBuildingDefinitionProvider.class, classLoader)
        .forEach(componentBuildingDefinitionProvider -> {
          if (componentBuildingDefinitionProvider instanceof ExtensionBuildingDefinitionProvider) {
            ((ExtensionBuildingDefinitionProvider) componentBuildingDefinitionProvider)
                .setExtensionModels(extensionModels);
          }
          componentBuildingDefinitionProvider.init();
          componentBuildingDefinitionProvider.getComponentBuildingDefinitions()
              .forEach(componentBuildingDefinitionRegistry::register);
        });
    return componentBuildingDefinitionRegistry;
  }

  public static ApplicationModel generateMinimalApplicationModel(ApplicationModel muleApplicationModel,
                                                                 Set<ExtensionModel> extensions,
                                                                 Predicate<ComponentModel> componentModelFilter) {
    final ComponentBuildingDefinitionRegistry componentBuildingDefinitionRegistry =
        MetadataProviderUtil
            .createComponentBuildingDefinitionRegistry(extensions, Thread.currentThread()
                .getContextClassLoader() /*muleApplicationModel.getClass().getClassLoader()*/);
    final ConfigurationDependencyResolver dependencyResolver =
        new ConfigurationDependencyResolver(muleApplicationModel,
                                            componentBuildingDefinitionRegistry);
    final MinimalApplicationModelGenerator minimalApplicationModelGenerator =
        new MinimalApplicationModelGenerator(dependencyResolver);
    muleApplicationModel.executeOnEveryComponentTree(componentModel -> {
      componentModel.setEnabled(false);
    });
    return minimalApplicationModelGenerator.getMinimalModel(componentModelFilter);
  }

}
