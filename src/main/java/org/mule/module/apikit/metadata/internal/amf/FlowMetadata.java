/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata.internal.amf;

import amf.apicontract.client.platform.model.domain.EndPoint;
import amf.apicontract.client.platform.model.domain.Parameter;
import amf.apicontract.client.platform.model.domain.Operation;
import amf.apicontract.client.platform.model.domain.Request;
import amf.apicontract.client.platform.model.domain.Payload;
import amf.apicontract.client.platform.model.domain.Response;
import amf.core.client.platform.model.domain.Shape;
import amf.shapes.client.platform.ShapesConfiguration;
import amf.shapes.client.platform.ShapesElementClient;
import amf.shapes.client.platform.model.domain.AnyShape;
import amf.shapes.client.platform.model.domain.Example;
import org.mule.metadata.api.builder.BaseTypeBuilder;
import org.mule.metadata.api.builder.FunctionTypeBuilder;
import org.mule.metadata.api.builder.ObjectTypeBuilder;
import org.mule.metadata.api.model.FunctionType;
import org.mule.metadata.api.model.MetadataFormat;
import org.mule.metadata.api.model.MetadataType;
import org.mule.metadata.api.model.ObjectType;
import org.mule.metadata.message.api.MessageMetadataType;
import org.mule.metadata.message.api.MessageMetadataTypeBuilder;
import org.mule.metadata.message.api.MuleEventMetadataType;
import org.mule.metadata.message.api.MuleEventMetadataTypeBuilder;
import org.mule.module.apikit.metadata.internal.model.ApiCoordinate;
import org.mule.module.apikit.metadata.internal.model.CertificateFields;
import org.mule.module.apikit.metadata.internal.model.HttpRequestAttributesFields;
import org.mule.module.apikit.metadata.internal.utils.CommonMetadataFactory;
import org.mule.runtime.api.metadata.MediaType;
import org.mule.runtime.apikit.metadata.api.MetadataSource;
import org.mule.runtime.apikit.metadata.api.Notifier;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.mule.module.apikit.metadata.internal.amf.MetadataFactory.fromJSONSchema;
import static org.mule.module.apikit.metadata.internal.amf.MetadataFactory.fromXSDSchema;
import static org.mule.module.apikit.metadata.internal.utils.CommonMetadataFactory.defaultMetadata;
import static org.mule.runtime.api.metadata.MediaType.APPLICATION_JSON;
import static org.mule.runtime.api.metadata.MediaType.parse;

class FlowMetadata implements MetadataSource {

  private static final String PARAMETER_INPUT_METADATA = "inputMetadata";
  private static final Pattern STATUS_CODE_2XX_PATTERN = Pattern.compile("^2\\d{2}$");

  final private EndPoint endPoint;
  final private Operation operation;
  final private ApiCoordinate coordinate;
  final private Map<String, Parameter> baseUriParameters;
  final private Notifier notifier;

  FlowMetadata(final EndPoint endPoint, final Operation operation, final ApiCoordinate coordinate,
               final Map<String, Parameter> baseUriParameters,
               Notifier notifier) {
    this.endPoint = endPoint;
    this.operation = operation;
    this.coordinate = coordinate;
    this.baseUriParameters = baseUriParameters;
    this.notifier = notifier;
  }

  @Override
  public Optional<FunctionType> getMetadata() {
    final MuleEventMetadataType input = inputMetadata(operation, coordinate, baseUriParameters);
    final MuleEventMetadataType output = outputMetadata(operation, coordinate);

    // FunctionType
    final FunctionTypeBuilder builder = BaseTypeBuilder.create(MetadataFormat.JAVA).functionType();
    final FunctionType function = builder
        .addParameterOf(PARAMETER_INPUT_METADATA, input)
        .returnType(output)
        .build();

    return of(function);
  }

  private MuleEventMetadataType inputMetadata(final Operation operation, final ApiCoordinate coordinate,
                                              final Map<String, Parameter> baseUriParameters) {

    final MessageMetadataType message = new MessageMetadataTypeBuilder()
        .payload(getInputPayload(operation, coordinate))
        .attributes(getInputAttributes(operation, baseUriParameters)).build();

    return new MuleEventMetadataTypeBuilder().message(message).build();
  }

  private MuleEventMetadataType outputMetadata(final Operation operation, final ApiCoordinate coordinate) {
    final MessageMetadataType message = new MessageMetadataTypeBuilder()
        .payload(getOutputPayload(operation, coordinate)).build();

    // TODO I' generating a compatible metadata with previous version
    return new MuleEventMetadataTypeBuilder().message(message)
        .addVariable("outboundHeaders", getOutputHeaders(operation).build())
        .addVariable("httpStatus", MetadataFactory.stringMetadata()).build();
  }

  private MetadataType getInputPayload(final Operation operation, final ApiCoordinate coordinate) {

    final Request request = operation.request();
    if (request == null) {
      return defaultMetadata();
    }

    final List<Payload> payloads = request.payloads();
    final Optional<Payload> payload = findPayload(payloads, coordinate.getMediaType());
    return payload.map(p -> loadIOPayloadMetadata(p, coordinate, "input")).orElseGet(CommonMetadataFactory::defaultMetadata);
  }

