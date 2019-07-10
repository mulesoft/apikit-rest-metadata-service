/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata.internal.model;

import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.mule.runtime.api.component.ComponentIdentifier.buildFromStringRepresentation;

import org.mule.runtime.api.component.ComponentIdentifier;
import org.mule.runtime.apikit.metadata.api.Notifier;
import org.mule.runtime.apikit.metadata.api.ResourceLoader;
import org.mule.runtime.ast.api.ArtifactAst;
import org.mule.runtime.ast.api.ComponentAst;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class ApplicationModelWrapper {

  private final static String PARAMETER_NAME = "name";
  private final static String PARAMETER_API_DEFINITION = "api";
  private final static String PARAMETER_RAML_DEFINITION = "raml";
  private final static String PARAMETER_OUTPUT_HEADERS_VAR = "outboundHeadersMapName";
  private final static String PARAMETER_HTTP_STATUS_VAR = "httpStatusVarName";
  private final static String PARAMETER_PARSER = "parser";

  private final static String PARAMETER_RESOURCE = "resource";
  private final static String PARAMETER_ACTION = "action";
  private final static String PARAMETER_CONTENT_TYPE = "content-type";
  private final static String PARAMETER_FLOW_REF = "flow-ref";

  private static final ComponentIdentifier FLOW = buildFromStringRepresentation("flow");
  private static final ComponentIdentifier APIKIT_CONFIG = buildFromStringRepresentation("apikit:config");

  private final ArtifactAst applicationModel;
  private final ResourceLoader resourceLoader;
  private final Notifier notifier;

  private final Map<String, ApikitConfig> configMap;
  private final Map<String, ApiCoordinate> metadataFlows;

  public ApplicationModelWrapper(final ArtifactAst applicationModel, final ResourceLoader loader, final Notifier notifier) {
    this.applicationModel = applicationModel;
    this.resourceLoader = loader;
    this.notifier = notifier;
    configMap = loadConfigs();
    metadataFlows = loadFlows();
  }

  private Map<String, ApiCoordinate> loadFlows() {
    // Finding all valid flows
    final List<Flow> flows = findFlows();

    // Creating a Coords Factory, giving the list of all valid config names
    final ApiCoordinateFactory coordsFactory = new ApiCoordinateFactory(getConfigNames());
    final Map<String, ApiCoordinate> conventionCoordinates = createCoordinatesForConventionFlows(flows, coordsFactory);
    final Map<String, ApiCoordinate> flowMappingCoordinates = createCoordinatesForMappingFlows(flows, coordsFactory);

    // Merging both results
    Map<String, ApiCoordinate> result = new HashMap<>(conventionCoordinates);
    result.putAll(flowMappingCoordinates);
    return result;
  }

  private Map<String, ApikitConfig> loadConfigs() {
    return applicationModel.topLevelComponentsStream()
        .filter(ApplicationModelWrapper::isApikitConfig)
        .map(this::createApikitConfig)
        .collect(toMap(ApikitConfig::getName, identity()));
  }

  private Set<String> getConfigNames() {
    return configMap.keySet();
  }

  Collection<ApikitConfig> getConfigurations() {
    return configMap.values();
  }

  private Map<String, ApiCoordinate> createCoordinatesForConventionFlows(final List<Flow> flows,
                                                                         final ApiCoordinateFactory coordsFactory) {
    return flows
        .stream()
        .map(flow -> coordsFactory.fromFlowName(flow.getName()))
        .filter(Optional::isPresent).map(Optional::get)
        .collect(toMap(ApiCoordinate::getFlowName, identity()));
  }

  private ApikitConfig createApikitConfig(final ComponentAst config) {
    final String configName = config.getRawParameterValue(PARAMETER_NAME).orElse(null);
    final String apiDefinition = getApiDefinition(config);
    final String outputHeadersVarName = config.getRawParameterValue(PARAMETER_OUTPUT_HEADERS_VAR).orElse(null);
    final String httpStatusVarName = config.getRawParameterValue(PARAMETER_HTTP_STATUS_VAR).orElse(null);
    final String parser = config.getRawParameterValue(PARAMETER_PARSER).orElse(null);

    final List<FlowMapping> flowMappings = config.recursiveStream()
        .filter(config.directChildrenPredicate())
        .filter(cfg -> ApikitElementIdentifiers.isFlowMappings(cfg.getIdentifier()))
        .flatMap(flowMappingsElement -> flowMappingsElement.recursiveStream()
            .filter(flowMappingsElement.directChildrenPredicate()))
        .filter(flowMapping -> ApikitElementIdentifiers.isFlowMapping(flowMapping.getIdentifier()))
        .map(unwrappedFlowMapping -> createFlowMapping(configName, unwrappedFlowMapping))
        .collect(toList());

    return new ApikitConfig(configName, apiDefinition, flowMappings, httpStatusVarName, outputHeadersVarName,
                            parser, resourceLoader, notifier);
  }

  private static String getApiDefinition(ComponentAst config) {
    return config.getRawParameterValue(PARAMETER_API_DEFINITION)
        .orElseGet(() -> config.getRawParameterValue(PARAMETER_RAML_DEFINITION).orElse(null));
  }

  public List<Flow> findFlows() {
    return findFlows(applicationModel);
  }

  public static List<Flow> findFlows(final ArtifactAst applicationModel) {
    return applicationModel.topLevelComponentsStream()
        .filter(ApplicationModelWrapper::isFlow)
        .map(ApplicationModelWrapper::createFlow)
        .collect(toList());
  }

  private static Flow createFlow(ComponentAst componentModel) {
    final String flowName = componentModel.getRawParameterValue(PARAMETER_NAME).orElse(null);
    return new Flow(flowName);
  }

  public Optional<ApiCoordinate> getApiCoordinate(final String flowName) {
    return ofNullable(metadataFlows.get(flowName));
  }

  public Optional<ApikitConfig> getConfig(final String name) {
    if (configMap.isEmpty()) {
      return empty();
    }

    // If the flow is not explicitly naming the config it belongs, we assume there is only one API
    return Optional.of(configMap.getOrDefault(name, configMap.values().iterator().next()));
  }

  private Map<String, ApiCoordinate> createCoordinatesForMappingFlows(final List<Flow> flows,
                                                                      final ApiCoordinateFactory factory) {
    final Set<String> flowNames = flows.stream().map(Flow::getName).collect(toSet());

    return configMap.values().stream()
        .flatMap(config -> config.getFlowMappings().stream())
        .filter(mapping -> flowNames.contains(mapping.getFlowRef()))
        .map(factory::createFromFlowMapping)
        .collect(toMap(ApiCoordinate::getFlowName, identity()));
  }


  private static boolean isFlow(final ComponentAst component) {
    return component.getIdentifier().equals(FLOW);
  }

  private static boolean isApikitConfig(final ComponentAst component) {
    return component.getIdentifier().equals(APIKIT_CONFIG);
  }

  private static FlowMapping createFlowMapping(final String configName, final ComponentAst component) {
    final String resource = component.getRawParameterValue(PARAMETER_RESOURCE).orElse(null);
    final String action = component.getRawParameterValue(PARAMETER_ACTION).orElse(null);
    final String contentType = component.getRawParameterValue(PARAMETER_CONTENT_TYPE).orElse(null);
    final String flowRef = component.getRawParameterValue(PARAMETER_FLOW_REF).orElse(null);

    return new FlowMapping(configName, resource, action, contentType, flowRef);
  }

}