  private MetadataType getOutputPayload(final Operation operation, final ApiCoordinate coordinate) {

    final Optional<Response> response = findFirstResponse(operation);
    if (!response.isPresent()) {
      return defaultMetadata();
    }
    final List<Payload> payloads = response.get().payloads();
    final Optional<Payload> payload = findPayload(payloads, coordinate.getMediaType());

    return payload.map(p -> loadIOPayloadMetadata(p, coordinate, "output")).orElseGet(CommonMetadataFactory::defaultMetadata);
  }

  private ObjectTypeBuilder getOutputHeaders(final Operation operation) {
    final List<Parameter> headers = findFirstResponse(operation).map(Response::headers).orElse(emptyList());

    final ObjectTypeBuilder builder = BaseTypeBuilder.create(MetadataFormat.JAVA).objectType();

    headers.forEach(header -> builder.addField().key(header.name().value().toLowerCase()).value(loadIOParameterMetadata(header))
        .required(header.required().value()));

    return builder;
  }


  private ObjectType getInputAttributes(final Operation operation, final Map<String, Parameter> baseUrParameters) {

    // TODO I'm generating compatible metadata with test golden files
    final ObjectTypeBuilder builder = BaseTypeBuilder.create(MetadataFormat.JAVA).objectType();
    builder.addField()
        .key(HttpRequestAttributesFields.ATTRIBUTES_CLIENT_CERTIFICATE.getName())
        .required(false)
        .value(getClientCertificate());
    builder.addField()
        .key(HttpRequestAttributesFields.ATTRIBUTES_HEADERS.getName())
        .required(true)
        .value(getInputHeaders(operation));
    builder.addField()
        .key(HttpRequestAttributesFields.ATTRIBUTES_LISTENER_PATH.getName())
        .required(true)
        .value(MetadataFactory.stringMetadata());
    builder.addField()
        .key(HttpRequestAttributesFields.ATTRIBUTES_METHOD.getName())
        .required(true)
        .value(MetadataFactory.stringMetadata());
    builder.addField()
        .key(HttpRequestAttributesFields.ATTRIBUTES_QUERY_PARAMS.getName())
        .required(true)
        .value(getQueryParameters(operation));
    builder.addField()
        .key(HttpRequestAttributesFields.ATTRIBUTES_QUERY_STRING.getName())
        .required(true)
        .value(MetadataFactory.stringMetadata());
    builder.addField()
        .key(HttpRequestAttributesFields.ATTRIBUTES_RELATIVE_PATH.getName())
        .required(true)
        .value(MetadataFactory.stringMetadata());
    builder.addField()
        .key(HttpRequestAttributesFields.ATTRIBUTES_REMOTE_ADDRESS.getName())
        .required(true)
        .value(MetadataFactory.stringMetadata());
    builder.addField()
        .key(HttpRequestAttributesFields.ATTRIBUTES_REQUEST_PATH.getName())
        .required(true)
        .value(MetadataFactory.stringMetadata());
    builder.addField()
        .key(HttpRequestAttributesFields.ATTRIBUTES_REQUEST_URI.getName())
        .required(true)
        .value(MetadataFactory.stringMetadata());
    builder.addField()
        .key(HttpRequestAttributesFields.ATTRIBUTES_SCHEME.getName())
        .required(true)
        .value(MetadataFactory.stringMetadata());
    builder.addField()
        .key(HttpRequestAttributesFields.ATTRIBUTES_URI_PARAMS.getName())
        .required(true)
        .value(getUriParameters(endPoint, operation, baseUrParameters));
    builder.addField()
        .key(HttpRequestAttributesFields.ATTRIBUTES_VERSION.getName())
        .required(true)
        .value(MetadataFactory.stringMetadata());
    builder.addField()
        .key(HttpRequestAttributesFields.ATTRIBUTES_LOCAL_ADDRESS.getName())
        .required(true)
        .value(MetadataFactory.stringMetadata());

    return builder.build();
  }

  private ObjectTypeBuilder getInputHeaders(final Operation operation) {
    final ObjectTypeBuilder builder = BaseTypeBuilder.create(MetadataFormat.JAVA).objectType();

    final Request request = operation.request();
    if (request != null) {
      request.headers().forEach(header -> builder.addField().key(header.name().value()).value(loadIOParameterMetadata(header))
          .required(header.required().value()));
    }

    return builder;
  }

  private static MetadataType getClientCertificate() {
    final ObjectTypeBuilder builder = BaseTypeBuilder.create(MetadataFormat.JAVA).objectType();

    builder.addField().key(CertificateFields.CLIENT_CERTIFICATE_PUBLIC_KEY.getName()).value(MetadataFactory.objectMetadata());
    builder.addField().key(CertificateFields.CLIENT_CERTIFICATE_TYPE.getName()).value(MetadataFactory.stringMetadata());
    builder.addField().key(CertificateFields.CLIENT_CERTIFICATE_ENCODED.getName()).value(MetadataFactory.binaryMetadata());

    return builder.build();
  }

  private ObjectTypeBuilder getQueryParameters(final Operation operation) {
    final ObjectTypeBuilder builder = BaseTypeBuilder.create(MetadataFormat.JAVA).objectType();

    final Request request = operation.request();
    if (request != null) {
      request.queryParameters()
          .forEach(param -> builder.addField().key(param.name().value()).value(loadIOParameterMetadata(param))
              .required(param.required().value()));
    }

    return builder;
  }

  private ObjectTypeBuilder getUriParameters(final EndPoint endPoint, Operation operation,
                                             final Map<String, Parameter> baseUriParameters) {
    final ObjectTypeBuilder builder = BaseTypeBuilder.create(MetadataFormat.JAVA).objectType();
    Map<String, Parameter> parameters = new LinkedHashMap<>(baseUriParameters);
    parameters.putAll(getEndpointUriParametersFields(endPoint));
    parameters.putAll(getUriParametersFromOperation(operation));
    Predicate<Entry<String, Parameter>> versionFilter = e -> !"version".equals(e.getKey());
    Consumer<Parameter> addParameterToBuilder = p -> builder.addField().key(p.name().value()).value(loadIOParameterMetadata(p))
        .required(p.required().value());
    parameters.entrySet().stream().filter(versionFilter).map(e -> e.getValue())
        .forEach(addParameterToBuilder);
    return builder;
  }

  private Map<String, Parameter> getEndpointUriParametersFields(EndPoint endPoint) {
    List<Parameter> endPointParameters = endPoint.parameters();
    return isEmpty(endPointParameters) ? emptyMap()
        : endPointParameters.stream()
            .collect(LinkedHashMap::new, (map, param) -> map.put(param.name().value(), param), Map::putAll);
  }

  private Map<String, Parameter> getUriParametersFromOperation(Operation operation) {
    Map<String, Parameter> uriParams = new LinkedHashMap<>();
    List<Parameter> requestUriParams =
        operation.requests().stream().map(r -> r.uriParameters()).flatMap(List::stream).collect(toList());
    requestUriParams.forEach(p -> uriParams.put(p.name().value(), p));
    return uriParams;
  }

  private static Optional<Response> findFirstResponse(final Operation operation) {
    return operation.responses().stream()
        .filter(response -> STATUS_CODE_2XX_PATTERN.matcher(response.statusCode().value()).matches()
            && !response.payloads().isEmpty())
        .findFirst();
  }

  private static Optional<Payload> findPayload(final List<Payload> payloads, final String mediaType) {
    if (payloads.isEmpty()) {
      return empty();
    }

    if (payloads.size() == 1 || mediaType == null) {
      return of(payloads.get(0));
    }

    MediaType mType = parse(mediaType);
    return payloads.stream().filter(p -> parse(p.mediaType().value()).matches(mType)).findFirst();
  }

  private MetadataType loadIOParameterMetadata(final Parameter parameter) {
    try {
      Example example = getFirstExample(parameter.schema()).orElse(null);
      ShapesElementClient client = ShapesConfiguration.predefined().elementClient();
      return fromJSONSchema(parameter.schema(),
                            example != null ? client.renderExample(example, APPLICATION_JSON.toString()) : "");
    } catch (Exception e) {
      notifier.warn(format("Error while trying to resolve metadata for parameter '%s'\nDetails: %s", parameter.name(),
                           e.getMessage()));
    }
    return MetadataFactory.defaultMetadata(parameter.schema());
  }

  private MetadataType loadIOPayloadMetadata(final Payload payload, ApiCoordinate coordinate,
                                             String payloadDescription) {
    try {
      String mediaType = payload.mediaType().option().orElse("").toLowerCase();
      Example example = getFirstExample(payload.schema()).orElse(null);
      ShapesElementClient client = ShapesConfiguration.predefined().elementClient();
      return mediaType.contains("xml") ? fromXSDSchema(payload.schema(), example != null ? example.value().value() : "")
          : fromJSONSchema(payload.schema(), example != null ? client.renderExample(example, APPLICATION_JSON.toString()) : "");
    } catch (Exception e) {
      notifier.warn(format("Error while trying to resolve %s payload metadata for flow '%s'.\nDetails: %s", payloadDescription,
                           coordinate.getFlowName(), e.getMessage()));
    }
    return MetadataFactory.defaultMetadata(payload.schema());
  }

  /**
   * Picks the first example of the schema if exists.
   *
   * @param schema
   * @return
   */
  private Optional<Example> getFirstExample(Shape schema) {
    if (schema instanceof AnyShape) {
      List<Example> examples = ((AnyShape) schema).examples();
      if (examples != null && !examples.isEmpty()) {
        return Optional.of(examples.get(0));
      }
    }
    return Optional.empty();
  }

}
